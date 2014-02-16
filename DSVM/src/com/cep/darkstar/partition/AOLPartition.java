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
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author colin
 *
 */

@SuppressWarnings("unused")
public class AOLPartition {

	public static void main(String[] args) throws JSONException, IOException {
		String hostName = "192.168.1.6";
		int portNumber =  AMQP.PROTOCOL.PORT;
		String exchange = null;
		String queue = null;

		PublishTopic publish1 = new PublishTopic.Builder().hostName("192.168.1.6").topic("EVENT").build();
		PublishTopic publish2 = new PublishTopic.Builder().hostName("192.168.1.7").topic("EVENT").build();
		Boolean pub = false;
		
		try {
			SubscribeTopic listenFor = new SubscribeTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();

			while(true){
				try {
					EventObject event = new EventObject(new String(listenFor.nextDelivery()));
					event.put("N_id", "null");
					event.put("N_ts", 0L);
					event.put("N_query_id", "null");
					// JSON key, value - object = string, int, double, long, etc.]
					if (pub) {
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