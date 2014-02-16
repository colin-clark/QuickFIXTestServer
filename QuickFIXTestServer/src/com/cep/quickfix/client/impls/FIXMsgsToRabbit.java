package com.cep.quickfix.client.impls;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.json.JSONException;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.TransactTime;

import com.cep.commons.EventObject;
import com.cep.quickfix.client.interfaces.MessageProcessor;
import com.cep.quickfix.client.util.DefaultConsistencyLevel;

public class FIXMsgsToRabbit implements MessageProcessor {
	// Must be format 192.168.0.14:9159
	protected final String darkStarNode;
	protected final String clusterName;
	protected String eventName;
	protected final String firm;
	protected long batch_size = 1000;
	
	// Client interface vars
	protected Mutator<ByteBuffer> mutator;
	protected Cluster cluster;
	protected Keyspace keyspace;
	protected CassandraHostConfigurator hostConfig;

	protected StringSerializer se = StringSerializer.get();
	protected LongSerializer ls = LongSerializer.get();
	protected ByteBufferSerializer bfs = ByteBufferSerializer.get();
	protected BytesArraySerializer bas = BytesArraySerializer.get();
	protected final ConsistencyLevelPolicy policy = new DefaultConsistencyLevel();
	protected static SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

	
	protected long count = 0;
	
	public FIXMsgsToRabbit(String darkStarNode, String clusterName, String firm, long batch_size) {
		this.darkStarNode = darkStarNode;
		this.clusterName = clusterName;
		this.firm = firm;
		this.batch_size = batch_size;
		init();
	}
	
	public FIXMsgsToRabbit(String darkStarNode, String clusterName, String firm) {
		this.darkStarNode = darkStarNode;
		this.clusterName = clusterName;
		this.firm = firm;
		init();
	}

	private synchronized void init() {
		hostConfig = new CassandraHostConfigurator(darkStarNode);
		hostConfig.setAutoDiscoveryDelayInSeconds(1);
		hostConfig.setAutoDiscoverHosts(true);
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keyspace = HFactory.createKeyspace("system", cluster, policy);
		mutator = HFactory.createMutator(keyspace, bfs);
	}
	
	@Override
	public void onMessage(Message message, SessionID sessionID) {
		try {
			System.out.println("New Message Received: \n" + message);
			EventObject anEvent = new EventObject();	
			// we want the symbol, shares, side, etc.
			anEvent.put("SenderCompID", sessionID.getSenderCompID());
			anEvent.put("TargetCompID", sessionID.getTargetCompID());
			anEvent.put("Firm", firm);
			// defaulting this to EXECUTION REPORT
			anEvent.put("MsgType", "8");
			//-
			anEvent.put("AvgPx", message.getDouble(6));
			anEvent.put("ClOrdID", String.valueOf(message.getInt(11)));
			anEvent.put("CumQty", message.getInt(14));
        	anEvent.put("ExecTransType", String.valueOf(message.getInt(20)));
			anEvent.put("ExecID", String.valueOf(message.getInt(17)));
			anEvent.put("Symbol", message.getString(55));
			anEvent.put("OrderID", String.valueOf(message.getInt(37)));
			anEvent.put("ExecType", String.valueOf(message.getInt(150)));
			anEvent.put("LeavesQty", message.getInt(151));
			// Adding OrdStatus, OrdType, Price, & Side to Execution Reports
			if (message.isSetField(Side.FIELD)){
				anEvent.put("Side", message.getInt(Side.FIELD));
			}
			if (message.isSetField(Price.FIELD)) {
				anEvent.put("Price", message.getDouble(Price.FIELD));
			}
			if (message.isSetField(OrdType.FIELD)) {
				anEvent.put("OrdType", String.valueOf(message.getChar(OrdType.FIELD)));
			}
			if (message.isSetField(OrdStatus.FIELD)) {
				anEvent.put("OrdStatus", String.valueOf(message.getChar(OrdStatus.FIELD)));
			}
			Date date;
			if (message.isSetField(TransactTime.FIELD)){
				date = message.getUtcTimeStamp(TransactTime.FIELD);
			} else {
				date = new Date();
			}
			String dateStr = shortFormat.format(date);
			anEvent.put("TransactTime", dateStr);
            if (message.getInt(14)==0) {
            	anEvent.setEventName("FIXOrderAck");
            } else {
            	anEvent.setEventName("FIXExecutionReport");
        		anEvent.put("LastPx", message.getDouble(31));
    			anEvent.put("LastShares", message.getInt(32));
    			anEvent.put("OrderQty", message.getInt(38));
			}
            anEvent.put("partition_on", message.getString(55));
            System.out.println("Sending message to DarkStar");
			send(anEvent);
			System.out.println("Message sent to DarkStar");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FieldNotFound e) {
			e.printStackTrace();
		}
	}
	
    protected void send(EventObject event) throws JSONException {
    	boolean doExecute = (++count % batch_size == 0); 
		ByteBuffer rowKey = se.toByteBuffer(event.getString("partition_on"));
		if (doExecute) {
			event.put("_ds_timestamp", System.currentTimeMillis());
		}
		mutator.addInsertion(rowKey, "system", createColumn(event.getEventName(), event.toString(), se, se));
		if  (doExecute){
			mutator.execute();
		}
	}
}

