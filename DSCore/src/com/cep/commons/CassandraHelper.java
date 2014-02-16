package com.cep.commons;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

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

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.eaio.uuid.UUID;

public class CassandraHelper {

	public class CleanupThread implements Runnable {

		@Override
		public void run() {
			EventObject warning = new EventObject();
			try {
				warning.setEventName("CLEANUP");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (System.currentTimeMillis() - commit_time > 1000) {
				try {
					insertionQueue.put(warning);
				} catch (InterruptedException e) {
					logger.warn("Exception notifying CassandraHelper to perform cleanup", e);
				}
			}
		}

	}

	protected Mutator<ByteBuffer> mutator;
	final protected ReentrantLock mutatorLock = new ReentrantLock();
	protected StringSerializer se = StringSerializer.get();
	protected LongSerializer ls = LongSerializer.get();
	protected ByteBufferSerializer bfs = ByteBufferSerializer.get();
	protected BytesArraySerializer bas = BytesArraySerializer.get();
	protected static int batch_size;
	protected static int queue_size = 5000;
	
	protected final static ConsistencyLevelPolicy mcl = HFactory.createDefaultConsistencyLevelPolicy();
	protected static Cluster cluster = null;
	protected static Keyspace ko;
	protected static CassandraHostConfigurator hostConfig;
	protected AtomicLong pending = new AtomicLong();
	protected long commit_time = System.currentTimeMillis();
	protected AtomicBoolean cleanup = new AtomicBoolean();
	protected long total = 0;
	protected ArrayBlockingQueue<EventObject> insertionQueue;
	
	static Logger logger = Logger.getLogger("com.cep.darkstar.node.CassandraHelper");
	
	protected final ScheduledExecutorService scheduler;
	protected final ExecutionThread executionThread = new ExecutionThread();
	protected final CleanupThread cleanupThread = new CleanupThread();
	protected static final String GUID = "guid";
	
	private static Set<String> doubleFields = new HashSet<String>();
	private static Set<String> longFields = new HashSet<String>();
	
	public CassandraHelper() {
		mutator = HFactory.createMutator(CassandraHelper.ko, bfs);	
		scheduler = Executors.newScheduledThreadPool(2);
		insertionQueue = new ArrayBlockingQueue<EventObject>(queue_size);
		scheduleInsertionThread();
		scheduleCleanupThread();
	}

	private void scheduleCleanupThread() {
		scheduler.scheduleAtFixedRate(cleanupThread, 1, 1, TimeUnit.SECONDS);
	}

	public static void init(Cluster cluster, Keyspace ko, int batch_size, int queue_size) {
		logger.info("Initializing Cassandra Helper");
		CassandraHelper.cluster = cluster;
		CassandraHelper.ko = ko;
		CassandraHelper.batch_size = batch_size;
		CassandraHelper.queue_size = queue_size;
		logger.info("Cassandra Helper batch size set to " + CassandraHelper.batch_size);
	}
	
	public ArrayBlockingQueue<EventObject> getQueue() {
		return insertionQueue;
	}
	
	public static Set<String> getDoubleFields() {
		return doubleFields;
	}

	public static void setDoubleFields(Set<String> doubleFields) {
		CassandraHelper.doubleFields = doubleFields;
	}

	public static Set<String> getLongFields() {
		return longFields;
	}

	public static void setLongFields(Set<String> longFields) {
		CassandraHelper.longFields = longFields;
	}

	private void scheduleInsertionThread() {
		scheduler.schedule(executionThread, 1, TimeUnit.SECONDS);
	}

	public void execute() {
		if (logger.isDebugEnabled()) {
			logger.debug("CassandraHelper execute method called with " + pending + " mutations pending");
		}
		if(++total % batch_size == 0) {
			cleanup.set(true);
		}
		pending.incrementAndGet();
	}
	
	private static boolean isDoubleField(String field) {
		return doubleFields.contains(field);
	}
	
	private static boolean isLongField(String field) {
		return longFields.contains(field);
	}
	
