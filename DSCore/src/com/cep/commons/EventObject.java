package com.cep.commons;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class EventObject extends JSONObject {
	
	// normal constructor
	public EventObject() {
		super();
	}
	
	// maps
	public EventObject(Map<?, ?> aMap) {
		super(aMap);
	}
	
	//array of event objects - this is starting to get funky with the cheeze whiz
	public EventObject(EventObject[] eventArray) {
		super(eventArray);
	}
	
	// we should use an interface to do this - uniting the above method call as well
	public EventObject(JSONObject[] jsonArray) {
		super(jsonArray);
	}
	
	// event name constructor
	public EventObject(String anEvent) throws JSONException {
		super(anEvent);
	}
	
	public EventObject(JSONTokener jsonTokener) throws JSONException {
		super(jsonTokener);
	}

	public String getEventName() throws JSONException {return getString("event_name");}
	public void setEventName(String anEventName) throws JSONException {
		put("event_name", anEventName);
	}
}
