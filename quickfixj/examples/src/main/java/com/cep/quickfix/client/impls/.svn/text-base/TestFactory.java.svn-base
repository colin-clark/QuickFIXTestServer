package com.cep.quickfix.client.impls;

import java.util.Date;

import quickfix.Message;
import quickfix.Session;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;

public class TestFactory {
	public static final String DUPLICATIVE_TRADE = "DuplicativeTrade";
	public static final String MARKING_CLOSE = "MarkingClose";
	public static final String MARKING_OPEN = "MarkingOpen";
	public static final String MIX_LOT = "MixLot";
	public static final String MOC_LOC = "MocLoc";
	public static final String NO_ACK = "NoAck";
	public static final String ODD_LOT = "OddLot";
	public static final String SELL_SHORT = "SellShort";
	public static final String WASH_TRADE = "WashTrade";
	
	private static int id = 0;
	
	public static void generateTest(String nextTest, Session session) throws Exception {
		if (nextTest.equals(DUPLICATIVE_TRADE)) {
			nextDuplicativeTrade(session);
		} else if (nextTest.equals(MARKING_CLOSE)) {
			nextMarkingClose(session);
		} else if (nextTest.equals(MARKING_OPEN)) {
			nextMarkingOpen(session);
		} else if (nextTest.equals(MIX_LOT)) {
			nextMixLot(session);
		} else if (nextTest.equals(MOC_LOC)) {
			nextMocLoc(session);
		} else if (nextTest.equals(NO_ACK)) {
			nextNoAck(session);
		} else if (nextTest.equals(ODD_LOT)) {
			nextOddLot(session);
		} else if (nextTest.equals(SELL_SHORT)) {
			nextSellShort(session);
		} else if (nextTest.equals(WASH_TRADE)) {
			nextWashTrade(session);
		} else {
			throw new Exception("Invalid Test: " + nextTest);
		}
	}

	private static void nextWashTrade(Session session) {
		session.send(washTradeOrder1());
		session.send(washTradeOrder2());
	}

	private static void nextSellShort(Session session) {
		session.send(sellShortOrder());
	}

	private static void nextOddLot(Session session) {
		session.send(oddLotOrder());
	}

	private static void nextNoAck(Session session) {
		session.send(noAckOrder());		
	}

	private static void nextMocLoc(Session session) {
		session.send(mocLocOrder());
	}

	private static void nextMixLot(Session session) {
		session.send(mixLotOrder1());
		session.send(mixLotOrder2());
	}

	private static void nextMarkingOpen(Session session) {
		session.send(markingOpenOrder());
	}

	private static void nextMarkingClose(Session session) {
		session.send(markingCloseOrder());
	}

	private static void nextDuplicativeTrade(Session session) {
		session.send(duplicativeTradeOrder());
		session.send(duplicativeTradeOrder());
	}

	private static Message noAckOrder() {
		return get42('1', "NACK", '1', new Date(), '1', 100.0, '1', "BrokerA");
	}

	public static quickfix.fix42.NewOrderSingle duplicativeTradeOrder() {
		return get42('1', "DUP", '1', new Date(), '1', 100.0, '1', "BrokerA");
	}
	
	public static quickfix.fix42.NewOrderSingle mixLotOrder1() {
		return get42('1', "MIX", '1', new Date(), '1', 99.0, '1', "BrokerA");
	}
	
	public static quickfix.fix42.NewOrderSingle mixLotOrder2() {
		return get42('1', "MIX", '2', new Date(), '1', 100.0, '1', "BrokerA");
	}
	
	private static Message oddLotOrder() {
		return get42('1', "ODD", '1', new Date(), '1', 1.0, '1', "BrokerA");
	}
	
	private static Message mocLocOrder() {
		return get42('1', "MOC", '1', new Date(System.currentTimeMillis()+86400000), '5', 1.0, '1', "BrokerA");
	}
	
	private static Message washTradeOrder2() {
		return get42('1', "WASH", '2', new Date(), '1', 100.0, '1', "BrokerA");
	}

	private static Message washTradeOrder1() {
		return get42('1', "WASH", '1', new Date(), '1', 100.0, '1', "BrokerA");
	}
	
	private static Message sellShortOrder() {
		return get42('1', "SHRT", '5', new Date(), '1', 100.0, '1', "BadBroker");
	}
	
	private static Message markingOpenOrder() {
		return get42('1', "MTO", '1', new Date(System.currentTimeMillis()-43200000), '1', 100.0, '1', "BrokerA");
	}

	private static Message markingCloseOrder() {
		return get42('1', "MTC", '1', new Date(System.currentTimeMillis()+86400000), '1', 100.0, '1', "BrokerA");
	}

	public static synchronized quickfix.fix42.NewOrderSingle get42(char handlInst, 
			String symbol, char side, Date transactTime, char ordType, double quantity, 
			char tif, String account) {
		quickfix.fix42.NewOrderSingle newOrderSingle = null;
		newOrderSingle = new quickfix.fix42.NewOrderSingle(
				new ClOrdID(newClOrdId()), 
				new HandlInst(handlInst), 
				new Symbol(symbol),
				new Side(side), 
				new TransactTime(transactTime), 
				new OrdType(ordType)
		);
		newOrderSingle.set(new OrderQty(quantity));
		newOrderSingle.set(new TimeInForce(tif));
		newOrderSingle.set(new Account(account));
		return newOrderSingle;
	}
	
	public static String newClOrdId() {
		return String.valueOf(id++);
	}

}
