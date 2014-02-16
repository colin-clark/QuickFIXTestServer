/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.partition;

import java.io.IOException;

import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.pub.PublishTopic;
import com.cep.darkstar.pubsub.sub.SubscribeTopic;
import com.espertech.esper.client.EPException;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author colin
 *
 */
public class DarkStarPartition {

	public static void main(String[] args) throws JSONException, IOException {
		PublishTopic publish1 = new PublishTopic.Builder().hostName("192.168.1.6").topic("EVENT").build();
		PublishTopic publish2 = new PublishTopic.Builder().hostName("192.168.1.7").topic("EVENT").build();
		Boolean pub = false;
		
		try {
			SubscribeTopic listenFor = new SubscribeTopic.Builder().hostName("192.168.1.6").topic("NYSETradeEvent").build();
			System.out.println("CEPEventsSendRunnable waiting for events:");
			while(true){
				try {
					EventObject event = new EventObject(new String(listenFor.nextDelivery()));
					// JSON key, value - object = string, int, double, long, etc.]
					if (event.getString("symbol").equalsIgnoreCase("IBSN")) {
						publish2.publish(event);
					} else {
						publish1.publish(event);
					}
					pub = !pub;
				} catch (InterruptedException e) {
						System.out.println("Interrupted:");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("I/O Error:");
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					System.out.println("JSON Error:");
					e.printStackTrace();
				} catch (Exception e) {
					System.out.println("Caught *unhandled* exception:"+e.getLocalizedMessage());				}
			}	 
		} catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}