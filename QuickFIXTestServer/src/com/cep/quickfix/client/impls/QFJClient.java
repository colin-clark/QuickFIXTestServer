package com.cep.quickfix.client.impls;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

public class QFJClient {
	// static constants
	public static final String LOG_TYPE = "LogType";
	public static final String LOG_HEARTBEATS = "logHeartbeats";
	public static final String APPLICATION_VERSION = "applicationVersion";

	// Instance vars
	protected final CountDownLatch shutdownLatch = new CountDownLatch(1);
	protected boolean initiatorStarted = false;
	protected Initiator initiator = null;
	protected String logType = "file";
	protected boolean logHeartbeats = true;
    
    public QFJClient(String[] args) throws Exception {
    	// Create an input stream from the config file
        InputStream inputStream = null;
        if (args.length != 0) {
            inputStream = new FileInputStream(args[0]);
        } 
        if (inputStream == null) {
            System.out.println("usage: " + QFJClient.class.getName() + " [configFile].");
            return;
        }
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();
        
        // Read in the values for the instance vars
        if (settings.isSetting(LOG_HEARTBEATS)) {
        	logHeartbeats = settings.getBool(LOG_HEARTBEATS);
        }
        if (settings.isSetting(LOG_TYPE)) {
        	logType = settings.getString(LOG_TYPE);
        } 
        
        // Create QFJ factories based on config
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = null;
        if (logType.equalsIgnoreCase("file")) {
        	logFactory = new FileLogFactory(settings);
        } else {
        	logFactory = new ScreenLogFactory(true, true, logHeartbeats);
        }
        System.out.println("QFJClient is using log type " + logType);       
        MessageFactory messageFactory = new DefaultMessageFactory();
        
        // Create the application and the QFJ initiator
        Application application;
        String appVersion = "1";
        if (settings.isSetting(APPLICATION_VERSION)) {
        	try {
        		appVersion = settings.getString(APPLICATION_VERSION);
        	} catch (Exception e) {
        		System.err.println("Exception caught setting application version, default to 1");
        		appVersion = "1";
        	}
        }
        if (appVersion.equalsIgnoreCase("2")) {
        	application = new QFJClientApplicationV2(settings);
        } else {
        	application = new QFJClientApplication(settings);
        }
        
        // Wait for client autodiscovery
        System.out.println("Pausing to allow host autodiscovery");
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted, host autodiscovery may not have taken place");
		}
        initiator = new SocketInitiator(application, messageStoreFactory, settings, 
        		logFactory, messageFactory);
    }

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                System.err.println("Logon failed");
                e.printStackTrace();
            }
        } else {
            Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
            while (sessionIds.hasNext()) {
                SessionID sessionId = (SessionID) sessionIds.next();
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    public void logout() {
        Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
        while (sessionIds.hasNext()) {
            SessionID sessionId = (SessionID) sessionIds.next();
            Session.lookupSession(sessionId).logout("user requested");
        }
    }

    public void stop() {
        shutdownLatch.countDown();
    }
    
    public CountDownLatch getShutdownLatch() {
    	return shutdownLatch;
    }
    
}