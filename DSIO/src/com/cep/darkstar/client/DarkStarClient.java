/**
 * Colin Clark
 * 
 */
package com.cep.darkstar.client;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.client.configuration.ClientConfigInfo;
import com.cep.darkstar.client.configuration.ClientConfiguration;
import com.cep.darkstar.pubsub.sub.SubscribeTopic;
import com.rabbitmq.client.ShutdownSignalException;

public class DarkStarClient {
	private static final Logger logger = Logger.getLogger(com.cep.darkstar.client.DarkStarClient.class);
	static final String[] firms = {"ABC","XYZ","MOM","POP","HAT","RAT"};
	static int index = 0;
	
	// Properties
	private String log4j_file;
	private String darkStarNode;
	private int darkStarPort;
	private String hostName;
	private String firm;
	private int portNumber;
	private String topicPattern;
	private String clusterName;
	private String keyspace;
	private int number_of_publishers;
	private int batch_size;
	
	// Hector stuff
	private final ConsistencyLevelPolicy mcl = new MyConsistencyLevel();
	private Cluster cluster;
	private Keyspace ko;
	
	public DarkStarClient() {
	}
	
	public void run(String configFile) throws Exception {
		setProperties(configFile);
		
		// initialize log4j
		PropertyConfigurator.configure(log4j_file);
		logger.info("All properties set for DarkStarClient");
		
		try {
			// shared queue that we'll pump to and distribute events to insertion threads
			BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(1000);
	
			// initialize cassandra and pass keyspace around
			CassandraHostConfigurator hostConfig = 
				new CassandraHostConfigurator(darkStarNode+":"+String.valueOf(darkStarPort));
			
			//hostConfig.setAutoDiscoverHosts(true);
			cluster = HFactory.createCluster(clusterName, hostConfig);
			ko = HFactory.createKeyspace(keyspace, cluster, mcl);
	
			final Executor toCassandra = Executors.newFixedThreadPool(number_of_publishers);
			for (int i = 0; i < number_of_publishers; i++) {
				toCassandra.execute(new Publish(queue, ko, firm, batch_size));
			}
			
			final Executor fromRabbit = Executors.newFixedThreadPool(1);
			fromRabbit.execute(new Subscribe(queue, hostName, portNumber, topicPattern));
		} catch (Exception ex) {
			logger.error("Fatal Exception " + ex.getMessage(), ex);
			cluster.getConnectionManager().shutdown();
			System.exit(1);
		}
	}

	private void setProperties(String configFile) throws Exception {
		ClientConfigInfo info = null;
		info = ClientConfiguration.getConfiguration(configFile);
		System.out.println("Using yaml config file " + configFile);

		log4j_file = info.getLog4j_file();
		darkStarNode = info.getDarkStarNode();
		darkStarPort = info.getDarkStarPort();
		hostName = info.getHostName();
		firm = info.getFirm();
		portNumber = info.getPortNumber();
		topicPattern = info.getTopicPattern();
		clusterName = info.getClusterName();
		keyspace = info.getKeyspace();
		number_of_publishers = info.getNumber_of_publishers();
		batch_size = info.getBatch_size();
		topicPattern = info.getTopicPattern();
	}
	
