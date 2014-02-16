package com.cep.darkstar.epl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONException;

import com.cep.commons.EventObject;

public class EplFunctions {
	
	// Time stuff
	private static SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
	private static SimpleDateFormat longFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
	private static GregorianCalendar baseCalendar = new GregorianCalendar();
	private static int year;
	private static int month;
	private static int day;
	
	// Side comparison stuff
	private static HashSet<Long> bidSides = new HashSet<Long>();
	private static HashSet<Long> askSides = new HashSet<Long>();
	
	// Marking the Open & Close
	private static ConcurrentLinkedQueue<EventObject> ordersOnClose = new ConcurrentLinkedQueue<EventObject>();
	private static ConcurrentLinkedQueue<EventObject> ordersOnOpen = new ConcurrentLinkedQueue<EventObject>();
	
	static {
		year = baseCalendar.get(Calendar.YEAR);
		month = baseCalendar.get(Calendar.MONTH);
		day = baseCalendar.get(Calendar.DAY_OF_MONTH);
		
		bidSides.add(1L);
		bidSides.add(3L);
		askSides.add(2L);
		askSides.add(4L);
		askSides.add(5L);
		askSides.add(6L);
	}
	
	public static void addOrderOnClose(EventObject event) {
		ordersOnClose.add(event);
	}
	
	public static double getMTCAskHigh(String Symbol) {
		double returnVal = 0.0;
		for (EventObject event : ordersOnClose) {
			try {
				if (isAskOrder(event) && (Symbol.equals(event.getString("Symbol")))) {
					Double price = event.getDouble("Price");
					if (price != null && price > returnVal) {
						returnVal = price.doubleValue();
					}
				}
			} catch (JSONException e) {
				// Just swallow this
			}
		}
		return returnVal;
	}
	
	public static double getMTCAskLow(String Symbol) {
		double returnVal = Double.MAX_VALUE;
		for (EventObject event : ordersOnClose) {
			try {
				if (isAskOrder(event) && (Symbol.equals(event.getString("Symbol")))) {
					Double price = event.getDouble("Price");
					if (price != null && price < returnVal) {
						returnVal = price.doubleValue();
					}
				}
			} catch (JSONException e) {
				// Just swallow this
			}
		}
		return returnVal;
	}
	
	public static void addOrderOnOpen(EventObject event) {
		ordersOnOpen.add(event);
	}
	
	public static double getMTOBidHigh(String Symbol) {
		double returnVal = 0.0;
		for (EventObject event : ordersOnOpen) {
			try {
				if (isBidOrder(event) && (Symbol.equals(event.getString("Symbol")))) {
					Double price;
					price = event.getDouble("Price");
					if (price != null && price > returnVal) {
						returnVal = price.doubleValue();
					}
				}
			} catch (JSONException e) {
				// Just swallow this
			}
		}
		return returnVal;
	}
	
	public static double getMTOBidLow(String Symbol) {
		double returnVal = Double.MAX_VALUE;
		for (EventObject event : ordersOnOpen) {
			try {
				if (isBidOrder(event) && (Symbol.equals(event.getString("Symbol")))) {
					Double price;
					price = event.getDouble("Price");
					if (price != null && price < returnVal) {
						returnVal = price.doubleValue();
					}
				}
			} catch (JSONException e) {
				// Just swallow this
			}
		}
		return returnVal;
	}
	
	public static boolean isBidOrder(EventObject order) {
		try {
			return (bidSides.contains((Long) order.get("Side")));
		} catch (JSONException e) {
			// If there is no "Side" field we have to return false
			return false;
		}		
	}

	public static boolean isAskOrder(EventObject order) {
		try {
			return (askSides.contains((Long) order.get("Side")));
		} catch (JSONException e) {
			// If there is no "Side" field we have to return false
			return false;
		}		
	}

	public static long fixTimestampToUtc(String timestamp) {
		try {
			if (timestamp.length() > 17) {
				return longTimestampFormatter(timestamp);
			} else {
				return shortTimestampFormatter(timestamp);
			}
		} catch (Throwable t) {
			return System.currentTimeMillis();
		} 
	}

	private static long shortTimestampFormatter(String timestamp) throws ParseException {
		return shortFormat.parse(timestamp).getTime();
	}

	private static long longTimestampFormatter(String timestamp) throws ParseException {
		return longFormat.parse(timestamp).getTime();
	}
	
	public static long timestampToLong(String startTime) {
		int hour = Integer.parseInt(startTime.substring(0, startTime.indexOf(":")));
		int minute = Integer.parseInt(startTime.substring(startTime.indexOf(":")+1, startTime.lastIndexOf(":")));
		int second = Integer.parseInt(startTime.substring(startTime.lastIndexOf(":")+1));
		GregorianCalendar cal = new GregorianCalendar(year, month, day, hour, minute, second);
		return cal.getTimeInMillis();
	}
	
	public static boolean isOppositeSide(Long side1, Long side2) {
		boolean opposite = false;
		if (bidSides.contains(side1)) {
			opposite = askSides.contains(side2);
		} else if (askSides.contains(side1)) {
			opposite = bidSides.contains(side2);
		}
		return opposite;
	}
	
}
