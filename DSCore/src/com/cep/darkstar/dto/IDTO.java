package com.cep.darkstar.dto;

import java.util.concurrent.BlockingQueue;

import com.cep.commons.EventObject;
import com.cep.darkstar.query.ICEPEngine;

public interface IDTO extends Runnable {
	public BlockingQueue<EventObject> getQueue();
	public void setCallback(ICEPEngine callback);
	public void doFunction(EventObject event);
}
