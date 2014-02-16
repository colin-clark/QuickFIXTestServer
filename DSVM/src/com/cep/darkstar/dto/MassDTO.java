package com.cep.darkstar.dto;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.node.CEPEventsSendRunnable;

public class MassDTO extends AbstractDTO implements IDTO {
	static Logger logger = Logger.getLogger("com.cep.darkstar.DTO.MassDTO");
	private LinkedBlockingQueue<EventObject> dsar_buy_window = new LinkedBlockingQueue<EventObject>();
	private LinkedBlockingQueue<EventObject> dsar_sell_window = new LinkedBlockingQueue<EventObject>();
	private HashMap<String,EventObject[]> apar_children = new HashMap<String,EventObject[]>();
	
	final static String IMPRESSIONS = "Impressions";
	final static String GUID = "guid";
	final static String SIDE = "Side";
	final static String SELL = "sell";
	final static String BUY = "buy";
	final static String FUNCTION = "function";
	
	/*
	 * Standard DTO Function Call - Sends and Saves and in this case attempts to match Orders.
	 */
	public void doFunction(EventObject e) {
		timestamp(e);
		e.remove(FUNCTION);
		boolean persisted = false;
		if (e.has(PERSISTED)) {
			try {
				persisted = e.getBoolean(PERSISTED);
			} catch (JSONException e1) {
				// This one we can safely swallow
			}
		}
		if (!persisted) {
			try {
				e.put(PERSISTED, true);
				doMatching(e);
				doSave(e);
			} catch (Exception e3) {
				logger.error("Save of event " + e + " interrupted", e3);
			}
		} else {
			try {
				doMatching(e);
			} catch (JSONException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}
	
	/*
	 * Send the incoming order into the StreamingBook Stream, then sort buys from sells
	 */
	private void doMatching(EventObject e) throws JSONException {
		// Copy the event so changes don't change the event in the order stream
		EventObject e2 = new EventObject(e.toString());
		
		// Send the order(s) into the engine
		CEPEventsSendRunnable.handleEventObject(e);
		
		// Do the matching
		if (e.getString(SIDE).equalsIgnoreCase(SELL)) {
			handleNewSell(e2);
		} else {
			handleNewBuy(e2);
		}
	}
	
	/*
	 * Handle new Sell Order:
	 * 	- Explode the APAR and create child orders
	 * 	- Match on Booked DSARs
	 * 	- Match on Booked APARs
	 * 	- Add any remaining Impressions to the Sell side Book
	 */
	private void handleNewSell(EventObject sellOrder) throws JSONException {
		if (logger.isDebugEnabled()) {
			logger.debug("MASS DTO received new sell order " + sellOrder.getString(GUID) + ": " + sellOrder);
		}
		Iterator<EventObject> buy_iterator = dsar_buy_window.iterator();
		EventObject buyOrder = null;
		EventObject[] apars = generateAparOrders(sellOrder);
		
		while ( (sellOrder.getLong(IMPRESSIONS) > 0) && (buy_iterator.hasNext()) ) {
			buyOrder = buy_iterator.next();
			if (isDsarMatch(sellOrder, buyOrder)) {
				if (logger.isDebugEnabled()) {
					logger.debug("MASS DTO matched new sell order " + sellOrder.getString(GUID) + 
							" DSAR with booked buy order: " + buyOrder + " DSAR");
				}
				int index = aparMatch(buyOrder, apars);
				if (index != -1) {
					handleSellMatch(sellOrder, buyOrder, apars, index);
				}
			}
		}
		if (sellOrder.getLong(IMPRESSIONS) > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("Adding sell order to book: " + sellOrder);
			}
			dsar_sell_window.add(sellOrder);
			apar_children.put(sellOrder.getString("guid"), apars);
		}
	}
	
	/*
	 * Handle new Buy Order:
	 * 	- Match on Booked DSARs
	 * 	- Match on Booked APARs
	 * 	- Add any remaining Impressions to the Buy side Book
	 */
	private void handleNewBuy(EventObject buyOrder) throws JSONException {
		if (logger.isDebugEnabled()) {
			logger.debug("MASS DTO received new buy order " + buyOrder.getString(GUID) + ": " + buyOrder);
		}
		Iterator<EventObject> sell_iterator = dsar_sell_window.iterator();
		EventObject sellOrder = null;
		while ( (buyOrder.getLong(IMPRESSIONS) > 0) && (sell_iterator.hasNext()) ) {
			sellOrder = sell_iterator.next();
			if (isDsarMatch(sellOrder, buyOrder)) {
				if (logger.isDebugEnabled()) {
					logger.debug("MASS DTO matched new buy order " + buyOrder.getString(GUID) + 
							" DSAR with booked sell order: " + sellOrder + " DSAR");
				}
				EventObject[] apars = getChildOrders(sellOrder.getString(GUID));
				if (apars != null) {
					int index = aparMatch(buyOrder, apars);
					if (index != -1) {
						handleBuyMatch(sellOrder, buyOrder, apars, index);
					}
				}
			}
		}
		if (buyOrder.getLong(IMPRESSIONS) > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("Adding buy order to book: " + buyOrder);
			}
			dsar_buy_window.add(buyOrder);
		}
	}
	
