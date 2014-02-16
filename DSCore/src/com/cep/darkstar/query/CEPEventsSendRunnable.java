package com.cep.darkstar.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.sub.SubscribeTopic;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ShutdownSignalException;

public class CEPEventsSendRunnable extends Thread {
	private static final Log log = LogFactory.getLog(CEPEventsSendRunnable.class);
	private final EPServiceProvider engine;
	private final String eventType;
	private volatile boolean isShutdown = false;

	public CEPEventsSendRunnable(EPServiceProvider engine, String eventType) throws IOException {
		this.engine = engine;
		this.eventType = eventType;
	}

	public void run() {
		String hostName = "localhost";
		int portNumber =  AMQP.PROTOCOL.PORT;
		String exchange = null;
		String queue = null;
		SubscribeTopic listen;

		try {
			listen = new SubscribeTopic.Builder().hostName(hostName).portNumber(portNumber).topic("REDUCE").build();

			log.info(".call Thread " + Thread.currentThread() + " starting");
			log.info("Listening to exchange " + exchange + ", pattern " + eventType + " from queue " + queue);
			log.info("CEPEventsSendRunnable waiting for events:");
			while(!isShutdown){
				try {
					String body = new String(listen.nextDelivery());
					EventObject event = new EventObject(body);
					
					// JSON key, value - object = string, int, double, long, etc.
					Map<String, Object> mapEvent = new HashMap<String, Object>();
					Iterator<?> keys = event.keys();
					while (keys.hasNext()){
						Object k = keys.next();
						Object v = event.get(k.toString());
						// we don't deal with int's - 64 bit faster actually
						if (v.getClass().equals(Integer.class)){
							mapEvent.put(k.toString(), Long.valueOf((Integer) v));
						} else {
							mapEvent.put(k.toString(), v);
						}
					}
					try {
						engine.getEPRuntime().sendEvent(mapEvent, event.getEventName());
					} catch (Exception e) {
						log.error("Exception caught attempting to inject event into CEP Engine:\n" + 
								event.toString(), e);
					}
				} catch (InterruptedException e) {
					log.error("Interrupted:", e);
				} catch (JSONException e) {
					log.error("JSON Error:", e);
				} catch (Exception e) {
					log.error("Caught *unhandled* exception:", e);				
				}
			}	 
		} catch (ShutdownSignalException e) {
			log.error(e.getMessage(), e);
		} catch (EPException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		log.info(".call Thread " + Thread.currentThread() + " done");
	}

	public void setShutdown()
	{
		isShutdown = true;
	}
}
