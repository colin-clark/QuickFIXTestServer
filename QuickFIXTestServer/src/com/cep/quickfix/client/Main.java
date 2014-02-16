package com.cep.quickfix.client;

import com.cep.quickfix.client.impls.QFJClient;

public class Main {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			QFJClient client;
			client = new QFJClient(args);
			client.logon();
			client.getShutdownLatch().await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
