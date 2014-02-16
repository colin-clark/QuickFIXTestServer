package com.cep.utils;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.espertech.esper.client.EventBean;

public class Utils {
	public static String stripGarbage(String s) {  
        String good = " @#abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789���T:";
        String result = "";
        for ( int i = 0; i < s.length(); i++ ) {
            if ( good.indexOf(s.charAt(i)) >= 0 )
               result += s.charAt(i);
            }
        return result;
    }
	
	// pull key/value pairs out for pretty printing
	public static String getProperties(EventBean event) {
		StringBuilder buf = new StringBuilder();
		for (String name : event.getEventType().getPropertyNames()) {
			Object value = event.get(name);
			buf.append(name);
			buf.append("=");

			if (name.contains("timestamp")){
				buf.append(new Date((Long) value));
			}
			else {
				buf.append(value);
			}
			buf.append(" ");
		}
		return buf.toString();
	}
	
	
	/**
	 * Constructs an instance of the given class, which must have a no-arg
	 * constructor.
	 * 
	 * @param classname
	 *            Fully qualified classname.
	 * @param readable
	 *            Descriptive noun for the role the class plays.
	 * @throws ConfigurationException
	 *             If the class cannot be found.
	 */
	public static <T> T construct(String classname, String readable) throws ConfigurationException {
		Class<T> cls = Utils.classForName(classname, readable);
		try {
			return cls.getConstructor().newInstance();
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException(String.format(
					"No default constructor for %s class '%s'.", readable, classname));
		} catch (IllegalAccessException e) {
			throw new ConfigurationException(String.format(
					"Default constructor for %s class '%s' is inaccessible.", readable, classname));
		} catch (InstantiationException e) {
			throw new ConfigurationException(String.format(
					"Cannot use abstract class '%s' as %s.", classname, readable));
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof ConfigurationException) {
				throw (ConfigurationException) e.getCause();
			}
			throw new ConfigurationException(String.format("Error instantiating %s class '%s'.", readable, classname), e);
		}
	}

	/**
	 * @return The Class for the given name.
	 * @param classname
	 *            Fully qualified classname.
	 * @param readable
	 *            Descriptive noun for the role the class plays.
	 * @throws ConfigurationException
	 *             If the class cannot be found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> classForName(String classname, String readable) throws ConfigurationException {
		try {
			return (Class<T>) Class.forName(classname);
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(String.format("Unable to find %s class '%s'", readable, classname));
		}
	}

	// handy parse date string to long
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
}