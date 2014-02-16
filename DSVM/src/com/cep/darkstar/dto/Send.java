package com.cep.darkstar.dto;

import org.apache.log4j.Logger;

import com.cep.commons.EventObject;
import com.cep.darkstar.node.CEPEventsSendRunnable;

public class Send extends AbstractDTO {
	static Logger logger = Logger.getLogger("com.cep.darkstar.DTO.Send");

	@Override
	public void doFunction(EventObject e) {
		timestamp(e);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending Event: " + e);
		}
		e.remove("function");
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
