/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.node;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EntityData;

/**
 * @author colin
 *
 */
public class Statement {

	private CEPEngine cepEngine;				// our engine
	private CEPStatement cepStatement;			// the cep statement-entry point
	private final String query;					// the EPL of the actual query

	// constructor with query and query entity
	// the query entity is what is pumped into the cep engine
	public Statement(String query, CEPEngine cepEngine) {
		this.cepEngine = cepEngine;
		this.query = query;
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
	public void submitStatement() throws IOException {
		// and a query 
		cepStatement = new CEPStatement(cepEngine.getEPAdministrator(), "dummy", query);
	}

	// return information about the query in an entitydata object
	public EntityData toEntityData() throws JSONException {
		EntityData anEntity = new EntityData();
		anEntity.put("query", query);
		return anEntity;
	}
}
