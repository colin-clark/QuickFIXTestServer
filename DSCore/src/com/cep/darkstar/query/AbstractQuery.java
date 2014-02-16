package com.cep.darkstar.query;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EntityData;

import flex.messaging.util.UUIDUtils;

/*
 * Subclass this abstract class and override the startCEPEngine() method for 
 * application-specific behavior.
 * 
 * @author Mark
 *
 */
public abstract class AbstractQuery implements QueryInt {
	protected AbstractCEPEngine cepEngine;				// our engine
	protected CEPStatement cepStatement;		// the cep statement-entry point
	protected final String queryID;				// unique query id
	protected final String query;				// the EPL of the actual query
	protected final String queryEntity;			// this really isn't used any more
	protected final String queryName;			// name of the query

	// constructor with query and query entity
	// the query entity is what is pumped into the cep engine
	public AbstractQuery(String queryName, String query, String queryEntity) {
		this.query = query;
		this.queryEntity = queryEntity;
		this.queryName = queryName;
		this.queryID = UUIDUtils.createUUID();
		
		System.out.println("queryentity:"+queryEntity+", queryID:"+queryID);
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

	/*
	 * Implement this method for individual projects
	 */
	public abstract void startCEPEngine() throws IOException;

	// return information about the query in an entitydata object
	public EntityData toEntityData() throws JSONException {
		EntityData anEntity = new EntityData();
		anEntity.put("query_id", queryID);
		anEntity.put("query", query);
		return anEntity;
	}
}