	public class ExecutionThread implements Runnable {
		@Override
		public void run() {
			while(true) {
				try {
					EventObject event = insertionQueue.take();
					if (event.getEventName().equals("CLEANUP")) {
						mutator.execute();
						commit_time = System.currentTimeMillis();
					} else {
						String uuid = (new UUID()).toString();
						if (event.has(GUID)) {
							try {
								uuid = event.getString(GUID);
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
						ByteBuffer rowKey = se.toByteBuffer(uuid);
						String columnFamily = "events";
						if (event.has("columnFamily")) {
							try {
								columnFamily = event.getString("columnFamily");
							} catch (JSONException e1) {
								logger.error(e1.getMessage(), e1);
							}
						}
						Iterator<?> iterator = event.keys();
						String key;
						while(iterator.hasNext()) {
							key = iterator.next().toString();
							Object value;
							try {
								value = event.get(key);
								if (value != null) {
									if (isDoubleField(key)) {
										handleDouble(key, value, columnFamily, rowKey);
									} else if (isLongField(key)) {
										handleLong(key, value, columnFamily, rowKey);
									} else {
										handleDefault(key, value, columnFamily, rowKey);
									}
								}
							} catch (JSONException e) {
								logger.error(e.getMessage(), e);
							}
						}
						if(++total % batch_size == 0) {
							mutator.execute();
							commit_time = System.currentTimeMillis();
						}
					}
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				} catch (JSONException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		

		private void handleDefault(String key, Object value, String CF, ByteBuffer rowKey) {
			try {
				if (value.getClass().equals(Long.class)) {
					mutator.addInsertion(rowKey, CF, createColumn(key, LongSerializer.get().toBytes((Long) value), se, bas));
				} else if (value.getClass().equals(String.class)) {
					mutator.addInsertion(rowKey, CF, createColumn(key, se.toByteBuffer((String) value),se, bfs));
				} if (value.getClass().equals(Double.class)) {
					double d = ((Double) value).doubleValue() * 100000;
					mutator.addInsertion(rowKey, CF, createColumn(key, ls.toBytes(Math.round(d)), se, bas));
				} else if (value.getClass().equals(Integer.class)) {
					mutator.addInsertion(rowKey, CF, createColumn(key, ls.toBytes(((Integer) value).longValue()), se, bas));						
				} else {
					mutator.addInsertion(rowKey, CF, createColumn(key, se.toByteBuffer(value.toString()),se, bfs));
				}
			} catch (ClassCastException e) {
				logger.error("Exception caught handling field " + key + " with value " + value.toString(), e);
			}		
		}

		private void handleLong(String key, Object value, String CF, ByteBuffer rowKey) {
			try {
				long lng = 0;
				if (value.getClass().equals(Long.class)) {
					lng = ((Long)value).longValue();
				} else if (value.getClass().equals(String.class)) {
					lng = Long.valueOf((String)value);
				}  else if (value.getClass().equals(Integer.class)) {
					lng = ((Integer)value).longValue();						
				} else if (value.getClass().equals(Double.class)) {
					lng = ((Double)value).longValue();
				}
				mutator.addInsertion(rowKey, CF, createColumn(key, ls.toBytes((long)lng), se, bas));
			} catch (ClassCastException e) {
				logger.error("Exception caught handling double field " + key + " with value " + value.toString(), e);
			}	
		} 
		
		private void handleDouble(String key, Object value, String CF, ByteBuffer rowKey) {
			try {
				double dbl = 0.0;
				if (value.getClass().equals(Long.class)) {
					dbl = ((Long)value).doubleValue();
				} else if (value.getClass().equals(String.class)) {
					dbl = Double.valueOf((String)value);
				} else if (value.getClass().equals(Integer.class)) {
					dbl = ((Integer)value).doubleValue();						
				} else if (value.getClass().equals(Double.class)) {
					dbl = ((Double)value).doubleValue();
				}
				dbl = (dbl * 100000);
				mutator.addInsertion(rowKey, CF, createColumn(key, ls.toBytes((long)dbl), se, bas));
			} catch (ClassCastException e) {
				logger.error("Exception caught handling double field " + key + " with value " + value.toString(), e);
			}
		}

	}

}
