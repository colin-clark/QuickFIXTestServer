package com.cep.quickfix.client.impls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

import com.cep.quickfix.client.interfaces.MessageProcessor;

public class QFJClientApplication implements Application {
	
	// static constants
	public static final String HOSTNAME = "hostname";
	public static final String CLUSTER_NAME = "clustername";
	public static final String RATE = "Rate";
	public static final String SYMBOLS = "Symbols";

	private MessageProcessor processor;
	private ArrayList<String> symbols = new ArrayList<String>();
	private HashMap<SessionID, String> sessionVersions = new HashMap<SessionID, String>();
	private long rate;
	private static int id = 0;
	private Random generator;
	private static final GregorianCalendar baseCalendar = new GregorianCalendar();
	private static GregorianCalendar postCloseCalendar = new GregorianCalendar();
	private static GregorianCalendar postOpenCalendar = new GregorianCalendar();
	private boolean mixLotOnly = false;
	private boolean sellShortOnly = false;
	private boolean washTradeOnly = false;
	private boolean generateNoExceptions = false;
	private boolean insiderTrading = true;
	private boolean insiderTradingOnly = false;
	private String insiderTradingSymbol = "CEP";
	
	private boolean oyster = false;
	private boolean testWashSale = true;
	private boolean testOrderSpoofing = true;
	private boolean testLayering = true;
	
	// number of seconds between each insider trade
	private long insiderTradingFrequency = 60; 
	private long insiderTradingSize = 2000000;
	private long delay = 5000;
	
	private String hostname = "localhost";
	private String clustername = "DarkStarCluster";

