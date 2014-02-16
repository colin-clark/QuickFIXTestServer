package com.cep.darkstar.node;

import java.io.IOException;

import org.json.JSONException;
import com.cep.commons.EntityData;
import com.cep.commons.EventObject;

public class Query implements QueryInt {
	@SuppressWarnings("unused")
	private final CEPEngine cepEngine;			// our engine
	private final CEPStatement cepStatement;	// the cep statement-entry point
	private final String queryID;				// unique query id
	private final String query;					// the EPL of the actual query
	private final String queryName;				// name of the query
	private final String nodeID;				// nodeID
	
	// PublishTopic info for RabbitMQ
	private Integer publishPort = null;
	private String publishExchange = null;
	private String publishTopic = null;

	// constructor with query and query entity
	// the query entity is what is pumped into the cep engine
	public Query(String queryName, String query, CEPEngine cepEngine) {
		this.query = query;
		this.queryName = queryName;
		this.queryID = queryName;
		this.cepEngine = cepEngine;
		this.nodeID = cepEngine.nodeID();
		cepStatement = new CEPStatement(cepEngine.getEPAdministrator(), queryName, query);
	}

	public String getQueryID() {
		return queryID;
	}

	public String getQuery() {
		return query;
	}
	
	public String getQueryName() {
		return queryName;
	}

	public void stop() {
		if (cepStatement.isRunning()) {
			cepStatement.stop();
		}
	}

	public void kill() {
		cepStatement.kill();
	}

	public void start() {
		if (cepStatement.isStopped()) {
			cepStatement.start();
		}
	}

	public boolean isRunning() {
		return cepStatement.isRunning();
	}

	public boolean isStopped() {
		return cepStatement.isRunning();
	}

	public boolean isDestroyed() {
		return cepStatement.isDestroyed();
	}

	// kick the tires and light the fires
	public void startCEPEngine() throws IOException {
		// add a listener 
		CEPStatementListener cepStatementListener;
		if (publishExchange != null) {
			cepStatementListener = new CEPStatementListener(queryID, nodeID, publishTopic, publishPort, publishExchange);
		} else if (publishPort != null) {
			cepStatementListener = new CEPStatementListener(queryID, nodeID, publishTopic, publishPort);
		} else if (publishTopic != null) {
			cepStatementListener = new CEPStatementListener(queryID, nodeID, publishTopic);
		} else {
			cepStatementListener = new CEPStatementListener(queryID, nodeID);
		}
		cepStatement.addListener(cepStatementListener);
	}

	// return information about the query in an entitydata object
	public EntityData toEntityData() throws JSONException {
		EntityData anEntity = new EntityData();
		anEntity.put("query_id", queryID);
		anEntity.put("query", query);
		return anEntity;
	}

	public void extractPublishingInfo(EventObject queryEvent) {
		if (queryEvent.has(QueryInt.PUBLISH_PORT)) {
			try {
				publishPort = queryEvent.getInt(QueryInt.PUBLISH_PORT);
			} catch (Exception e) {
				// Just swallow these and use the default value
			}
		}
		if (queryEvent.has(QueryInt.EXCHANGE)) {
			try {
				publishExchange = queryEvent.getString(QueryInt.EXCHANGE);
			} catch (Exception e) {
				// Just swallow these and use the default value
			}
		}
		if (queryEvent.has(QueryInt.TOPIC)) {
			try {
				publishTopic = queryEvent.getString(QueryInt.TOPIC);
			} catch (Exception e) {
				// Just swallow these and use the default value
			}
		}
	}
}
