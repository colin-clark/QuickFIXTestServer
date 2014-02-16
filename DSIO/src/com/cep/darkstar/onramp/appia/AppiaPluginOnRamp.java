package com.cep.darkstar.onramp.appia;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.javtech.appia.ef.session.DefaultSessionPlugin;
import com.javtech.appia.ef.session.InboundPostValidationPlugin;
import com.javtech.appia.ef.session.OutboundPostValidationPlugin;
import com.javtech.appia.ef.session.SessionPluginFilter;
import com.javtech.javatoolkit.fix.FixConstants;
import com.javtech.javatoolkit.fix.FixMessageObject;
import com.javtech.javatoolkit.fix.codec.FixAttributeFactory;
import com.javtech.javatoolkit.message.Attribute;
import com.javtech.javatoolkit.message.MessageData;
import com.javtech.javatoolkit.message.MessageObject;


public class AppiaPluginOnRamp extends DefaultSessionPlugin 
		implements InboundPostValidationPlugin,OutboundPostValidationPlugin {
	
	/*
	 * Internal Classes
	 */
	public class CleanupThread implements Runnable {
		@Override
		public void run() {
			if ((System.currentTimeMillis() - last_received) > 1000L) {
				cleanup.set(true);
			}
		}
	}
	
	public class WorkerThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					processMessage(messageQueue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public class DequeueThread implements Runnable {
		private EventObject event;
		
		@Override
		public void run() {
			while(true) {
				try {
					event = eventQueue.poll();
					if (event == null) {
						if (pending > 0 && cleanup.getAndSet(false)) {
							mutator.execute();
							pending = 0;
						} else {
							Thread.yield();
						}
					} else {
						last_received = System.currentTimeMillis();
						ByteBuffer rowKey = se.toByteBuffer(event.getString(PARTITION_ON));	
						if (++count % batch_size == 0) {
							event.put(SENDING_TIME, System.currentTimeMillis());
							mutator.addInsertion(rowKey, keyspace, createColumn(event.getEventName(), event.toString(), se, se));
							mutator.execute();
							pending = 0;
						} else {
							mutator.addInsertion(rowKey, keyspace, createColumn(event.getEventName(), event.toString(), se, se));
							pending++;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * Instance vars
	 */
	private final Map<String,String> messageTypeMapping = new ConcurrentHashMap<String,String>();
	private final Set<String> doubleFields = new HashSet<String>();
	private final Set<String> longFields = new HashSet<String>();
	private final ArrayBlockingQueue<EventObject> eventQueue;
	private final ArrayBlockingQueue<MessageObject> messageQueue;
	private FixAttributeFactory factory;
	private Attribute timestampFieldAttribute = null;
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
	private long last_received;
	private int pending = 0;
	private AtomicBoolean cleanup = new AtomicBoolean(false);
	
	/*
	 * Configurable properties
	 */
	private String defaultFirm;
	private String darkStarNode;
	private int darkStarPort;
	private String clusterName;
	private int firmTag;
	private int timestampFieldTag;
	private String timestampFieldName;
	private String keyspace;
	private int batch_size;
	private volatile long count = 0;
	private String protocol;
	private int queueSize = 1000;
	private boolean useWorkerThread = false;
	
	/*
	 * Client interface vars
	 */
	private final Mutator<ByteBuffer> mutator;
	private final Cluster cluster;
	private final Keyspace keySpace;

	private final StringSerializer se = StringSerializer.get();
	private final ByteBufferSerializer bfs = ByteBufferSerializer.get();
	private final ConsistencyLevelPolicy policy = HFactory.createDefaultConsistencyLevelPolicy();
	
	/*
	 * Constants
	 */
	private final static String partition_key = "Symbol";	
	private final static String SENDING_TIME = "_onsndTS";
	private final static String RECEIVED_TIME = "_onrcvTS";
	private final static String FIRM = "Firm";
	private final static String EVENT_NAME = "event_name";
	private final static String PARTITION_ON = "partition_on";


	@SuppressWarnings("rawtypes")
	public AppiaPluginOnRamp(String plugin_name, SessionPluginFilter interest_filter, Map plugin_args) {
		super(plugin_name, interest_filter);
		setProperties(plugin_args);
		eventQueue = new ArrayBlockingQueue<EventObject>(queueSize);
		messageQueue = new ArrayBlockingQueue<MessageObject>(queueSize);
		CassandraHostConfigurator hostConfig = 
			new CassandraHostConfigurator(darkStarNode+":"+String.valueOf(darkStarPort));
		hostConfig.setPort(darkStarPort);
		hostConfig.setAutoDiscoverHosts(true);
		hostConfig.setAutoDiscoveryDelayInSeconds(1);
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keySpace = HFactory.createKeyspace(keyspace, cluster, policy);
		mutator = HFactory.createMutator(keySpace, bfs);
		startWorkerThread();
		startDequeueThread();
		startCleanupThread();
	}
	
	private void startCleanupThread() {
		scheduler.scheduleAtFixedRate(new CleanupThread(), 1, 1, TimeUnit.SECONDS);
	}
	
	private void startDequeueThread() {
		scheduler.submit(new DequeueThread());
	}
	
	private void startWorkerThread() {
		scheduler.submit(new WorkerThread());
	}

	@Override
	public void processOutboundMessagePostValidation(MessageObject message, CallContext context) {
		if (useWorkerThread) {
			try {
				messageQueue.put(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		} else {
			processMessage(message);
		}
	}

	@Override
	public void processInboundMessagePostValidation(MessageObject message, CallContext context) {
		if (useWorkerThread) {
			try {
				messageQueue.put(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		} else {
			processMessage(message);
		}
	}

	public void processMessage(MessageObject message) {
		String stream = messageTypeMapping.get(message.getMessageType().getCode());
		if (stream != null) {
			iterateMessage(message, stream);
		}
	}
	
	private void iterateMessage(MessageObject msg, String stream) {
		EventObject event = new EventObject();
		long time = System.currentTimeMillis();
		try {
			event.put(RECEIVED_TIME, time);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		boolean firmSet = false;
	    MessageData data = msg.getMessageData();
	    for (MessageData.EntryIterator it = data.iterator(); it.hasNext();) {
	    	MessageData.Entry entry = it.nextEntry();
	    	addField(event, entry.getAttribute().getName(), entry.getValue());
	    	if (entry.getAttribute().getId() == firmTag) {
	    		addField(event, FIRM, entry.getValue());
	    		firmSet = true;
	    	}
	    }
	    if (!firmSet) {
	    	addField(event, FIRM, defaultFirm);
	    }
		addField(event, EVENT_NAME, stream);
		if (event.has(partition_key)) {
			try {
				addField(event, PARTITION_ON, event.getString(partition_key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("No Symbol in " + event);
			System.err.println("Derived from " + msg);
		}
		send(event);
		// Add the plugin timestamp to the FIX message as well
		if (timestampFieldAttribute != null) {
			msg.getMessageData().setLong(timestampFieldAttribute, time);
		}
	}

	private void addField(EventObject event, String key, Object val) {
    	try {
    		if (doubleFields.contains(key)) {
    			val = Double.valueOf((String) val);
    		} else if (longFields.contains(key)) {
    			val = Long.valueOf((String) val);
    		}
    		event.put(key, val);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
		
	}

    @SuppressWarnings("rawtypes")
	public void setProperties(Map info) {
		try {
			darkStarNode = (String)info.get("darkStarNode");
		} catch (Exception e) {
			darkStarNode = "localhost";
		}
		try {
			darkStarPort = Integer.valueOf((String)info.get("darkStarPort"));
		} catch (Exception e) {
			darkStarPort = 9159;
		}
		try {
			clusterName = (String)info.get("clusterName");
		} catch (Exception e) {
			clusterName = "DarkStarCluster";
		}
		try {
		    defaultFirm = (String)info.get("firm");
		} catch (Exception e) {
			defaultFirm = "NYSE";
		}
		try {
		    timestampFieldName = (String)info.get("timestampFieldName");
		} catch (Exception e) {
			timestampFieldName = null;
		}
	    try {
	    	protocol = (String)info.get("protocol");
	    } catch (Exception e) {
	    	protocol = "FIX_42";
	    }
	    try {
	    	firmTag = Integer.valueOf((String)info.get("firmTag"));
	    } catch (Exception e) {
	    	firmTag = 0;
	    }
	    try {
	    	batch_size = Integer.valueOf((String)info.get("batchSize"));
	    } catch (Exception e) {
	    	batch_size = 1;
	    }
	    try {
	    	timestampFieldTag = Integer.valueOf((String)info.get("timestampFieldTag"));
	    } catch (Exception e) {
	    	timestampFieldTag = 0;
	    }
	    try {
	    	keyspace = (String)info.get("keyspace");
	    } catch (Exception e) {
	    	keyspace = "system";
	    }
	    if (info.containsKey("queueSize")) {
	    	try {
	    		queueSize = Integer.valueOf((String)info.get("queueSize"));
	    	} catch (Exception e) {
	    		queueSize = 1000;
	    	}
	    }
	    if ( (timestampFieldTag > 0) && (timestampFieldName != null) ) {
	    	factory = FixAttributeFactory.getInstance(protocol);
	    	factory.addAttributeString(timestampFieldTag, timestampFieldName);
	    	timestampFieldAttribute = factory.getAttributeById(timestampFieldTag);
	    }
	    if (info.containsKey("useWorkerThread")) {
	    	useWorkerThread = Boolean.getBoolean((String)info.get("useWorkerThread"));
	    }
	    count = 0;
	    createMapping(info);
    }
    
    @SuppressWarnings("rawtypes")
	private void createMapping(Map info) {
	    messageTypeMapping.put(FixConstants.ExecutionReport.getCode(),"FIXExecutionReport");
	    messageTypeMapping.put(FixConstants.NewOrderList.getCode(),"FIXNewOrderSingle");
	    messageTypeMapping.put(FixConstants.NewOrderSingle.getCode(),"FIXNewOrderSingle");
	    messageTypeMapping.put(FixConstants.Order.getCode(),"FIXNewOrderSingle");
	    messageTypeMapping.put(FixConstants.OrderCancelReplace.getCode(),"FIXNewOrderSingle");
	    messageTypeMapping.put(FixConstants.OrderCancelRequest.getCode(),"FIXNewOrderSingle");
	    messageTypeMapping.put(FixConstants.CrossOrder.getCode(),"FIXNewOrderSingle");
	    messageTypeMapping.put(FixConstants.CrossOrderCancelReplace.getCode(),"FIXNewOrderSingle");
	    messageTypeMapping.put(FixConstants.CrossOrderCancelRequest.getCode(),"FIXNewOrderSingle");
	    
	    doubleFields.add("AvgPx");
	    doubleFields.add("Price");
	    doubleFields.add("LastPx");
	    
	    longFields.add("LastShares");
	    longFields.add("CumQty");
	    longFields.add("Leaves");
	    longFields.add("OrderQty");
	    longFields.add("Side");
	}
	
    private void send(EventObject event) {
    	try {
			eventQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
    static CountDownLatch shutdownLatch = new CountDownLatch(1);
    
    public static void main(String[] args) {
    	long rate = Long.valueOf(args[0]);
    	long total = Long.valueOf(args[1]);
    	
    	Map<String,String> plugin_args = new HashMap<String,String>();
    	plugin_args.put("darkStarNode", "192.168.1.6,192.168.1.7,192.168.1.8");
    	plugin_args.put("darkStarPort", "9159");
    	plugin_args.put("clusterName", "DarkStarCluster");
    	plugin_args.put("firm", "NYSE");
    	plugin_args.put("firmTag", "20020");
    	plugin_args.put("timestampFieldName", "PluginTime");
    	plugin_args.put("timestampFieldTag", "20030");
    	plugin_args.put("protocol", "FIX");
    	plugin_args.put("batchSize", "1000");
    	plugin_args.put("keyspace", "system");
    	
    	SessionPluginFilter sessionFilter = new SessionPluginFilter();
		AppiaPluginOnRamp onramp = new AppiaPluginOnRamp("AppiaPluginOnRamp", sessionFilter, plugin_args);
		
		sendMessages(onramp, rate, total);
		try {
			shutdownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

	private static void sendMessages(AppiaPluginOnRamp onramp, long rate, long num) {
		
		String tickers_[] = new String[]{"IBM", "MSFT", "NSCP", "EMC", "EBAY", "YHOO",
                "CSCO", "AOL", "MO", "T", "INTL", "CELL", "PPRT",
                "VISX", "GE", "GM", "GTE", "HP", "DELL", "SUNW"};
		
		/*
		 *  Loop here creating Messages and calling the onramp's processInboundMessagePostValidation method
		 */
        String sender = "STUB";
        String target = "PLUG";

        //Create a FIX Order message.
        //Start by creating a new object
        MessageObject msg = new FixMessageObject();

        //Identify the message type of message
        msg.setMessageType(FixConstants.Order);

        //Identify the FIX protocol version
        msg.setProtocol(FixConstants.FIX_42);

        //Access the data map for the object
        MessageData data = msg.getMessageData();
        //Fill in the message fields (provided by the data map)
        data.setValue(FixConstants.SenderCompID, sender);              //FIX Tag 49
        data.setValue(FixConstants.TargetCompID, target);              //FIX Tag 56
        data.setValue(FixConstants.TargetSubID, "Simulator");          //FIX Tag 57
        data.setValue(FixConstants.OrdType, "2");                      //FIX Tag 40
        data.setValue(FixConstants.HandlInst, "2");                    //FIX Tag 21
        data.setInt(FixConstants.OrderQty, 5000);                      //FIX Tag 38
        data.setValue(FixConstants.Side, "1");                         //FIX Tag 54
        data.setDouble(FixConstants.Price, 110.5);                     //FIX Tag 44
        long startTime = System.currentTimeMillis();
        long baseTime = System.currentTimeMillis();
        int sent = 0;
        long totalSent = 0;
        while(totalSent < num) {
        	while ((System.currentTimeMillis() - baseTime) <= 1000) {
        		if ( (sent < rate) && (totalSent < num) ) {
		            data.setValue(FixConstants.ClOrdID, "order " + totalSent);                 
		            data.setValue(FixConstants.Symbol, tickers_[(int) (totalSent % tickers_.length)]); 
		            data.setValue(FixConstants.TransactTime, getTransactTime());   
		            onramp.processInboundMessagePostValidation(msg, null);
		            totalSent++;
		            sent++;
        		} else {
        			try {
        				Thread.sleep(1);
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
        	}
        	baseTime = System.currentTimeMillis();
        	sent = 0;
        }
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        double rate1 = (double)num/(double)elapsed;
        System.out.println("*******************************************************************************");
        System.out.println("Sent " + num + " orders in " + elapsed + " ms for a rate of " + (rate1 * 1000) + " messages/second");
        System.out.println("*******************************************************************************");
	}

	public static String getTransactTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
	
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(new Date(System.currentTimeMillis())).toString();
	}
	
}
