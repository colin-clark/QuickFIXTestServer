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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;

import com.cep.quickfix.client.interfaces.MessageProcessor;

public class QFJClientApplication implements Application {

	private static Logger log = LoggerFactory
			.getLogger(QFJClientApplication.class);
	private MessageProcessor processor;
	private ArrayList<String> symbols = new ArrayList<String>();
	private HashMap<SessionID, String> sessionVersions = new HashMap<SessionID, String>();
	private long rate;
	private static final String RATE = "Rate";
	private static final String SYMBOLS = "Symbols";
	private static int id = 0;
	private Random generator;

	public QFJClientApplication(SessionSettings settings) {
		processor = new FIXMsgsToRabbit();
		Iterator<SessionID> sectionIterator = settings.sectionIterator();
		generator = new Random();
		while (sectionIterator.hasNext()) {
			SessionID sessionID = sectionIterator.next();
			String sessionVersion;
			try {
				sessionVersion = settings.getString(SessionSettings.BEGINSTRING);
			} catch (Exception e) {
				sessionVersion = "FIX.4.2";
			}
			sessionVersions.put(sessionID, sessionVersion);
		}
		try {
			rate = settings.getLong(RATE);
		} catch (Exception e) {
			rate = 1000;
		}
		try {
			if (settings.isSetting(SYMBOLS)) {
				List<String> Types = Arrays.asList(settings.getString(SYMBOLS).trim().split("\\s*,\\s*"));
				symbols.addAll(Types);
			} else {
				symbols.add("IBM");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onCreate(SessionID sessionID) {
	}

	public void onLogon(SessionID sessionID) {
		log.info("Session " + sessionID + " logged on");
		Session session = Session.lookupSession(sessionID);
		if (session != null) {
			startSenderThread(session);
		}
	}

	private void startSenderThread(final Session session) {
		new Thread() {
			public void run() {
				int numSent;
				while (true) {
					numSent = 0;
					long baseTime = System.currentTimeMillis();
					while ((System.currentTimeMillis() - baseTime) <= 1000) {
						if (numSent++ < rate) {
							session.send(createOrder(sessionVersions.get(session.getSessionID())));
						} else {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
							}
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			private Message createOrder(String beginString) {
				return get42();
			}
		}.start();
	}

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

	public String getSymbol() {
		int randomIndex = generator.nextInt(symbols.size());
		return symbols.get(randomIndex);
	}

	public char getSide() {
		return (id % 2 == 0) ? '1' : '2';
	}

	public double getQuantity() {
		int qty = generator.nextInt(10000);
		if (qty < 100) {
			qty = 100;
		}
		qty = qty / 100 * 100;
		return qty;
	}

	quickfix.fix42.NewOrderSingle newOrderSingle = null;
	static int tif = 0;

	public quickfix.fix42.NewOrderSingle get42() {
		newOrderSingle = new quickfix.fix42.NewOrderSingle(new ClOrdID(
				newClOrdId()), new HandlInst('1'), new Symbol(getSymbol()),
				new Side(getSide()), new TransactTime(), new OrdType('1'));
		newOrderSingle.set(new OrderQty(getQuantity()));
		//newOrderSingle.set(new TradeDate(DateUtils.now()));
		tif = tif + 1;
		if ((tif % 20) != 0) {
			newOrderSingle.set(new TimeInForce('1'));
		} else {
			newOrderSingle.set(new TimeInForce('6'));
		}
		if ((tif % 23) == 0) {
			newOrderSingle.set(new Side('5'));
			if (tif % 2 == 0) {
				newOrderSingle.set(new Account("BadBroker"));
			} else {
				newOrderSingle.set(new Account("BrokerA"));
			}
		}
		return newOrderSingle;
	}
}
