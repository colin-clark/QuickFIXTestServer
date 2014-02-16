package com.cep.metadata;

import java.util.HashMap;
import java.util.Map;

public class MapEvent {
	private String eventName;
	private Map<String,Object> fieldMap = new HashMap<String,Object>();
	
	public String getEventName() {
		return eventName;
	}
	
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public Map<String, Object> getFieldMap() {
		return fieldMap;
	}
	
	public void addField(String key, Object value) {
		fieldMap.put(key, value);
	}

}
