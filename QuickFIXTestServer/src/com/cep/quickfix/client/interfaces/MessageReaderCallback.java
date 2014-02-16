package com.cep.quickfix.client.interfaces;

import quickfix.Message;
import quickfix.Session;

public interface MessageReaderCallback {
	
	public void sendMessage(Session session, Message message);

}
