package com.cep.darkstar.onramp.namedentities;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.configuration.DefaultConsistencyLevel;
import com.cep.darkstar.onramp.configuration.djnews.ClientConfigInfo;
import com.cep.darkstar.onramp.configuration.djnews.ClientConfiguration;

public class NamedEntitiesTest {

	final static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.namedentities.NamedEntitiesTest");
	
	/*
	 * Offset into alphabet array for partition_on field
	 */
	private int offset = 0;
	
	/*
	 * Parameters which are configurable through yaml
	 */
	private String log4j_file;
	private String darkStarNode;
	private int darkStarPort;
	private String clusterName;
	private String keyspace;
	private int delay;
	
	/*
	 * DarkStar Client api classes
	 */
	private Mutator<ByteBuffer> mutator;
	private StringSerializer se = StringSerializer.get();
	private ByteBufferSerializer bfs = ByteBufferSerializer.get();
	private Cluster cluster;
	private Keyspace keySpace;
	private final ConsistencyLevelPolicy policy = new DefaultConsistencyLevel();

	public void processMessages() throws IOException {
		while (true) {
			sendMessage();
		}
	}
	
	private void sendMessage() {
		EventObject message = new EventObject();
		try {
			message.put("type", "symbol");
			message.put("value", "CEP");
			message.put("guid", UUID.randomUUID().toString());
			message.setEventName("named_entities");
			message.put("partition_on", randomLetter());
			if (logger.isDebugEnabled()) {
				logger.debug("Sending Event: " + message.toString());
			}
			send(message);
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
    private String randomLetter() {
    	String[] alpha = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		return alpha[offset++%26];
	}

	private void send(EventObject event) throws JSONException {
		ByteBuffer rowKey = se.toByteBuffer(event.getString("partition_on"));		
		mutator.addInsertion(rowKey, keyspace, createColumn(event.getEventName(), event.toString(), se, se));
		mutator.execute();
	}

	private void init() {
		logger.info("initializing cluster connection");
		
		CassandraHostConfigurator hostConfig = 
			new CassandraHostConfigurator(darkStarNode+":"+String.valueOf(darkStarPort));
		cluster = HFactory.createCluster(clusterName, hostConfig);
		keySpace = HFactory.createKeyspace(keyspace, cluster, policy);
		mutator = HFactory.createMutator(keySpace, bfs);
		
		logger.info("cluster connection initialized");
	}

	private void setProperties(String configFile) throws Exception {
		// Create the configuration class from yaml
		ClientConfigInfo info = null;
		info = ClientConfiguration.getConfiguration(configFile);
		
		// Set the configurable properties
		log4j_file = info.getLog4j_file();
		darkStarNode = info.getDarkStarNode();
		darkStarPort = info.getDarkStarPort();
		clusterName = info.getClusterName();
		keyspace = info.getKeyspace();
		delay = info.getDelay();
		
		// Configure logging
		PropertyConfigurator.configure(log4j_file);
		logger.info("DJ News Log Reader configuration complete");
	}
	
	public static void main(String[] args) {
		NamedEntitiesTest onramp = new NamedEntitiesTest();
		try {
			onramp.setProperties(args[0]);
			onramp.init();
			onramp.processMessages();
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			System.exit(1);
		}
    }
	
}
