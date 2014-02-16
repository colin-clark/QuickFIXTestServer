package com.cep.darkstar.dto;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.node.CEPEventsSendRunnable;

public class DefaultDTO extends AbstractDTO implements IDTO {
	static Logger logger = Logger.getLogger("com.cep.darkstar.DTO.DefaultDTO");
	
	public void doFunction(EventObject e) {
		timestamp(e);
		e.remove("function");
		boolean persisted = false;
		if (e.has(PERSISTED)) {
			try {
				persisted = e.getBoolean(PERSISTED);
			} catch (JSONException e1) {
				// This one we can safely swallow
			}
		}
		if (!persisted) {
			try {
				e.put(PERSISTED, true);
				CEPEventsSendRunnable.handleEventObject(e);
				doSave(e);
			} catch (Exception e3) {
				logger.error("Save of event " + e + " interrupted", e3);
			}
		} else {
			CEPEventsSendRunnable.handleEventObject(e);
		}
	}
	

	@Override
	public void run() {
		while(true) {
			try {
				doFunction(getNext());
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}


	private void doSave(EventObject next) {
		if (logger.isDebugEnabled()) {
			logger.debug("Doing Save for " + next);
		}
		save(next);
	}


}
