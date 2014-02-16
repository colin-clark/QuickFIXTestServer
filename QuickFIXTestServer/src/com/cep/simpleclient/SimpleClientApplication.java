package com.cep.simpleclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import quickfix.SessionSettings;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;

public class SimpleClientApplication {
	
	// static constants
	public static final String HOSTNAME = "hostname";
	public static final String CLUSTER_NAME = "clustername";
	public static final String RATE = "Rate";
	public static final String SYMBOLS = "Symbols";

	private FIXMsgsToDarkstar processor;
	private ArrayList<String> symbols = new ArrayList<String>();
	private long rate;
	private static int id = 0;
	private Random generator;
	private String hostname = "localhost";
	private String clustername = "DarkStarCluster";
	private long maxMessages = 100000;

	public SimpleClientApplication(SessionSettings settings) {
		String firm = "CEP";
		if (settings.isSetting("Firm")) {
			try {
				firm = settings.getString("Firm");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
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
        if (settings.isSetting("max_messages")) {
        	try {
        		maxMessages = settings.getLong("max_messages");
        	} catch (Exception e) {
        		System.err.println("Exception caught setting max messages, using default of 100000");
        		e.printStackTrace();
        	}
        }
		processor = new FIXMsgsToDarkstar(hostname, clustername, firm, batch_size);
		generator = new Random();
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

	void start() {
		long start = System.currentTimeMillis();
		long totalSent = 0;
		int numSent = 0;
		while (totalSent < maxMessages) {
			numSent = 0;
			long baseTime = System.currentTimeMillis();
			while ((System.currentTimeMillis() - baseTime) <= 1000) {
				if (totalSent == maxMessages) {
					break;
				}
				if (numSent < rate) {
					processor.onMessage(get42());
					totalSent++;
					numSent++;
				} else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		long end = System.currentTimeMillis();
		long runTime = end - start;
		double trueRate = ((double)maxMessages /(double)runTime) * 1000;
		System.out.println("*******************************************************************************************");
		System.out.println(totalSent + " total messages sent to DarkStar in " + runTime + " ms for a true message rate of " + trueRate);
		System.out.println("******************************************************************************************");
		Main.shutdownLatch.countDown();
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
		return new Date();
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
		newOrderSingle = new quickfix.fix42.NewOrderSingle(
				new ClOrdID(newClOrdId()), 
				new HandlInst('1'), 
				new Symbol(getSymbol()),
				new Side(getSide()), 
				new TransactTime(getTransactTime()), 
				new OrdType(getOrdType()));
		newOrderSingle.set(new OrderQty(getQuantity()));
		newOrderSingle.set(new Price(getPrice()));
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
