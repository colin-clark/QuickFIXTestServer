package com.cep.simpleclient;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.PropertyConfigurator;

public class Main {
	public static final CountDownLatch shutdownLatch = new CountDownLatch(1);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("log4j.configuration");
			SimpleClient client = new SimpleClient(args);
			client.logon();
			Main.shutdownLatch.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
