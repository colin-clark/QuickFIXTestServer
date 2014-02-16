/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved. 
 * 
 * This file is part of the QuickFIX FIX Engine 
 * 
 * This file may be distributed under the terms of the quickfixengine.org 
 * license as defined by quickfixengine.org and appearing in the file 
 * LICENSE included in the packaging of this file. 
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING 
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 * 
 * See http://www.quickfixengine.org/LICENSE for licensing information. 
 * 
 * Contact ask@quickfixengine.org if any conditions of this licensing 
 * are not clear to you.
 ******************************************************************************/

package com.cep.quickfix.client.impls;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Entry point for the Banzai application.
 */
public class QFJClient {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
	private static final String LOG_TYPE = "LogType";

    /** enable logging for this class */
    private static Logger log = LoggerFactory.getLogger(QFJClient.class);
    private static QFJClient client;
    private boolean initiatorStarted = false;
    private Initiator initiator = null;
    private String logType;
    
    public QFJClient(String[] args) throws Exception {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = QFJClient.class.getResourceAsStream("QFJClient.cfg");
        } else if (args.length == 2) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + QFJClient.class.getName() + " [configFile].");
            return;
        }
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();
        
        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true")).booleanValue();
        
        Application application;
        if (settings.isSetting("application_type") && 
        		settings.getString("application_type").equalsIgnoreCase("testspecific")) {
        	application = new TestSpecificQFJClientApplication(settings);
        } else {
        	application = new QFJClientApplication(settings);
        }
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        if (settings.isSetting(LOG_TYPE)) {
        	logType = settings.getString(LOG_TYPE);
        } else {
        	logType = "file";
        }
        LogFactory logFactory = null;
        if (logType.equalsIgnoreCase("file")) {
        	logFactory = new FileLogFactory(settings);
        } else {
        	logFactory = new ScreenLogFactory(true, true, logHeartbeats);
        }
        log.info("QFJClient is using log type " + logType);
       
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory, settings, 
        		logFactory, messageFactory);
    }

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                log.error("Logon failed", e);
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

    public static QFJClient get() {
        return client;
    }
    
    public CountDownLatch getShutdownLatch() {
    	return shutdownLatch;
    }

}