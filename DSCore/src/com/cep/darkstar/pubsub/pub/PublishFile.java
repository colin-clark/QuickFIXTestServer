package com.cep.darkstar.pubsub.pub;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import com.cep.commons.EventObject;

public class PublishFile {
	static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.PublishFile");
	 
	public static void main(String[] args) throws JSONException, IOException {
		// initialize log4j
		PropertyConfigurator.configure("log4j.properties");
		PublishTopic publish = new PublishTopic.Builder().hostName("192.168.1.5").build();
		long begin = System.currentTimeMillis();
		long end = 0;
		int i =0;
		String aTopic = "";
		// and publish a test object
		EventObject anEvent = new EventObject();

		while (true) {
			i = i + 1;
			
			if ((i % 3) == 0) {aTopic = "stock.IBM";}
			if ((i % 3) == 1) {aTopic = "stock.AAPL";}
			if ((i % 3) == 2) {aTopic = "bond.AIG";}
			
			anEvent.setEventName("stock.price");
			anEvent.put("price",i);
			anEvent.put("instrument", aTopic);
			publish.publish(anEvent);
			if ((i % 50000) == 0) {
				end = System.currentTimeMillis();
				logger.info("50,000 messages sent in "+Long.toString((end-begin)/1000)+" seconds");
				begin = end;
			
			//take a break, we can do 100 events/second
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}

			}
		}
	}
}
