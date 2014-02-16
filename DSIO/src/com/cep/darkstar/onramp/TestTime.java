package com.cep.darkstar.onramp;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class TestTime {
	
	public static long getLongForDateFormat(String timestring) throws ParseException {
		SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
		SimpleDateFormat shortFormatColon = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
		SimpleDateFormat reallyShortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
		SimpleDateFormat esotericFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		long timestamp = 0;
	
		if (timestring.length() > 17) {
			if (timestring.length() == 23) {
				timestamp = esotericFormat.parse(timestring).getTime();
			} else {
				if (timestring.charAt(17) == ':') {
					timestamp = shortFormatColon.parse(timestring).getTime();
				} else {
					timestamp = shortFormat.parse(timestring).getTime();
				}
			}
		} else if (timestring.length() == 16) {
			timestamp = reallyShortFormat.parse(timestring).getTime();
		}
		return timestamp;
	}
	
	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		System.out.println(getLongForDateFormat("2011-06-01 00:00:11.000"));
	}
}
