package com.cep.quickfix.client.impls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;

import com.cep.quickfix.client.interfaces.MessageProcessor;

public class TestSpecificQFJClientApplication implements Application {

	private static Logger log = LoggerFactory.getLogger(TestSpecificQFJClientApplication.class);
	private MessageProcessor processor;
	private ArrayList<String> symbols = new ArrayList<String>();
	private HashMap<SessionID, String> sessionVersions = new HashMap<SessionID, String>();
	private long interval;
	private static final String INTERVAL = "Interval";
	private static final String TESTS = "Tests";
	private static int id = 0;
	private Random generator;
	private ArrayList<String> tests = new ArrayList<String>();
	private static ScheduledExecutorService scheduler;

	public TestSpecificQFJClientApplication(SessionSettings settings) throws Exception {
		processor = new FIXMsgsToRabbit();
		Iterator<SessionID> sectionIterator = settings.sectionIterator();
		generator = new Random();
		int noSessions = 0;
		while (sectionIterator.hasNext()) {
			SessionID sessionID = sectionIterator.next();
			String sessionVersion;
			try {
				sessionVersion = settings.getString(SessionSettings.BEGINSTRING);
			} catch (Exception e) {
				sessionVersion = "FIX.4.2";
			}
			sessionVersions.put(sessionID, sessionVersion);
			noSessions++;
		}
		scheduler = Executors.newScheduledThreadPool(noSessions);
		try {
			interval = settings.getLong(INTERVAL);
		} catch (Exception e) {
			interval = 1000;
		}
		if (settings.isSetting(TESTS)) {
			List<String> Types = Arrays.asList(settings.getString(TESTS).trim().split(","));
			tests.addAll(Types);
			if (tests.isEmpty()) {
				throw new Exception("No Tests defined");
			}
		}
	}

	public void onCreate(SessionID sessionID) {
	}

	public void onLogon(SessionID sessionID) {
		log.info("Session " + sessionID + " logged on");
		Session session = Session.lookupSession(sessionID);
		if (session != null) {
			TestRunner testRunner = new TestRunner(session);
			scheduler.scheduleAtFixedRate(testRunner, 0, interval, TimeUnit.MILLISECONDS);
		}
	}
	
	class TestRunner implements Runnable {
		private Session session;
        
		public TestRunner(Session session) {
			this.session = session;
		}

		public void run() { 
			int randomIndex = generator.nextInt(symbols.size());
			String nextTest = tests.get(randomIndex);
			try {
				TestFactory.generateTest(nextTest, session);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		
    };

	public void onLogout(SessionID sessionID) {
		log.info("Session " + sessionID + " logged off");
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

	public String newClOrdId() {
		return String.valueOf(id++);
	}

	quickfix.fix42.NewOrderSingle newOrderSingle = null;
	static int tif = 0;

}