	/*
	 * Send the trade event with the matching Orders
	 * Delete from Order Book where appropriate
	 * Decrement Impressions where appropriate
	 */
	private void handleSellMatch(EventObject sellOrder, EventObject buyOrder, EventObject[] apars, int index) throws JSONException {
		long sell_quantity = sellOrder.getLong(IMPRESSIONS);
		long buy_quantity = buyOrder.getLong(IMPRESSIONS);
		EventObject sell = apars[index];
		sell.put(IMPRESSIONS, sellOrder.getLong(IMPRESSIONS));
		
		if (sell_quantity > buy_quantity) {
			sendMassTrade(sell, buyOrder, buy_quantity);
			updateBook(apars, buyOrder, buy_quantity);
			dsar_buy_window.remove(buyOrder);
			sellOrder.put(IMPRESSIONS, (sell_quantity - buy_quantity));
		} else if (sell_quantity < buy_quantity) {
			sendMassTrade(sell, buyOrder, sell_quantity);
			updateBook(apars, buyOrder, sell_quantity);
			buyOrder.put(IMPRESSIONS, (buy_quantity - sell_quantity));
			sellOrder.put(IMPRESSIONS, 0L);
		} else { /* (sell_quantity == buy_quantity) */
			sendMassTrade(sell, buyOrder, sell_quantity);
			updateBook(apars, buyOrder, sell_quantity);
			dsar_buy_window.remove(buyOrder);
			sellOrder.put(IMPRESSIONS, 0L);
		}	
	}
	
	private void updateBook(EventObject[] apars, EventObject buyOrder, long quantity) throws JSONException {
		sendBookDelete(buyOrder, quantity);
		for (int i = 0; i < apars.length; i++) {
			if (apars[i] != null) {
				sendBookDelete(apars[i], quantity);
			}
		}
	}

	private void sendBookDelete(EventObject order, long quantity) throws JSONException {
		EventObject deleteEvent = new EventObject();
		deleteEvent.put("Publisher", order.getString("Publisher"));
		deleteEvent.put("WebSite", order.getString("WebSite"));
		deleteEvent.put("Page", order.getString("Page"));
		deleteEvent.put("ScreenLocation", order.getString("ScreenLocation"));
		deleteEvent.put("AdUnit", order.getString("AdUnit"));
		deleteEvent.put("AdContext", order.getString("AdContext"));
		deleteEvent.put("Flight", order.getString("Flight"));
		deleteEvent.put("DSARPrice", order.getDouble("DSARPrice"));
		deleteEvent.put("APAR", order.getString("APAR"));
		deleteEvent.put("APARPrice", order.getDouble("APARPrice"));
		deleteEvent.put("Quantity", quantity);
		deleteEvent.put("Side", order.getString("Side"));
	
		deleteEvent.setEventName("consolidated_book_deletes");
		if (logger.isDebugEnabled()) {
			logger.debug("MASS DTO Sending Book Delete Event " + deleteEvent);
		}
		CEPEventsSendRunnable.handleEventObject(deleteEvent);
	}

	/*
	 * Send the trade event with the matching Orders
	 * Delete from Order Book where appropriate
	 * Decrement Impressions where appropriate
	 */
	private void handleBuyMatch(EventObject sellOrder, EventObject buyOrder, EventObject[] apars, int index) throws JSONException {
		long sell_quantity = sellOrder.getLong(IMPRESSIONS);
		long buy_quantity = buyOrder.getLong(IMPRESSIONS);
		EventObject sell = apars[index];
		sell.put(IMPRESSIONS, sellOrder.getLong(IMPRESSIONS));
		
		if (sell_quantity > buy_quantity) {
			sendMassTrade(sell, buyOrder, buy_quantity);
			updateBook(apars, buyOrder, buy_quantity);
			sellOrder.put(IMPRESSIONS, (sell_quantity - buy_quantity));
			buyOrder.put(IMPRESSIONS, 0L);
		} else if (sell_quantity < buy_quantity) {
			sendMassTrade(sell, buyOrder, sell_quantity);
			updateBook(apars, buyOrder, sell_quantity);
			dsar_sell_window.remove(sellOrder);
			apar_children.remove(sellOrder.getString(GUID));
			buyOrder.put(IMPRESSIONS, (buy_quantity - sell_quantity));
		} else { /* (sell_quantity == buy_quantity) */
			sendMassTrade(sellOrder, buyOrder, sell_quantity);
			updateBook(apars, buyOrder, sell_quantity);
			dsar_sell_window.remove(sellOrder);
			apar_children.remove(sellOrder.getString(GUID));
			buyOrder.put(IMPRESSIONS, 0L);
		}
	}
	
