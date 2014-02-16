package com.cep.darkstar.node;
import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

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
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.eaio.uuid.UUID;

public class PersistEvent implements Runnable {
	static Logger dsLog = Logger.getLogger("com.cep.darkstar.node.PersistEvent");
	static final protected int DEFAULT_BATCH_SIZE = 500;

	private final BlockingQueue<EventObject> queue;
	protected final Mutator<ByteBuffer> mutator;
	protected int batch_size;
	protected boolean external_timing = true;

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

	final ConsistencyLevelPolicy mcl = new MyConsistencyLevel();
	Cluster cluster = null;
	Keyspace ko;
	CassandraHostConfigurator hostConfig;

	PersistEvent(BlockingQueue<EventObject> queue, Keyspace ko, boolean external_timing) {
		this(queue, ko, DEFAULT_BATCH_SIZE, external_timing);
	}

	public PersistEvent(BlockingQueue<EventObject> queue, Keyspace ko, int batch_size, boolean external_timing) {
		this.queue = queue;
		this.ko = ko;
		this.mutator = HFactory.createMutator(this.ko, bfs);
		this.batch_size = batch_size;
		dsLog.info("PersistEvent loaded with batch size="+batch_size);
		this.external_timing = external_timing;
		dsLog.info("PersistEvent loaded with external timing="+external_timing);
	}

	// do the actual insertions
	private void insert(EventObject event) {
		try {
			// stuff batch mutation with insertions
			rowKey = se.toByteBuffer((new UUID()).toString());
			// if external timing, find it in TransactTime
			if (external_timing) {
				mutator.addInsertion(rowKey,"events", HFactory.createColumn("ds_timestamp", ls.toBytes(event.getLong("TransactTime")), se, bas));
				dsLog.info("Using external transact time - after call.");
			} else {
				mutator.addInsertion(rowKey,"events", HFactory.createColumn("ds_timestamp", ls.toBytes(System.currentTimeMillis()), se, bas));
				dsLog.info("Using arrival transact time - after call.");
			}
			// there's got to be a faster way of doing this
			iterator = event.keys();
			while(iterator.hasNext()) {
				key = iterator.next().toString();
				Object value = event.get(key);
				if (value.getClass().equals(Long.class)) {
					mutator.addInsertion(rowKey,"events", createColumn(key, ls.toBytes((Long) value), se, bas));
				} else if (value.getClass().equals(String.class)) {
					mutator.addInsertion(rowKey,"events", createColumn(key, se.toByteBuffer((String) value),se, bfs));
				} if (value.getClass().equals(Double.class)) {
					Double val = (((Double) value) * 100000);
					mutator.addInsertion(rowKey,"events", createColumn(key, ls.toBytes(val.longValue()), se, bas));
				} else if (value.getClass().equals(Integer.class)) {
					mutator.addInsertion(rowKey,"events", createColumn(key, ls.toBytes(((Integer) value).longValue()), se, bas));						
				} else {
					mutator.addInsertion(rowKey,"events", createColumn(key, se.toByteBuffer(event.get(key).toString()),se, bfs));
				}
			}
			if(++commit%batch_size==0) {
				try {
					mr = mutator.execute();
					mutator.discardPendingMutations();
					dsLog.info(Thread.currentThread().getName()+" committing on "+mr.getHostUsed());
				} catch (Exception ex){
					dsLog.error("Problem executing insert:"+ex.getLocalizedMessage(), ex);
				}
			}
		} catch (HectorException e) {
			dsLog.error(e.getMessage(), e);
		} catch (JSONException e) {
			dsLog.error(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		try{
			while(true) 
				insert(queue.take());
		} catch (InterruptedException e) {
			// never, ever, ever swallow this exception if possible
			Thread.currentThread().interrupt();
		}
	}	
}

// custom consistency level
class MyConsistencyLevel implements ConsistencyLevelPolicy {
	@Override
	public HConsistencyLevel get(OperationType op, String arg1) {
		switch(op) {
		case READ: return HConsistencyLevel.ONE;
		case WRITE: return HConsistencyLevel.ANY;
		}
		return HConsistencyLevel.ONE;
	}

	@Override
	public HConsistencyLevel get(OperationType op) {
		switch(op) {
		case READ: return HConsistencyLevel.ONE;
		case WRITE: return HConsistencyLevel.ANY;
		}
		return HConsistencyLevel.ONE;
	}
}

