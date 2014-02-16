package com.cep.darkstar.dto;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.CassandraHelper;
import com.cep.commons.EventObject;
import com.cep.darkstar.query.ICEPEngine;

public abstract class AbstractDTO implements IDTO {
	protected CassandraHelper cassandraHelper = new CassandraHelper();
	protected LongSerializer ls = LongSerializer.get();
	protected StringSerializer ss = StringSerializer.get();
	protected BytesArraySerializer bas = BytesArraySerializer.get();
	protected ByteBufferSerializer bbs = ByteBufferSerializer.get();
	protected ArrayBlockingQueue<EventObject> queue = new ArrayBlockingQueue<EventObject>(1000);
	protected ICEPEngine engine;
	static Logger logger = Logger.getLogger("com.cep.darkstar.DTO.AbstractDTO");
	protected boolean external_timing = true;
	
	public static final String PERSISTED = "persisted";
	public static final String _DTO_RECEIVED = "_dtoReceived";
	public static final String _TRANSACT_TIME = "TransactTime";
	public static final String _DSTIMESTAMP = "ds_timestamp";
	
	public abstract void doFunction(EventObject e);
	
	public void setCallback(ICEPEngine callback) {
		engine = callback;
	}
	
	public void setBatchMode() {
		external_timing = true;
	}
	
	protected void save(EventObject e) {
		try {
			timestamp(e);
			cassandraHelper.getQueue().put(e);
		} catch (InterruptedException e1) {
			logger.error("Exception putting message on queue to Cassandra: " + e1.getMessage(), e1);
		}
	}
	
	protected void send(EventObject e) {
		try {
			engine.send(e);
		} catch (Exception e2) {
			logger.error("Exception sending message into the event engine: " + e2.getMessage(), e2);
		}
	}
	
	
	// have to account for batch operations here
	// if real time, use current system time, if not, use TransactTime
	protected void timestamp(EventObject e) {
		try {
			if (external_timing) {
				e.put(_DTO_RECEIVED, e.get(_TRANSACT_TIME));
				e.put(_DSTIMESTAMP, e.get(_TRANSACT_TIME));

			} else {
				e.put(_DTO_RECEIVED, System.currentTimeMillis());
			}	
		} catch (JSONException e3) {
			logger.warn("Exception timestamping message: " + e3.getMessage(), e3);
		}
	}
	
	protected EventObject getNext() throws InterruptedException {
		return queue.take();
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

	@Override
	public abstract void run();

	@Override
	public BlockingQueue<EventObject> getQueue() {
		return queue;
	}
}
