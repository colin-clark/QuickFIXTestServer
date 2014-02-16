package com.cep.simpleclient;

import java.io.FileInputStream;
import java.io.InputStream;

import quickfix.SessionSettings;

public class SimpleClient {
	// Instance vars
	protected String logType = "file";
	protected boolean logHeartbeats = false;
	protected SimpleClientApplication application;
    
    public SimpleClient(String[] args) throws Exception {
    	// Create an input stream from the config file
        InputStream inputStream = null;
        if (args.length != 0) {
            inputStream = new FileInputStream(args[0]);
        } 
        if (inputStream == null) {
            System.out.println("usage: " + SimpleClient.class.getName() + " [configFile].");
            return;
        }
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();
        application = new SimpleClientApplication(settings);
    }

    public synchronized void logon() {
    	application.start();
    }
   
}