	public QFJClientApplication(SessionSettings settings) {
		System.out.println("Initializing Application");
		setCalendarTimes();
		String firm = "CEP";
		if (settings.isSetting("Firm")) {
			try {
				firm = settings.getString("Firm");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		oyster = settings.isSetting("oyster");
        if (settings.isSetting(HOSTNAME)) {
        	try {
				hostname = settings.getString(HOSTNAME);
			} catch (Exception e) {
				System.err.println("Exception caught setting hostname, using default of localhost");
				hostname = "localhost";
			} 
        }
        if (settings.isSetting(CLUSTER_NAME)) {
        	try {
				clustername = settings.getString(CLUSTER_NAME);
			} catch (Exception e) {
				System.err.println("Exception caught setting clustername, using default of DarkStarCluster");
				clustername = "DarkStarCluster";
			} 
        }
        long batch_size = 1000;
        if (settings.isSetting("batch_size")) {
        	try {
        		batch_size = settings.getLong("batch_size");
        	} catch (Exception e) {
        		System.err.println("Exception caught setting batch size, using default of 1000");
        		e.printStackTrace();
        	}
        }

		System.out.println("Initializing Processor");
		processor = new FIXMsgsToRabbit(hostname, clustername, firm, batch_size);
		System.out.println("Initializing Sessions");
		Iterator<SessionID> sectionIterator = settings.sectionIterator();
		generator = new Random();
		while (sectionIterator.hasNext()) {
			SessionID sessionID = sectionIterator.next();
			String sessionVersion;
			sessionVersion = "FIX.4.2";
			sessionVersions.put(sessionID, sessionVersion);
		}
		System.out.println("Initializing Global parameters");
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
		if (!oyster) {
			System.out.println("Getting Test Types");
			try {
				if (settings.isSetting("MixLotOnly")) {
					mixLotOnly = settings.getBool("MixLotOnly");
					if (mixLotOnly) System.out.println("Sending Mix Lots only");
				}
				if (settings.isSetting("NoExceptions")) {
					generateNoExceptions = true;
				}
				if (settings.isSetting("SellShortOnly")) {
					sellShortOnly = settings.getBool("SellShortOnly");
					if (sellShortOnly) System.out.println("Sending Short Sales only");
				}
				if (settings.isSetting("WashTradeOnly")) {
					washTradeOnly = settings.getBool("WashTradeOnly");
					if (washTradeOnly) System.out.println("Sending Wash Trades only");
				}
				if (settings.isSetting("delay")) {
					delay = settings.getLong("delay");
					System.out.println("Using a delay of " + delay);
				}
				if (settings.isSetting("insiderTrading")) {
					insiderTrading = settings.getBool("insiderTrading");
					if (insiderTrading) System.out.println("Using Insider Trading");
				}
				if (settings.isSetting("insiderTradingSymbol")) {
					insiderTradingSymbol = settings.getString("insiderTradingSymbol");
					System.out.println("Insider Trading with Symbol " + insiderTradingSymbol);
				}
				if (settings.isSetting("insiderTradingFrequency")) {
					insiderTradingFrequency = settings.getLong("insiderTradingFrequency");
					System.out.println("Scheduling 1 Insider Trade every " + insiderTradingFrequency + " seconds");
				}
				if (settings.isSetting("insiderTradingSize")) {
					insiderTradingSize = settings.getLong("insiderTradingSize");
					System.out.println("Using a size of " + insiderTradingSize + " for Insider Trades");
				}
				if (settings.isSetting("insiderTradingOnly")) {
					insiderTradingOnly = settings.getBool("insiderTradingOnly");
					if (insiderTradingOnly) System.out.println("Sending Insider Trades Only");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Testing Oyster Exception Types" + 
					(testWashSale ? " WashSale" : " ") + 
					(testOrderSpoofing ? ", OrderSpoofing" : "") + 
					(testLayering ? ", Layering" : ""));
		}
	}

	private void setCalendarTimes() {
		postCloseCalendar.clear();
		postOpenCalendar.clear();
		postCloseCalendar.set(baseCalendar.get(Calendar.YEAR), baseCalendar.get(Calendar.MONTH), baseCalendar.get(Calendar.DAY_OF_MONTH), 15, 1, 1);
		postOpenCalendar.set(baseCalendar.get(Calendar.YEAR), baseCalendar.get(Calendar.MONTH), baseCalendar.get(Calendar.DAY_OF_MONTH), 9, 1, 1);
	}

	public void onCreate(SessionID sessionID) {
	}

	public void onLogon(SessionID sessionID) {
		System.out.println("Session " + sessionID + " logged on");
		Session session = Session.lookupSession(sessionID);
		if (session != null) {
			startSenderThread(session);
		}
	}

	private void startSenderThread(final Session session) {
		if (oyster) {
			startOysterThread(session);
		} else {
			new Thread() {
				public void run() {
					if (insiderTrading) {
						scheduleInsiderOrders(session);
					}
					if (!mixLotOnly && !sellShortOnly && !washTradeOnly && !insiderTradingOnly) {
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
						}
					} else if (mixLotOnly){
						while (true) {
							session.send(getMixLot1());
							if (generateNoExceptions) {
								try { 
									Thread.sleep(10000);
								} catch (Exception e2) {
									e2.printStackTrace();
							    }
							}
							session.send(getMixLot2());
							try {
								Thread.sleep(delay);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else if (sellShortOnly) {
						while (true) {
							session.send(getSellShort());
							try {
								Thread.sleep(delay);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}					
					} else if (washTradeOnly) {
						while (true) {
							session.send(getWashTrade1());
							try {
								Thread.sleep(15000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							session.send(getWashTrade2());
							try {
								Thread.sleep(15000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}					
					}
				} 
				
				class generateInsiderOrder implements Runnable {
					private Session session;
					
					public generateInsiderOrder(Session session) {
						this.session = session;
					}
					
	                public void run() { 
	                	session.send(createInsiderOrder(sessionVersions.get(session.getSessionID()))); 
	                }
	
					private Message createInsiderOrder(String string) {
						newOrderSingle = new quickfix.fix42.NewOrderSingle(
								new ClOrdID(newClOrdId()), 
								new HandlInst('1'), 
								new Symbol(insiderTradingSymbol),
								new Side(getSide()), 
								new TransactTime(getTransactTime()), 
								new OrdType(getOrdType()));
						newOrderSingle.set(new OrderQty(insiderTradingSize));
						newOrderSingle.set(new Price(getPrice()));
						newOrderSingle.set(new TimeInForce('6'));
						return newOrderSingle;
					}
	            };
	
				private void scheduleInsiderOrders(Session session) {
					ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
					scheduler.scheduleAtFixedRate(new generateInsiderOrder(session), 1, insiderTradingFrequency, TimeUnit.SECONDS);
				}
	
				private Message createOrder(String beginString) {
					return get42();
				}
			}.start();
		}
	}

	private void startOysterThread(final Session session) {
		new Thread() {
			public void run() {
				System.out.println("Starting Message Generation Thread");
				int numSent;
				while (true) {
					System.out.println("Setting message count for this second to 0");
					numSent = 0;
					long baseTime = System.currentTimeMillis();
					while ((System.currentTimeMillis() - baseTime) <= 1000) {
						if (numSent < rate) {
							if (testWashSale){
								System.out.println("Generating messages for Wash Sale");
								generateWashSale(session);
								numSent += 4;
							}
							if (testOrderSpoofing) {
								System.out.println("Generating messages for Order Spoofing");
								generateOrderSpoofing(session);
								numSent += 3;
							} 
							if (testLayering) {
								System.out.println("Generating messages for Layering");
								generateLayering(session);
								numSent += 4;
							} 
						} else {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}

			private void generateLayering(Session session) {
				quickfix.fix42.NewOrderSingle layer1 = newLayerOrder('1');
				quickfix.fix42.OrderCancelRequest cancel1 = newCancel(layer1);
				session.send(layer1);
				session.send(cancel1);
				quickfix.fix42.NewOrderSingle layer2 = newLayerOrder('2');
				quickfix.fix42.OrderCancelRequest cancel2 = newCancel(layer2);
				session.send(cancel2);		
			}

			private void generateOrderSpoofing(Session session) {
				quickfix.fix42.NewOrderSingle spoof = newSpoofOrder('1');
				quickfix.fix42.OrderCancelRequest cancel = newCancel(spoof);
				session.send(spoof);
				session.send(cancel);
				session.send(newSpoofOrder('2'));
			}

			private void generateWashSale(Session session) {
				session.send(newWashOrder('1'));
				session.send(newWashOrder('2'));
				session.send(newWashOrder('2'));
				session.send(newWashOrder('1'));
			}
			
			private quickfix.fix42.NewOrderSingle newWashOrder(char side) {
				quickfix.fix42.NewOrderSingle order = new quickfix.fix42.NewOrderSingle(
						new ClOrdID(newClOrdId()), 
						new HandlInst('1'), 
						new Symbol("WASH"),
						new Side(side), 
						new TransactTime(new Date()), 
						new OrdType('1'));
				order.set(new OrderQty(200.0));
				order.set(new TimeInForce('1'));
				return order;
			}
			
			private quickfix.fix42.NewOrderSingle newSpoofOrder(char side) {
				quickfix.fix42.NewOrderSingle order = new quickfix.fix42.NewOrderSingle(
						new ClOrdID(newClOrdId()), 
						new HandlInst('1'), 
						new Symbol("SPOOF"),
						new Side(side), 
						new TransactTime(new Date()), 
						new OrdType('1'));
				order.set(new OrderQty(200.0));
				order.set(new TimeInForce('1'));
				return order;
			}
			
			private quickfix.fix42.NewOrderSingle newLayerOrder(char side) {
				quickfix.fix42.NewOrderSingle order = new quickfix.fix42.NewOrderSingle(
						new ClOrdID(newClOrdId()), 
						new HandlInst('1'), 
						new Symbol("LAYER"),
						new Side(side), 
						new TransactTime(new Date()), 
						new OrdType('1'));
				order.set(new OrderQty(200.0));
				order.set(new TimeInForce('1'));
				return order;
			}
			
			private OrderCancelRequest newCancel(NewOrderSingle order) {
				OrderCancelRequest cancel = new OrderCancelRequest();
				try {
					cancel.setString(OrigClOrdID.FIELD, order.getString(ClOrdID.FIELD));
					cancel.set(order.getClOrdID());
					cancel.set(order.getSymbol());
					cancel.set(order.getSide());
					cancel.set(order.getOrderQty());
					cancel.set(new TransactTime(new Date()));
				} catch (FieldNotFound e) {
					e.printStackTrace();
				}
				return cancel;
			}
			
		}.start();
	}

	public void onLogout(SessionID sessionID) {
		System.out.println("Session " + sessionID + " logged off");
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

	public char getOrdType() {
		return (id % 2 == 0) ? '1' : '5';
	}
	
	public Date getTransactTime() {
		return (id % 2 == 0) ? new Date() : postCloseCalendar.getTime();
	}
	
	public double getQuantity() {
		int qty = generator.nextInt(10000);
		if (qty < 100) {
			qty = 100;
		}
		qty = qty / 100 * 100;
		return qty;
	}
	
	public double getPrice() {
		return (generator.nextDouble() * 10);
	}

	quickfix.fix42.NewOrderSingle newOrderSingle = null;
	static int tif = 0;

	public quickfix.fix42.NewOrderSingle get42() {
		newOrderSingle = new quickfix.fix42.NewOrderSingle(new ClOrdID(
				newClOrdId()), new HandlInst('1'), new Symbol(getSymbol()),
				new Side(getSide()), new TransactTime(getTransactTime()), new OrdType(getOrdType()));
		newOrderSingle.set(new OrderQty(getQuantity()));
		newOrderSingle.set(new Price(getPrice()));
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
	
	public quickfix.fix42.NewOrderSingle getMixLot1() {
		newOrderSingle = new quickfix.fix42.NewOrderSingle(
				new ClOrdID(newClOrdId()), 
				new HandlInst('1'), 
				new Symbol("A"),
				new Side('1'), 
				new TransactTime(new Date()), 
				new OrdType('1'));
		newOrderSingle.set(new OrderQty(50.0));
		newOrderSingle.set(new TimeInForce('1'));
		return newOrderSingle;
	}
	
	public quickfix.fix42.NewOrderSingle getMixLot2() {
		newOrderSingle = new quickfix.fix42.NewOrderSingle(
				new ClOrdID(newClOrdId()), 
				new HandlInst('1'), 
				new Symbol("A"),
				new Side('2'), 
				new TransactTime(new Date()), 
				new OrdType('1'));
		newOrderSingle.set(new OrderQty(200.0));
		newOrderSingle.set(new TimeInForce('1'));
		return newOrderSingle;
	}
	
	public quickfix.fix42.NewOrderSingle getSellShort() {
		newOrderSingle = new quickfix.fix42.NewOrderSingle(
				new ClOrdID(newClOrdId()), 
				new HandlInst('1'), 
				new Symbol("A"),
				new Side('5'), 
				new TransactTime(new Date()), 
				new OrdType('1'));
		newOrderSingle.set(new OrderQty(200.0));
		newOrderSingle.set(new TimeInForce('1'));
		newOrderSingle.set(new Account("BrokerB"));
		return newOrderSingle;
	}
	
	public quickfix.fix42.NewOrderSingle getWashTrade1() {
		newOrderSingle = new quickfix.fix42.NewOrderSingle(
				new ClOrdID(newClOrdId()), 
				new HandlInst('1'), 
				new Symbol("A"),
				new Side('1'), 
				new TransactTime(new Date()), 
				new OrdType('1'));
		newOrderSingle.set(new OrderQty(200.0));
		newOrderSingle.set(new TimeInForce('1'));
		return newOrderSingle;
	}
	
	public quickfix.fix42.NewOrderSingle getWashTrade2() {
		newOrderSingle = new quickfix.fix42.NewOrderSingle(
				new ClOrdID(newClOrdId()), 
				new HandlInst('1'), 
				new Symbol("A"),
				new Side('2'), 
				new TransactTime(new Date()), 
				new OrdType('1'));
		newOrderSingle.set(new OrderQty(200.0));
		newOrderSingle.set(new TimeInForce('1'));
		return newOrderSingle;
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