	/*
	 * Explode the APAR field into n! Orders, send each order into the StreamingBookbuy
	 */
	private EventObject[] generateAparOrders(EventObject sellOrder) throws JSONException {
		String APAR = sellOrder.getString("APAR");
		EventObject[] orders;
		if (APAR.indexOf(',') != -1) {
			String[] AparTokens = APAR.split(",");
			orders = new EventObject[calculateSize(AparTokens.length)];
			int index = 0;
			for (int i = 0; i < AparTokens.length; i++) {
				String[] arg_i = {AparTokens[i]};
				orders[index++] = createSubOrder(sellOrder, arg_i);
				if (AparTokens.length > 1) {
					for (int j = i+1; j < AparTokens.length; j++) {
						String[] arg_j = {AparTokens[i], AparTokens[j]};
						orders[index++] = createSubOrder(sellOrder, arg_j);
						if (AparTokens.length > 2) {
							for (int k = j+1; k < AparTokens.length; k++) {
								String[] arg_k = {AparTokens[i], AparTokens[j], AparTokens[k]};
								orders[index++] = createSubOrder(sellOrder, arg_k);
							}
						}
					}
				}
			}
		} else {
			orders = new EventObject[1];
			orders[0] = new EventObject(sellOrder.toString());
		}
		return orders;
	}

	private int calculateSize(int n) {
		return (n <= 1) ? 1 : (n * calculateSize(n - 1) + 1);
	}

	/*
	 * Create a new Order with the APAR properties specified. If it's not a 
	 * 	duplicate of the original Sell Order send it in to the StreamingBook.
	 */
	private EventObject createSubOrder(EventObject sellOrder, String[] apars) throws JSONException {
		EventObject e = new EventObject(sellOrder.toString());
		if (!duplicateEntry(sellOrder, apars)) {
			StringBuffer sb_apar = new StringBuffer();
			for (int i = 0; i < apars.length; i++) {
				sb_apar.append(apars[i]);
				if (i != (apars.length - 1)) {
					sb_apar.append(",");
				}
			}
			String apar = sb_apar.toString();
			e.put("APAR", apar);
			if (logger.isDebugEnabled()) {
				logger.debug("Sending new derived APAR " + e);
			}
			EventObject e2 = new EventObject(e.toString());
			CEPEventsSendRunnable.handleEventObject(e2);
		}
		return e;
	}
	
	private HashMap<String,String> getAparMap(String apar) {
		HashMap<String,String> aparMap = new HashMap<String,String>();
		if (apar.indexOf(',') != -1) {
			for (String aparStr : apar.split(",")) {
				String[] keyval = aparStr.split(":");
				aparMap.put(keyval[0], keyval[1]);
			}
		} else {
			String[] keyval = apar.split(":");
			aparMap.put(keyval[0], keyval[1]);
		}
		return aparMap;
	}

	/*
	 * Compare the set of apar properties for the current order with the ones from the original
	 * Sell Order to determine if they are identical.
	 */
	//TODO: Replace the way we're storing APARs to avoid this ridiculous comparison.
	private boolean duplicateEntry(EventObject sellOrder, String[] apars) throws JSONException {
		boolean dup = false;
		if (sellOrder.getString("APAR").indexOf(',') != -1) {
			String[] tokens = sellOrder.getString("APAR").split(",");
			if (tokens.length == apars.length) {
				dup = true;
				for (int i = 0; i < tokens.length; i++) {
					if (!tokens[i].equals(apars[i])) {
						return false;
					}
				}
			}
		} else {
			dup = (sellOrder.getString("APAR").equals(apars[0]));
		}
		return dup;
	}

	private int aparMatch(EventObject buyOrder, EventObject[] orders) throws JSONException {
		if (logger.isDebugEnabled()) {
			logger.debug("Matching buy order " + buyOrder.getString(GUID) + " against APARs");
		}
		HashMap<String,String> buy_apars = getAparMap(buyOrder.getString("APAR"));
		EventObject order;
		for (int i = 0; i < orders.length; i++) {
			order = orders[i];
			HashMap<String,String> sell_apars = getAparMap(order.getString("APAR"));
			
			if (matchingApars(buy_apars, sell_apars) &&
					buyOrder.getDouble("APARPrice") >= order.getDouble("APARPrice")) {
				if (logger.isDebugEnabled()) {
					logger.debug("Matched buy order with APAR " + buy_apars + 
							" with sell order APAR " + order.get("APAR"));
				}
				return i;
			}
		}
		
		return -1;
	}

