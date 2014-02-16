package com.cep.commons;

import java.sql.Date;
import java.sql.Timestamp;

import org.json.JSONException;


public class EntityData {
	private EventObject row = null;
	
	public String toString() {
		return row.toString();
	}
	
	public String eventType() throws JSONException {
		return row.getEventName();
	}
	
	public String getEntityData() {
		return row.toString();
	}
	
	public void setEntityData(String aRow) throws JSONException {
		row = new EventObject(aRow);
	}
	public EntityData(String aRow) throws JSONException {
		row = new EventObject(aRow);
	}
	
	public EntityData() {
		row = new EventObject();
	}

	public void setRow(String aRow) throws JSONException {
		row = new EventObject(aRow);
	}
	
	public void put(String aKey, String aValue) throws JSONException {
		row.put(aKey, aValue);
	}
	
	public Object get(String aKey) throws JSONException {
		return row.get(aKey);
	}

	public void put(String aKey, long aLongValue) throws JSONException {
		row.put(aKey, aLongValue);		
	}

	public EventObject getEventObject() {
		return row;
	}

	/**
	 * @param aKey
	 * @param date
	 * @throws JSONException 
	 */
	public void put(String aKey, Date date) throws JSONException {
		// TODO Auto-generated method stub
		row.put(aKey, date.toString());
	}

	/**
	 * @param aKey
	 * @param timestamp
	 * @throws JSONException 
	 */
	public void put(String aKey, Timestamp timestamp) throws JSONException {
		row.put(aKey, timestamp.toString());		
	}
}
