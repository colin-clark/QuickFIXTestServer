package com.cep.darkstar.utility.cassandra;

import org.json.JSONException;

import com.cep.commons.EventObject;

public interface MessageReaderCallback {
	
	public void send(EventObject event) throws JSONException;

	public void finished();

}
