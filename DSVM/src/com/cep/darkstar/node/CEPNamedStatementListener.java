/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.rabbitmq.client.AMQP;

import flex.messaging.util.UUIDUtils;

/**
 * @author colin
 *
 */
public class CEPNamedStatementListener implements StatementAwareUpdateListener {
	final static Logger dsLog = Logger.getLogger("com.cep.darkstar.node.CEPNamedStatementListener");
	
	private String queryID = null;
	private long ts;
	private PublishTopic publisher = null;
	static String machineID = null;
	
	// Publisher settings
	private final String topic;	
	private final int portNumber;
	private final String exchange;
	
	public static final String DEFAULT_EXCHANGE = "amq.topic";
	public static final int DEFAULT_PORT = AMQP.PROTOCOL.PORT;
	public static final String DEFAULT_TOPIC = "REDUCE";

	public CEPNamedStatementListener(String queryID, CEPEngine cepEngine, String topic, int portNumber, String exchange) {
		this.queryID = queryID;
		this.topic = topic;
		this.portNumber = portNumber;
		this.exchange = exchange;
		createPublisher();
		if (machineID==null) {
			machineID = UUIDUtils.createUUID();
		}
	}
	
	public CEPNamedStatementListener(String queryID, CEPEngine cepEngine, String topic, int portNumber) {
		this(queryID, cepEngine, topic, portNumber, DEFAULT_EXCHANGE);
	}
	
	public CEPNamedStatementListener(String queryID, CEPEngine cepEngine, String topic) {
		this(queryID, cepEngine, topic, DEFAULT_PORT, DEFAULT_EXCHANGE);
	}
	
	public CEPNamedStatementListener(String queryID, CEPEngine cepEngine) {
		this(queryID, cepEngine, DEFAULT_TOPIC, DEFAULT_PORT, DEFAULT_EXCHANGE);
	}
	
	private void createPublisher() {
		try {
			publisher = new PublishTopic.Builder().hostName(DarkStarNode.getSupervisorHostname())
				.topic(topic).portNumber(portNumber).exchange(exchange).build();
		} catch (IOException e) {
			dsLog.error(e.getMessage(), e);
		}				
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData, EPStatement statement, EPServiceProvider epServiceProvider) {
		if (newData != null) {
			for(int i=0;i<newData.length;i++) {
				try {
					EventObject anEvent = new EventObject();
					try {
						Map<?, ?> aMap = (HashMap<?, ?>) newData[i].getUnderlying();
						anEvent = new EventObject(aMap);
					} catch (Exception e) {
						for (String name : newData[i].getEventType().getPropertyNames()) {
							try {
								anEvent.put(name, newData[i].get(name));
							} catch (PropertyAccessException e1) {
								dsLog.error(e1.getMessage(), e1);
							} catch (JSONException e1) {
								dsLog.error(e1.getMessage(), e1);
							}
						}
					}
					ts = System.currentTimeMillis();
					anEvent.put("_query_id", queryID);
					anEvent.put("_N_ts", ts);
					anEvent.setEventName(queryID);
					anEvent.put("_N_id", machineID);
					anEvent.put("_N_query_id", queryID);
					try {
						publisher.publish(anEvent);
					} catch (IOException e) {
						dsLog.error(e.getMessage(), e);
					}
				} catch (JSONException e) {
					dsLog.error(e.getMessage(), e);
				}
			}
		}
	}
}

