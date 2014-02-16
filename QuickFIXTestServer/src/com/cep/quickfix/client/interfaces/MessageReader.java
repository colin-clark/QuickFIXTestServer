package com.cep.quickfix.client.interfaces;

import quickfix.Session;

public interface MessageReader extends Runnable {

	public void setSession(Session session);

}
