package com.cep.darkstar.dto;

import org.apache.log4j.Logger;

import com.cep.commons.EventObject;
import com.cep.darkstar.node.CEPEventsSendRunnable;

public class AppiaDTO extends AbstractDTO implements IDTO {
	static Logger logger = Logger.getLogger("com.cep.darkstar.DTO.AppiaDTO");
	
	public void doFunction(EventObject e) {
		e.remove("function");
		save(e);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending Event: " + e);
		}
		CEPEventsSendRunnable.handleEventObject(e);
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


}
