package com.cep.darkstar.node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.UpdateListener;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;

public class CEPStatementListener implements UpdateListener {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.node.CEPStatementListener");
	private String queryID = null;
	private long ts;
	private PublishTopic publisher = null;
	private PublishTopic HAPublisher = null;
	private boolean publishHA = false;
	InetAddress myAddress = null;
	private String nodeID = null;
	private volatile boolean primaryPublisherDown = false;
	private volatile boolean HAPublisherDown = false;
	
	// Publisher settings
	private final String topic;	
	private final int portNumber;
	private final String exchange;
	
	public static final String DEFAULT_EXCHANGE = "amq.topic";
	public static final int DEFAULT_PORT = AMQP.PROTOCOL.PORT;
	public static final String DEFAULT_TOPIC = "REDUCE";

	public CEPStatementListener(String queryID, String nodeID, String topic, int portNumber, String exchange) {
		this.queryID = queryID;
		this.nodeID = nodeID;
		this.topic = topic;
		this.portNumber = portNumber;
		this.exchange = exchange;
		createPublisher();
	}
	
	public CEPStatementListener(String queryID, String nodeID, String topic, int portNumber) {
		this(queryID, nodeID, topic, portNumber, DEFAULT_EXCHANGE);
	}
	
	public CEPStatementListener(String queryID, String nodeID, String topic) {
		this(queryID, nodeID, topic, DEFAULT_PORT, DEFAULT_EXCHANGE);
	}
	
	public CEPStatementListener(String queryID, String nodeID) {
		this(queryID, nodeID, DEFAULT_TOPIC, DEFAULT_PORT, DEFAULT_EXCHANGE);
	}
	
	private void createPublisher() {
		try {
			logger.info("Creating publisher for topic " + topic + ", address " + DarkStarNode.getSupervisorHostname() + 
					" on port " + portNumber);
			publisher = new PublishTopic.Builder().hostName(DarkStarNode.getSupervisorHostname())
				.topic(topic).portNumber(portNumber).exchange(exchange).build();
			if (DarkStarNode.isCluster_surveillance() && topic.toUpperCase().startsWith("SURV")) {
				logger.info("Creating HA publisher for topic " + topic + ", address " + DarkStarNode.getHASupervisorHostname() +
						" on port " + portNumber);
				HAPublisher = new PublishTopic.Builder().hostName(DarkStarNode.getHASupervisorHostname())
					.topic(topic).portNumber(portNumber).exchange(exchange).build();
				publishHA = true;
			}
		} catch (IOException e) {
			logger.error("Exception caught creating publisher(s): " + e.getMessage(), e);
		}				
	}
		
	public void update(EventBean[] newData, EventBean[] oldData) {
		// new event
		if (newData != null) {
			for(int i=0;i<newData.length;i++) {
				EventObject anEvent = new EventObject();
				try {
					Map<?, ?> aMap = (HashMap<?, ?>) newData[i].getUnderlying();
					anEvent = new EventObject(aMap);
				} catch (Exception e) {
					for (String name : newData[i].getEventType().getPropertyNames()) {
						try {
							anEvent.put(name, newData[i].get(name));
						} catch (PropertyAccessException e1) {
							logger.error(e1.getMessage(), e1);
						} catch (JSONException e1) {
							logger.error(e1.getMessage(), e1);
						}
					}
				}
				// add our wholesome instrumentation
				try {
					ts = System.currentTimeMillis();
					anEvent.put("_N_query_id", queryID);
					anEvent.put("_N_ts", ts);
					anEvent.setEventName(queryID);
					anEvent.put("_N_id", nodeID);
				} catch (JSONException e) {
					logger.error(e.getMessage(), e);
				}
				// now publish it
				if (!primaryPublisherDown) {
					try {
						publisher.publish(anEvent);
					} catch (IOException e) {
						logger.error("Exception caught publishing on Primary RabbitMQ connection: " + e.getMessage(), e);
					} catch (JSONException e) {
						logger.error("Exception caught publishing on Primary RabbitMQ connection: " + e.getMessage(), e);
					} catch (AlreadyClosedException e) {
						logger.error("AlreadyClosedException caught publishing on Primary RabbitMQ connection: " + e.getMessage() 
								+ ": This connection will be shut down!", e);
						primaryPublisherDown = true;
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Cannot publish on primary RabbitMQ connection");
					}
				}
				if (publishHA && !HAPublisherDown) {
					try {
						HAPublisher.publish(anEvent);
					} catch (IOException e) {
						logger.error("Exception caught publishing on Secondary RabbitMQ connection: " + e.getMessage(), e);
					} catch (JSONException e) {
						logger.error("Exception caught publishing on Secondary RabbitMQ connection: " + e.getMessage(), e);
					} catch (AlreadyClosedException e) {
						logger.error("AlreadyClosedException caught publishing on Secondary RabbitMQ connection: " + e.getMessage()
								+ ": This connection will be shut down!", e);
						HAPublisherDown = true;
					}
				}
				
			}
		}
	}
}			
