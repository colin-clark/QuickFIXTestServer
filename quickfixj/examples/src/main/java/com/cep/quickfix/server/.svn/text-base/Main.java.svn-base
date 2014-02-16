package com.cep.quickfix.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.cep.quickfix.server.impls.QFJServer;

import quickfix.SessionSettings;

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
	
    public static void main(String args[]) throws Exception {
        try {
            InputStream inputStream = getSettingsInputStream(args);
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();
            
			switch(args.length){
			case 3:
				clustername = args[2];
			case 2:
				hostname = args[1];
			case 1:
			default:
			}
			System.out.println("Sending messages to cluster " + clustername + " at "+hostname);
            
            QFJServer executor = new QFJServer(settings);
            executor.start();

            System.out.println("press <enter> to quit");
            System.in.read();

            executor.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = QFJServer.class.getResourceAsStream("QFJServer.cfg");
        } else if (args.length == 2) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + QFJServer.class.getName() + " [configFile].");
            System.exit(1);
        }
        return inputStream;
    }

}
