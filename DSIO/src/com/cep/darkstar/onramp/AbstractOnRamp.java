package com.cep.darkstar.onramp;

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

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.configuration.BaseConfig;
import com.cep.darkstar.onramp.configuration.DefaultConsistencyLevel;

public class AbstractOnRamp implements IOnRamp {
	// Configurable properties
	protected String darkStarNode;
	protected int darkStarPort;
	protected String clusterName;
	protected String keyspace;
	protected String eventName;
	
	// Client interface vars
	protected Mutator<ByteBuffer> mutator;
	protected Cluster cluster;
	protected Keyspace keySpace;

	protected StringSerializer se = StringSerializer.get();
	protected LongSerializer ls = LongSerializer.get();
	protected ByteBufferSerializer bfs = ByteBufferSerializer.get();
	protected BytesArraySerializer bas = BytesArraySerializer.get();
	protected final ConsistencyLevelPolicy policy = new DefaultConsistencyLevel();
	
	protected final static String timestamp_field = "_onRampSent";
	
	protected void setProperties(BaseConfig info) {
		darkStarNode = info.getDarkStarNode();
		darkStarPort = info.getDarkStarPort();
		clusterName = info.getClusterName();
		keyspace = info.getKeyspace();
		eventName = info.getEventName();
	}
	
	public void setProperties(String darkStarNode, int darkStarPort, String clusterName, String keyspace, String eventName) { 
		this.darkStarNode = darkStarNode;
		this.darkStarPort = darkStarPort;
		this.clusterName = clusterName;
		this.keyspace = keyspace;
		this.eventName = eventName;
	}
	
	public void init() {
		CassandraHostConfigurator hostConfig = 
			new CassandraHostConfigurator(darkStarNode+":"+String.valueOf(darkStarPort));
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keySpace = HFactory.createKeyspace(keyspace, cluster, policy);
		mutator = HFactory.createMutator(keySpace, bfs);
	}
		
	public void send(EventObject event, String key) throws JSONException {
		ByteBuffer rowKey = se.toByteBuffer(key);
		event.put(timestamp_field, System.currentTimeMillis());
		mutator.addInsertion(rowKey, "system", createColumn(eventName, event.toString(), se, se));
		mutator.execute();
	}
	
    public void send(EventObject event) throws JSONException {
		ByteBuffer rowKey = se.toByteBuffer(event.getString("partition_on"));		
		event.put(timestamp_field, System.currentTimeMillis());
		mutator.addInsertion(rowKey, keyspace, createColumn(event.getEventName(), event.toString(), se, se));
		mutator.execute();
	}
}
