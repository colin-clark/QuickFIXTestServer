/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.imports;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author colin
 *
 *	this class contains addin's for the CEP engine
 *  these functions may be called from within EPL
 *
 */


public class CEP {
  public static long removeSeconds(long timestamp) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
}
