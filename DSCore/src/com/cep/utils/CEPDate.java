package com.cep.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CEPDate {
	public static Date getDateTime() {
		return new Date();
	}
	
	public static String getDateTimeFormatted() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
