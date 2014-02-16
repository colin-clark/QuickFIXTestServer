package com.cep.darkstar.dto;

import org.apache.log4j.Logger;

import com.cep.commons.EventObject;

public class Save extends AbstractDTO {
	static Logger logger = Logger.getLogger("com.cep.darkstar.DTO.Save");

	@Override
	public void doFunction(EventObject e) {
		timestamp(e);
		e.remove("function");
		logger.info("Saving Event: " + e);
		save(e);
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