	public static void main(String[] args) {
		try {
			String filename = args[0];
			if (new File(filename).exists()) {
				new DarkStarClient().run(filename);
			} else {
				System.err.println("File " + filename + " does not exist, a valid yaml " +
						"configuration file must be specified.");
			}
		} catch (Exception e) {
			System.err.println("Fatal Configuration Exception " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	/*
	 * ---------------------------------------------------------------------------------------------
	 * Nested Classes: Publish, Subscribe, and MyConsistencyLevel                                   *
	 * ---------------------------------------------------------------------------------------------
	 */
	
	public static class Publish implements Runnable {
		private final BlockingQueue<byte[]> queue;
		protected final Mutator<ByteBuffer> mutator;
		//protected final String firm;
		protected int batch_size;

		protected static StringSerializer se = StringSerializer.get();
		protected static LongSerializer ls = LongSerializer.get();
		protected static ByteBufferSerializer bfs = ByteBufferSerializer.get();
		protected static BytesArraySerializer bas = BytesArraySerializer.get();

		protected static long commit = 0;
		protected EventObject event;
		protected ByteBuffer rowKey;
		protected MutationResult mr;
		protected String key;
		protected Iterator<?> iterator;
		
		// give me the queue and keyspace please
		Publish(BlockingQueue<byte[]> queue, Keyspace ko, String firm, int batch_size) {
			this.queue = queue;
			this.mutator = HFactory.createMutator(ko, bfs);
			//this.firm = firm;
			this.batch_size = batch_size;
		}

		// do the actual insertions
		private void insert(byte[] msg) {
			try {
				// Batch mutation with insertions
				// rowkey = partition on
				// column family = event type
				// byteBuffer = EventObject.toString
				
				//System.out.println(msg);
				
				// so let's start with the object
				event = new EventObject(new String(msg));

				// we'll use the firm field
				/*
				 * This is a mistake.  If you put "firm" in the partition_on field every message will be sent
				 * to whichever node is handling the token range which the word "firm" falls into.  We don't
				 * want to partition on the word "firm", we want to partition on the contents of the field.
				 */
				String firm = firms[index++ % firms.length];
				rowKey = se.toByteBuffer(firm);
				event.put("partition_on", firm);
				
				// we want a local timestamp
				event.put("_ds_timestamp", System.currentTimeMillis());
				// convert the event object to a bytebuffer
				mutator.addInsertion(rowKey, "system", createColumn(event.getEventName(), event.toString(), se, se));
				// we'll experiment with batching and see what there is to see
				if(++commit%batch_size==0) {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("Commiting message batch");
						}
						mr = mutator.execute();
						mutator.discardPendingMutations();
						if (logger.isDebugEnabled()) {
							logger.debug(Thread.currentThread().getName() + " committing on " + mr.getHostUsed());
						}
					} catch (Exception ex){
						logger.error("Problem executing insert: " + ex.getLocalizedMessage(), ex);
					}
				}
				
			} catch (HectorException e) {
				logger.error("HectorException: " + e.getMessage(), e);
			} catch (JSONException e) {
				logger.error("JSONException: " + e.getMessage(), e);
			}
		}

		// and handle the insertions
		public void run() {
			try{
				while(true) {
					insert(queue.take());
				}
			} catch (InterruptedException e) {
				// never, ever, ever swallow this exception if possible
				Thread.currentThread().interrupt();
			}
		}
	}

	static class Subscribe implements Runnable {
		// set everything up
		long end = 0;
		long begin = 0;
		int i = 0;
		EventObject event;

		// shared queue
		private final BlockingQueue<byte[]> queue;

		SubscribeTopic listen;

		// give me the queue 
		Subscribe(BlockingQueue<byte[]> q, String hostName, int portNumber, String topicPattern) {
			queue = q;
			try {
				logger.info("Subscribing to " + hostName + ":" + portNumber);
				listen = new SubscribeTopic.Builder().hostName(hostName).portNumber(portNumber).build();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		public void run() {
			// subscribe to bus for messages
			try {
				begin = System.currentTimeMillis();
				while (true) {
				
					queue.put(listen.nextDelivery());
					
					if (logger.isTraceEnabled()) {
						if ((++i%10000)==0) {
							end = System.currentTimeMillis();
							logger.trace("10,000 messages sent to thread pool in " + 
									Double.toString((end-begin)/1000.0)+" seconds");
							begin = end;
						}
					}
					
				}
			} catch (ShutdownSignalException e) {
				logger.error("ShutdownSignalException: " + e.getMessage(), e);
			} catch (IOException e) {
				logger.error("IOException: " + e.getMessage(), e);
			} catch (InterruptedException e) {
				// don't swallow this exception
				Thread.currentThread().interrupt();
			}
		}
	}

	// we should really make this a part of our 
	// client library
	private static class MyConsistencyLevel implements ConsistencyLevelPolicy {
		@Override
		public HConsistencyLevel get(OperationType op, String arg1) {
			switch(op) {
				case READ: return HConsistencyLevel.ONE;
				case WRITE: return HConsistencyLevel.ANY;
			case META_READ:
				break;
			case META_WRITE:
				break;
			default:
				break;
			}
			return HConsistencyLevel.ONE;
		}

		@Override
		public HConsistencyLevel get(OperationType op) {
			switch(op) {
				case READ: return HConsistencyLevel.ONE;
				case WRITE: return HConsistencyLevel.ANY;
			case META_READ:
				break;
			case META_WRITE:
				break;
			default:
				break;
			}
			return HConsistencyLevel.ONE;
		}
	}
}