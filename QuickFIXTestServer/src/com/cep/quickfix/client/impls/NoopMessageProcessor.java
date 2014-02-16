package com.cep.quickfix.client.impls;

import quickfix.Message;
import quickfix.SessionID;

import com.cep.quickfix.client.interfaces.MessageProcessor;

public class NoopMessageProcessor implements MessageProcessor {

	@Override
	public void onMessage(Message message, SessionID sessionID) {
		
	}

}
