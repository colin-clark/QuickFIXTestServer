/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.query.remote;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

/**
 * @author colin
 *
 */

@SuppressWarnings("unused")
public class CEPRemoteNamedStatement {

	private EPStatement statement;
	private String eventType;
	private String eplStatement;
	private String queryName;
	private EPStatement cepCreateRemoteEvent;
	
	public CEPRemoteNamedStatement(EPAdministrator admin, String eventType, String queryName, String remoteQueryID, String eplStatement)
	{
		this.eplStatement = eplStatement;
		this.queryName = queryName;
		this.eventType = eventType;

		System.out.println("Running query:"+this.eplStatement);
		// named statement
		// insert the query into the name so that the query name can be re-used
		//cepCreateRemoteEvent = admin.createEPL("insert into "+queryName+" "+eplStatement);
		this.cepCreateRemoteEvent = admin.createEPL("create variant schema "+queryName+" as *");
		this.statement = admin.createEPL("select * from "+queryName);
	
		// and now distribute query
		try {
			submitRemoteQuery(queryName, eplStatement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void submitRemoteQuery(String queryID, String query) throws IOException {
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", queryID);
			anEvent.put("Nq", query);
			anEvent.put("cmd", "NAMED_QUERY");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void submitRemoteStatement(String queryID, String query) throws IOException {
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", queryID);
			anEvent.put("Nq", query);
			anEvent.put("cmd", "STATEMENT");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SubmitRemoteStop(String queryID, String query) throws IOException {
		PublishTopic publish = new PublishTopic.Builder().topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", queryID);
			anEvent.put("Nq", query);
			anEvent.put("cmd", "STOP");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SubmitRemoteKill(String queryID, String query) throws IOException {
		PublishTopic publish = new PublishTopic.Builder().topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", queryID);
			anEvent.put("Nq", query);
			anEvent.put("cmd", "KILL");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SubmitRemoteStart(String queryID, String query) throws IOException {
		PublishTopic publish = new PublishTopic.Builder().topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", queryID);
			anEvent.put("Nq", query);
			anEvent.put("cmd", "START");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			System.out.println("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void addListener(UpdateListener cepStatementListener)
	{
		statement.addListener(cepStatementListener);
	}

	// kill the statement
	public void kill() {
		try {
			SubmitRemoteKill(queryName, eplStatement);
		} catch (IOException e) {
			e.printStackTrace();
		}
		statement.destroy();
	}

	public void stop() {
		try {
			SubmitRemoteStop(queryName, eplStatement);
		} catch (IOException e) {
			e.printStackTrace();
		}
		statement.stop();
	}

	public void start() {
		try {
			SubmitRemoteStart(queryName, eplStatement);
		} catch (IOException e) {
			e.printStackTrace();
		}
		statement.start();
	}

	public boolean isRunning() {
		return statement.isStarted();
	}

	public boolean isStopped() {
		return statement.isStopped();
	}

	public boolean isDestroyed() {
		return statement.isDestroyed();
	}
}
