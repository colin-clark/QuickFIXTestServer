package com.cep.quickfix.client.interfaces;

import quickfix.Message;
import quickfix.SessionID;

public interface MessageProcessor {
	public void onMessage(Message message, SessionID sessionID);
}
