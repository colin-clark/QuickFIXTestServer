package com.cep.darkstar.utility.cassandra;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.nio.ByteBuffer;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.configuration.DefaultConsistencyLevel;

public class SendToDarkStar implements MessageReaderCallback {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.utility.cassandra.SendToDarkStar");
	
	private String eventName;
	
	/*
	 * DarkStar Client api classes
	 */
	private StringSerializer se = StringSerializer.get();
	private ByteBufferSerializer bfs = ByteBufferSerializer.get();
	private final ConsistencyLevelPolicy policy = new DefaultConsistencyLevel();

	private Mutator<ByteBuffer> mutator;
	private Cluster cluster;
	private Keyspace keySpace;
	private String darkStarNode;
	private int darkStarPort;
	private String clusterName;
	private String keyspace;

	public SendToDarkStar(CassandraClientConfigInfo info) {
		// Set configurable properties
		darkStarNode = info.getDarkStarNode();
		darkStarPort = info.getDarkStarPort();
		clusterName = info.getClusterName();
		keyspace = info.getKeyspace();
		eventName = info.getEventName();

		// Initialize the connection to DarkStar
		logger.info("initializing cluster connection");
		
		CassandraHostConfigurator hostConfig = 
			new CassandraHostConfigurator(darkStarNode+":"+String.valueOf(darkStarPort));
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keySpace = HFactory.createKeyspace(keyspace, cluster, policy);
		mutator = HFactory.createMutator(keySpace, bfs);
		
		logger.info("cluster connection initialized");
	}
	
	@Override
	public void send(EventObject event) throws JSONException {
		event.setEventName(eventName);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending: " + event);
		}
		ByteBuffer rowKey = se.toByteBuffer(event.getString("partition_on"));		
		mutator.addInsertion(rowKey, keyspace, createColumn(event.getEventName(), event.toString(), se, se));
		mutator.execute();
	}

	@Override
	public void finished() {
		logger.info("Cassandra Message Replay Complete");
	}

}
