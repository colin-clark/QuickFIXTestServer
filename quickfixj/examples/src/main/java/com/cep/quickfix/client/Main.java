package com.cep.quickfix.client;

import com.cep.quickfix.client.impls.QFJClient;

public class Main {
	
	// default to localhost
	static String hostname = "localhost";
	static String clustername = "DarkStarCluster";
	
	public static String getHostname() {
		return hostname;
	}
	
	public static String getClustername() {
		return clustername;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			switch(args.length){
			case 3:
				clustername = args[2];
			case 2:
				hostname = args[1];
			case 1:
			default:
			}
			System.out.println("Sending messages to cluster " + clustername + " at "+hostname);
			QFJClient client;
			client = new QFJClient(args);
			client.logon();
			client.getShutdownLatch().await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
