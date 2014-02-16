/**
 * Cloud Event Processing, Inc.
 * 
 */

package com.cep.darkstar.query.remote;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

import flex.messaging.util.UUIDUtils;

/**
 * @author colin
 *
 */

@SuppressWarnings("unused")
public class CEPRemoteStatement {
	private EPStatement statement;
	private EPStatement cepCreateRemoteEvent;
	private EPStatement cepNamedStatement;
	private String eventType;
	private String eplStatement;
	private String queryName;
	String remoteQueryID = null;
	Logger logger = Logger.getLogger("com.cep.darkstar.query.remote.CEPRemoteStatement");
	
	public CEPRemoteStatement(EPAdministrator admin, String eventType, String remoteQueryID, String eplStatement)
	{
		this.eplStatement = eplStatement;
		this.setQueryName(remoteQueryID);
		this.setEventType(eventType);
		this.remoteQueryID = "RQ"+stripGarbage(UUIDUtils.createUUID());
		logger.info("Running query:"+this.eplStatement);
		cepCreateRemoteEvent = admin.createEPL("create variant schema "+remoteQueryID +" as *");
		statement = admin.createEPL("select * from "+remoteQueryID);
	
		// and now distribute query
		try {
			SubmitRemoteQuery(remoteQueryID, this.eplStatement);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String stripGarbage(String s) {  
        String good = " @#abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789���T:";
        String result = "";
        for ( int i = 0; i < s.length(); i++ ) {
            if ( good.indexOf(s.charAt(i)) >= 0 )
               result += s.charAt(i);
            }
        return result;
    }
	
	private void SubmitRemoteQuery(String queryID, String query) throws IOException {
		PublishTopic publish = new PublishTopic.Builder().topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", queryID);
			anEvent.put("Nq", query);
			anEvent.put("cmd", "NEW");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			logger.info("Sending query to cluster for execution:"+query);
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
			logger.info("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SubmitRemoteKill(String queryID, String query) throws IOException {
		logger.info("Submitting remote kill");
		PublishTopic publish = new PublishTopic.Builder().topic("QUERY").build();
		// and publish a test object
		EventObject anEvent = new EventObject();
		try {
			anEvent.put("NqID", queryID);
			anEvent.put("Nq", query);
			anEvent.put("cmd", "KILL");
			anEvent.setEventName("QUERY");
			publish.publish(anEvent);
			logger.info("Sending query to cluster for execution:"+query);
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
			logger.info("Sending query to cluster for execution:"+query);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void addListener(UpdateListener listener) {
		statement.addListener(listener);
	}
	
	// kill the statement
	public void kill() {
		try {
			SubmitRemoteKill(remoteQueryID, eplStatement);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			statement.destroy();
		}
	}
	
	public void stop() {
		try {
			SubmitRemoteStop(remoteQueryID, eplStatement);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			statement.stop();
		}
	}
	
	public void start() {
		try {
			SubmitRemoteStart(remoteQueryID, eplStatement);
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

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * @param queryName the queryName to set
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	/**
	 * @return the queryName
	 */
	public String getQueryName() {
		return queryName;
	}
}