	private boolean matchingApars(HashMap<String, String> buy_apars, Map<String,String> sellApars) {
		if (logger.isDebugEnabled()) {
			logger.debug("Matching buy Apars of " + buy_apars + " against sell Apars of " + sellApars);
		}
		for (Entry<String, String> kv : buy_apars.entrySet()) {
			if (sellApars.containsKey(kv.getKey())) {
				if (!(kv.getValue().equals(sellApars.get(kv.getKey())))) {
					if (logger.isDebugEnabled()) {
						logger.debug("No APAR match because buy APAR " + kv.getKey() + " value " + kv.getValue() +
								" does not match sell APAR value of " + sellApars.get(kv.getKey()));
					}
					return false;
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("No APAR match because buy APAR " + kv.getKey() + " does not exist for sell APAR");
				}
				return false;
			}
		}		
		return true;
	}

	private EventObject[] getChildOrders(String guid) {
		return apar_children.get(guid);
	}

	private void sendMassTrade(EventObject sellOrder, EventObject buyOrder, long quantity) throws JSONException {
		EventObject tradeEvent = new EventObject();
		tradeEvent.put("Publisher", sellOrder.getString("Publisher"));
		tradeEvent.put("WebSite", sellOrder.getString("WebSite"));
		tradeEvent.put("Page", sellOrder.getString("Page"));
		tradeEvent.put("ScreenLocation", sellOrder.getString("ScreenLocation"));
		tradeEvent.put("AdUnit", sellOrder.getString("AdUnit"));
		tradeEvent.put("AdContext", sellOrder.getString("AdContext"));
		tradeEvent.put("Sell_DSARPrice", sellOrder.getDouble("DSARPrice"));
		tradeEvent.put("Buy_DSARPrice", buyOrder.getDouble("DSARPrice"));
		tradeEvent.put("Sell_APARPrice", sellOrder.getDouble("APARPrice"));
		tradeEvent.put("Buy_APARPrice", buyOrder.getDouble("APARPrice"));
		tradeEvent.put("APAR", sellOrder.getString("APAR"));
		tradeEvent.put("DSARTradePrice", sellOrder.getDouble("DSARPrice"));
		tradeEvent.put("APARTradePrice", sellOrder.getDouble("APARPrice"));
		tradeEvent.put("Flight", sellOrder.getString("Flight"));
		tradeEvent.put("Quantity", quantity);
		tradeEvent.put("SellImpressions", sellOrder.getLong(IMPRESSIONS));
		tradeEvent.put("BuyImpressions", buyOrder.getLong(IMPRESSIONS));
		tradeEvent.put("Sell_guid", sellOrder.getString(GUID));
		tradeEvent.put("Buy_guid", buyOrder.getString(GUID));
	
		tradeEvent.setEventName("mass_trades");
		if (logger.isDebugEnabled()) {
			logger.debug("MASS DTO Sending Trade Event " + tradeEvent);
		}
		CEPEventsSendRunnable.handleEventObject(tradeEvent);
	}

	/*
	 * Match on fields constituting "Symbol" and buy price >= sell price
	 */
	private boolean isDsarMatch(EventObject sellOrder, EventObject buyOrder) throws JSONException {
		return ( 
				(sellOrder.getString("Publisher").equals(buyOrder.getString("Publisher"))) && 
				(sellOrder.getString("WebSite").equals(buyOrder.getString("WebSite"))) && 	
				(sellOrder.getString("Page").equals(buyOrder.getString("Page"))) && 
				(sellOrder.getString("ScreenLocation").equals(buyOrder.getString("ScreenLocation"))) && 
				(sellOrder.getString("AdUnit").equals(buyOrder.getString("AdUnit"))) && 
				(sellOrder.getString("AdContext").equals(buyOrder.getString("AdContext"))) && 
				(sellOrder.getString("Flight").equals(buyOrder.getString("Flight"))) && 
				(sellOrder.getDouble("DSARPrice") <= buyOrder.getDouble("DSARPrice"))
		);
	}

	@Override
	public void run() {
		while(true) {
			try {
				doFunction(getNext());
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}


	private void doSave(EventObject next) {
		if (logger.isDebugEnabled()) {
			logger.debug("MASS DTO saving order " + next);
		}
		save(next);
	}


}
