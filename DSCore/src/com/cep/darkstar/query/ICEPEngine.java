package com.cep.darkstar.query;

import com.cep.commons.EventObject;

public interface ICEPEngine {
	
	public String nodeID();

	public void handleEventObject(EventObject event);

	public void send(EventObject e);
	
}
