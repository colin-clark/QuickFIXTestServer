/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.query;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EntityData;

import flex.messaging.util.UUIDUtils;

/**
 * @author colin
 *
 */
public class NamedQuery implements QueryInt {

	private AbstractCEPEngine cepEngine;				// our engine
	private CEPNamedStatement cepStatement;			// the cep statement-entry point
	private final String queryID;				// unique query id
	private final String query;					// the EPL of the actual query
	private final String queryEntity;			// this really isn't used any more
	private final String queryName;				// name of the query

	// constructor with query and query entity
	// the query entity is what is pumped into the cep engine
	public NamedQuery(String queryName, String query, String queryEntity) {
		this.query = query;
		this.queryEntity = queryEntity;
		this.queryName = queryName;
		this.queryID = UUIDUtils.createUUID();
	}

	public String getQueryID() {
		return queryID;
	}

	public String getQueryEntity() {
		return queryEntity;
	}

	public String getQuery() {
		return query;
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
		// so, this creation of the cep engine needs to move
		cepEngine = new DefaultCEPEngine();	

		// and a query 
		cepStatement = new CEPNamedStatement(cepEngine.getEPAdministrator(), queryName, query);

		// and a listener 
		CEPNamedStatementListener cepStatementListener = new CEPNamedStatementListener(queryID, cepEngine);
		cepStatement.addListener(cepStatementListener);
	}

	// return information about the query in an entitydata object
	public EntityData toEntityData() throws JSONException {
		EntityData anEntity = new EntityData();
		anEntity.put("query_id", queryID);
		anEntity.put("query", query);
		return anEntity;
	}
}
