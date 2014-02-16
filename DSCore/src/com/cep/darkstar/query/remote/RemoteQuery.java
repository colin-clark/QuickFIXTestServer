/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.query.remote;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EntityData;
import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;
import com.cep.darkstar.query.AbstractCEPEngine;
import com.cep.darkstar.query.DefaultCEPEngine;
import com.cep.darkstar.query.QueryInt;
import com.cep.utils.Utils;
import com.espertech.esper.client.UpdateListener;

import flex.messaging.util.UUIDUtils;

/**
 * @author colin
 *
 */
public class RemoteQuery implements QueryInt {
	private AbstractCEPEngine cepEngine;				// our engine
	private CEPRemoteStatement cepStatement;	// the cep statement-entry point
	private final String localQueryID;			// unique query id
	private final String remoteQueryID;			// unique query id
	private final String query;					// the EPL of the actual query
	private final String queryEntity;			// this really isn't used any more
	private UpdateListener listener = null;

	// constructor with query and query entity
	// the query entity is what is pumped into the cep engine
	// need to remove the queryName parameter
	public RemoteQuery(String queryName, String query, String queryEntity) {
		this.query = query;
		this.queryEntity = queryEntity;
		// have to have an alpha in front of this string
		this.localQueryID = "LQ"+Utils.stripGarbage(UUIDUtils.createUUID());
		this.remoteQueryID = "RQ"+Utils.stripGarbage(UUIDUtils.createUUID());
		
		System.out.println("queryentity:"+queryEntity+", queryID:"+localQueryID);
	}
	
	public RemoteQuery(String queryName, String query, String queryEntity, UpdateListener listener) {
		this.query = query;
		this.queryEntity = queryEntity;
		// have to have an alpha in front of this string
		this.localQueryID = "LQ"+Utils.stripGarbage(UUIDUtils.createUUID());
		this.remoteQueryID = "RQ"+Utils.stripGarbage(UUIDUtils.createUUID());
		this.listener = listener;
		System.out.println("queryentity:"+queryEntity+", queryID:"+localQueryID);
	}
	
	public String getQueryID() {
		return localQueryID;
	}
	
	public String getRemoteQueryID() {
		return remoteQueryID;
	}

	public String getQueryEntity() {
		return queryEntity;
	}

	public String getQuery() {
		return query;
	}

	public void stop() {
		if (cepStatement.isRunning()) {
			try {
				stopRemoteQuery();
			} catch (IOException e) {
				e.printStackTrace();
			}
			cepStatement.stop();
		}
	}
	
	public void stopRemoteQuery() throws IOException {
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", remoteQueryID);
			anEvent.put("cmd", "STOP");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void kill() {
		try {
			killRemoteQuery();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cepStatement.kill();
	}
	
	public void killRemoteQuery() throws IOException {
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", remoteQueryID);
			anEvent.put("cmd", "KILL");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void startRemoteQuery() throws IOException {
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", remoteQueryID);
			anEvent.put("cmd", "START");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+anEvent.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if (cepStatement.isStopped()) {
			cepStatement.start();
		}
		try {
			startRemoteQuery();
		} catch (IOException e) {
			e.printStackTrace();
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
		cepEngine = new DefaultCEPEngine();	
		cepStatement = new CEPRemoteStatement(cepEngine.getEPAdministrator(), queryEntity, remoteQueryID, query);

		// and a listener 
		if (listener != null) {
			cepStatement.addListener(listener);
		}
			
	}

	// return information about the query in an entitydata object
	public EntityData toEntityData() throws JSONException {
		EntityData anEntity = new EntityData();
		anEntity.put("query_id", localQueryID);
		anEntity.put("query", query);
		return anEntity;
	}
}
