package com.cep.darkstar.utility.cassandra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;

public class FileMessageReader {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.utility.cassandra.FileMessageReader");
	
	public static final String PARTITION_ON = "partition_on";
	public static final String DS_TIMESTAMP = "_ds_timestamp";
	
	private MessageReaderCallback callback;

	private static Set<String> longTypes = new HashSet<String>();
	
	static {
		longTypes.add("OrderQty");
		longTypes.add("Price");
	}
	
	private String fileName;
	private BufferedReader reader;
	
	public FileMessageReader(CassandraClientConfigInfo info) throws FileNotFoundException {
		fileName = info.getInputFile();
		reader = new BufferedReader(new FileReader(fileName));
	}

	public void read(MessageReaderCallback callback) {
		this.callback = callback;
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				try {
					this.callback.send(convertLineToEvent(line));
				} catch (JSONException e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private static final String ORDER = "Order";
	private static final String ONRCV_TS = "_onrcvTS";
	private static final String PRICE = "Price";
	
	private EventObject convertLineToEvent(String line) {
		EventObject event = new EventObject();
		if (line.indexOf(ORDER) >= 0) {
			String _onrcvTS = line.substring(0, 13);
			try {
				event.put(ONRCV_TS, Long.valueOf(_onrcvTS));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} 
			String[] attributes = line.split("Attribute: ");
			// Skip the first String, it's the timestamp
			for (int i = 1; i < attributes.length; i++) {
				String attribute = attributes[i];
				String name = attribute.substring(0, attribute.indexOf('(')).trim();
				String val = attribute.substring((attribute.indexOf("is:")+3)).trim();
				if (longTypes.contains(name)) {
					long value = 0;
					if (name.equals(PRICE)) {
						Double dval = Double.valueOf(val)*1000;
						value = dval.longValue();
					} else {
						value = Long.valueOf(val);
					}
					try {
						event.put(name, value);
					} catch (JSONException e) {
						logger.error(e.getMessage(), e);
					}
				} else {
					try {
						event.put(name, val);
					} catch (JSONException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		return event;
	}

}
