package com.cep.darkstar.onramp.appia.test.stub;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

import com.cep.darkstar.onramp.appia.AppiaPluginOnRamp;
import com.javtech.appia.ef.session.SessionPluginFilter;
import com.javtech.javatoolkit.fix.FixConstants;
import com.javtech.javatoolkit.fix.FixMessageObject;
import com.javtech.javatoolkit.message.MessageData;
import com.javtech.javatoolkit.message.MessageObject;

public class AppiaStub {
	protected static final CountDownLatch shutdownLatch = new CountDownLatch(1);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String name = "AppiaPluginOnRamp";
		Map<String,Object> plugin_args = new HashMap<String,Object>();

		long rate = 3000;
		long total = 1000000;
	   
		if (args.length < 1) {
			System.out.println("You must specify the name of the file to use for Appia configuration");
            return;
        } else {
            try {
				BufferedReader reader = new BufferedReader(new FileReader(args[0]));
				String line;
				while((line = reader.readLine()) != null) {
					if (line.toUpperCase().startsWith("RATE")) {
						rate = Long.valueOf(line.substring(line.indexOf('=')+1).trim());
					} else if (line.toUpperCase().startsWith("TOTAL")) {
						total = Long.valueOf(line.substring(line.indexOf('=')+1).trim());
					} else if (line.toUpperCase().startsWith("DARKSTARNODE")) {
						plugin_args.put("darkStarNode", line.substring(line.indexOf('=')+1).trim());
					} else if (line.toUpperCase().startsWith("DARKSTARPORT")) {
						plugin_args.put("darkStarPort", line.substring(line.indexOf('=')+1).trim());
					} else if (line.toUpperCase().startsWith("CLUSTERNAME")) {
						plugin_args.put("clusterName", line.substring(line.indexOf('=')+1).trim());
					} else if (line.toUpperCase().startsWith("FIRMTAG")) {
						plugin_args.put("firmTag", line.substring(line.indexOf('=')+1).trim());
					} else if (line.toUpperCase().startsWith("FIRM")) {
						plugin_args.put("firm", line.substring(line.indexOf('=')+1).trim());
					} else if (line.toUpperCase().startsWith("BATCHSIZE")) {
						plugin_args.put("batchSize", line.substring(line.indexOf('=')+1).trim());
					}
				}
				System.out.println("Sending " + total + " messages at a rate of " + rate);
				System.out.println("Plugin args: " + plugin_args);
				
				SessionPluginFilter sessionFilter = new SessionPluginFilter();
				System.out.println("Creating onramp");
				AppiaPluginOnRamp onramp = new AppiaPluginOnRamp(name, sessionFilter, plugin_args);
				System.out.println("Onramp created");
				sendMessages(onramp, rate, total);
				try {
					shutdownLatch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}
	
	private static void sendMessages(AppiaPluginOnRamp onramp, long rate, long num) {
		
		String tickers_[] = new String[]{"IBM", "MSFT", "NSCP", "EMC", "EBAY", "YHOO",
                "CSCO", "AOL", "MO", "T", "INTL", "CELL", "PPRT",
                "VISX", "GE", "GM", "GTE", "HP", "DELL", "SUNW"};
		
		/*
		 *  Loop here creating Messages and calling the onramp's processInboundMessagePostValidation method
		 */
        String sender = "STUB";
        String target = "PLUG";

        //Create a FIX Order message.
        //Start by creating a new object
        MessageObject msg = new FixMessageObject();

        //Identify the message type of message
        msg.setMessageType(FixConstants.Order);

        //Identify the FIX protocol version
        msg.setProtocol(FixConstants.FIX_42);

        //Access the data map for the object
        MessageData data = msg.getMessageData();

        //Fill in the message fields (provided by the data map)
        data.setValue(FixConstants.SenderCompID, sender);              //FIX Tag 49
        data.setValue(FixConstants.TargetCompID, target);              //FIX Tag 56
        data.setValue(FixConstants.TargetSubID, "Simulator");          //FIX Tag 57
        data.setValue(FixConstants.OrdType, "2");                      //FIX Tag 40
        data.setValue(FixConstants.HandlInst, "2");                    //FIX Tag 21
        data.setInt(FixConstants.OrderQty, 5000);                      //FIX Tag 38
        data.setValue(FixConstants.Side, "1");                         //FIX Tag 54
        data.setDouble(FixConstants.Price, 110.5);                     //FIX Tag 44

        long startTime = System.currentTimeMillis();
        long baseTime = System.currentTimeMillis();
        int sent = 0;
        long totalSent = 0;
        while(totalSent < num) {
        	while ((System.currentTimeMillis() - baseTime) <= 1000) {
        		if ( (sent < rate) && (totalSent < num) ) {
		            data.setValue(FixConstants.ClOrdID, "order " + totalSent);                 
		            data.setValue(FixConstants.Symbol, tickers_[(int) (totalSent % tickers_.length)]); 
		            data.setValue(FixConstants.TransactTime, getTransactTime());   
		            onramp.processInboundMessagePostValidation(msg, null);
		            totalSent++;
		            sent++;
        		} else {
        			try {
        				Thread.sleep(1);
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
        	}
        	baseTime = System.currentTimeMillis();
        	sent = 0;
        }
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        double rate1 = (double)num/(double)elapsed;
        System.out.println("*******************************************************************************");
        System.out.println("Sent " + num + " orders in " + elapsed + " ms for a rate of " + (rate1 * 1000) + " messages/second");
        System.out.println("*******************************************************************************");
	}
	
    public static String getTransactTime() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
    	
    	dateFormat.setTimeZone(TimeZone.getDefault());
    	return dateFormat.format(new Date(System.currentTimeMillis())).toString();
    }

}
