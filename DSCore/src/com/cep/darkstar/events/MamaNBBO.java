package com.cep.darkstar.events;

import org.json.JSONException;

import com.cep.commons.EventObject;

public class MamaNBBO {
	public double bb;
	public double bo;
	public double bbSize;
	public double boSize;
	public long timestamp;
	public String symbol;
	
	public MamaNBBO(String symbol, double bb, double bo, double bbSize, double boSize, long timestamp) {
		this.symbol = symbol;
		this.bb = bb;
		this.bo = bo;
		this.bbSize = bbSize;
		this.boSize = boSize;
		this.timestamp = timestamp;
	}
	public double getBb() {
		return bb;
	}
	public void setBb(double bb) {
		this.bb = bb;
	}
	public double getBo() {
		return bo;
	}
	public void setBo(double bo) {
		this.bo = bo;
	}
	public double getBbSize() {
		return bbSize;
	}
	public void setBbSize(double bbSize) {
		this.bbSize = bbSize;
	}
	public double getBoSize() {
		return boSize;
	}
	public void setBoSize(double boSize) {
		this.boSize = boSize;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public EventObject toEventObject() {
		EventObject eo = new EventObject();
		try {
			eo.put("symbol", symbol);
			eo.put("bb", bb);
			eo.put("bo", bo);
			eo.put("bbSize", bbSize);
			eo.put("boSize", boSize);
			eo.put("timestamp", timestamp);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return eo;
	}

}
