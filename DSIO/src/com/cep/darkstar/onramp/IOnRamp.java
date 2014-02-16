package com.cep.darkstar.onramp;

import org.json.JSONException;

import com.cep.commons.EventObject;

public interface IOnRamp {

	public void setProperties(String darkStarNode, int darkStarPort, String clusterName, 
			String keyspace, String eventName);
	
	public void init();
		
	public void send(EventObject event, String key) throws JSONException;
	
    public void send(EventObject event) throws JSONException ;
}
