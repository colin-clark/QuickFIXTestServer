/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.partition;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.node.CEPEventsSendRunnable;
import com.cep.darkstar.pubsub.pub.PublishTopic;
import com.cep.darkstar.pubsub.sub.SubscribeTopic;
import com.espertech.esper.client.EPException;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author colin
 *
 */

@SuppressWarnings("unused")
public class DarkStarRedirect {
	private static final Log log = LogFactory.getLog(CEPEventsSendRunnable.class);
	private volatile boolean isShutdown = false;

	public static void main(String[] args) throws JSONException, IOException {
		PublishTopic publish1 = new PublishTopic.Builder().hostName("192.168.1.5").topic("EVENT").build();
		Boolean pub = false;
		try {
			SubscribeTopic listenFor = new SubscribeTopic.Builder().hostName("192.168.1.5").topic("QUERY").build();
			while(true){
				try {
					EventObject event = new EventObject(new String(listenFor.nextDelivery()));
					// JSON key, value - object = string, int, double, long, etc.]
					publish1.publish(event);
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
		log.info(".call Thread " + Thread.currentThread() + " done");
	}
}