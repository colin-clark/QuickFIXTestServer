package com.cep.quickfix.client.impls;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.nio.ByteBuffer;

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

import com.cep.commons.EventObject;
import com.cep.quickfix.client.Main;
import com.cep.quickfix.client.interfaces.MessageProcessor;

public class FIXMsgsToRabbit implements MessageProcessor {
	// Must be format 192.168.0.14:9159
	protected String darkStarNode;
	protected String clusterName;
	protected String eventName;
	
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
	
	private synchronized void init() {
		darkStarNode = Main.getHostname();
		clusterName = Main.getClustername();
		hostConfig = new CassandraHostConfigurator(darkStarNode);
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keyspace = HFactory.createKeyspace("system", cluster, policy);
		mutator = HFactory.createMutator(keyspace, bfs);
	}
	
	@Override
	public void onMessage(Message message, SessionID sessionID) {
		try {
			if (mutator == null) {
				init();
			}
			EventObject anEvent = new EventObject();	
			// we want the symbol, shares, side, etc.
			anEvent.put("SenderCompID", sessionID.getSenderCompID());
			anEvent.put("SenderSubID", sessionID.getSenderSubID());
			anEvent.put("TargetCompID", sessionID.getTargetCompID());
			anEvent.put("TargetSubID", sessionID.getTargetSubID());
			// defaulting this to EXECUTION REPORT
			anEvent.put("MsgType", "8");
			//-
			anEvent.put("AvgPx", message.getDouble(6));
			anEvent.put("ClOrdID", message.getInt(11));
			anEvent.put("CumQty", message.getInt(14));
        	anEvent.put("ExecTransType", message.getInt(20));
			anEvent.put("ExecID", message.getInt(17));
			anEvent.put("Symbol", message.getString(55));
			anEvent.put("OrderID", message.getInt(37));
			anEvent.put("ExecType", message.getInt(150));
			anEvent.put("LeavesQty", message.getInt(151));
            if (message.getInt(14)==0) {
            	anEvent.setEventName("FIXOrderAck");
            } else {
            	anEvent.setEventName("FIXExecutionReport");
        		anEvent.put("LastPx", message.getDouble(31));
    			anEvent.put("LastShares", message.getInt(32));
    			anEvent.put("OrderQty", message.getInt(38));
			}
            anEvent.put("partition_on", message.getString(55));
			send(anEvent);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (FieldNotFound e) {
			e.printStackTrace();
		}
	}
	
    protected void send(EventObject event) throws JSONException {
		ByteBuffer rowKey = se.toByteBuffer(event.getString("partition_on"));		
		event.put("_ds_timestamp", System.currentTimeMillis());
		mutator.addInsertion(rowKey, "system", createColumn(event.getEventName(), event.toString(), se, se));
		System.out.println(event.toString());
		mutator.execute();
	}
}

