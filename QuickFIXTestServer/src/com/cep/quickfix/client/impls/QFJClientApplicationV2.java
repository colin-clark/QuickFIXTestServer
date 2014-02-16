package com.cep.quickfix.client.impls;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;

import com.cep.quickfix.client.interfaces.MessageProcessor;
import com.cep.quickfix.client.interfaces.MessageReader;
import com.cep.quickfix.client.interfaces.MessageReaderCallback;

public class QFJClientApplicationV2 implements Application, MessageReaderCallback {
	/**
	 * This version of the Client Application is designed to read in an input file
	 * specifying the messages to create rather than sending randomly generated
	 * messages in at a fixed rate to allow for finer-grained testing.
	 */
	
	// static constants
	public static final String HOSTNAME = "hostname";
	public static final String CLUSTER_NAME = "clustername";
	public static final String MESSAGE_FILE = "messageFile";
	public static final String MESSAGE_READER_TYPE = "type";
	public static final String MESSAGE_READER_SOURCE = "source";

	// MessageProcessor is the interface used for implementing the DarkStar client
	private MessageProcessor processor;
	
	// Instance vars for this application instance which will be set from config file
	private HashMap<SessionID, String> sessionVersions = new HashMap<SessionID, String>();
	private HashMap<SessionID, MessageReader> sessionReaders = new HashMap<SessionID, MessageReader>();
	private String hostname = "localhost";
	private String clustername = "DarkStarCluster";
	private String firm = "CEP";
	
	// vars for setting up the MessageReader
	private String type = "file";

	public QFJClientApplicationV2(SessionSettings settings) throws Exception {
		System.out.println("Initializing Application");
		if (settings.isSetting("Firm")) {
			try {
				firm = settings.getString("Firm");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Exception caught setting firm, using default of CEP");
				firm = "CEP";
			} 
		}
        if (settings.isSetting(HOSTNAME)) {
        	try {
				hostname = settings.getString(HOSTNAME);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Exception caught setting hostname, using default of localhost");
				hostname = "localhost";
			} 
        }
        if (settings.isSetting(CLUSTER_NAME)) {
        	try {
				clustername = settings.getString(CLUSTER_NAME);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Exception caught setting clustername, using default of DarkStarCluster");
				clustername = "DarkStarCluster";
			} 
        }
		try {
			type = settings.getString(MESSAGE_READER_TYPE);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Exception caught attempting to set message reader type, " +
					"defaulting to file");
			type = "file";
		}
		
		processor = new FIXMsgsToRabbit(hostname, clustername, firm);
		
        Iterator<SessionID> sectionIterator = settings.sectionIterator();
		while (sectionIterator.hasNext()) {
			SessionID sessionID = sectionIterator.next();
			String sessionVersion;
			sessionVersion = "FIX.4.2";
			sessionVersions.put(sessionID, sessionVersion);
			String sourceLocation = settings.getString(sessionID, MESSAGE_READER_SOURCE);
			InputStream source = new FileInputStream(sourceLocation);
			MessageReader reader = MessageReaderFactory.getReader(type, source, this);
			sessionReaders.put(sessionID, reader);
		}

	}

	public void onCreate(SessionID sessionID) {
		System.out.println("Session " + sessionID + " created");
	}

	public void onLogon(SessionID sessionID) {
		System.out.println("Session " + sessionID + " logged on");
		Session session = Session.lookupSession(sessionID);
		MessageReader reader = sessionReaders.get(sessionID);
		if (session != null && reader != null) {
			startMessageReader(session, reader);
		}
	}

	public void onLogout(SessionID sessionID) {
		System.out.println("Session " + sessionID + " logged off");
	}

	private void startMessageReader(Session session, MessageReader reader) {
		reader.setSession(session);
		new Thread(reader).start();
	}
	
	@Override
	public void sendMessage(Session session, Message message) {
		session.send(message);
	}

	public void toAdmin(quickfix.Message message, SessionID sessionID) {
	}

	public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
	}

	public void fromAdmin(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		processor.onMessage(message, sessionID);
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getClustername() {
		return clustername;
	}

	public void setClustername(String clustername) {
		this.clustername = clustername;
	}

}
