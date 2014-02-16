package com.cep.darkstar.node;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.messaging.impls.gossip.node.GossipService;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;
import com.cep.messaging.interfaces.IMessagingService;
import com.cep.messaging.util.exception.InvalidTokenException;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.rabbitmq.client.ShutdownSignalException;

import com.cep.utils.Utils;


@SuppressWarnings("unused")
public class CEPEventsSendRunnable extends Thread {
	private static final Log dsLog = LogFactory.getLog(DarkStarNode.class);
	private final EPServiceProvider engine;
	private final MessagingService messaging;
	private final String eventType;
	private volatile boolean isShutdown = false;
	private int version;
	private static IMessagingService messagingService  = GossipService.instance();
	private static ArrayBlockingQueue<EventObject> eventQueue = new ArrayBlockingQueue<EventObject>(1000);
	private final String partition_field;
	private final boolean external_timing;
	private SimpleDateFormat shortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
	private SimpleDateFormat shortFormatColon = new SimpleDateFormat("yyyyMMdd-HH:mm:ss:SSS");
	private SimpleDateFormat reallyShortFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
	private SimpleDateFormat esotericFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //2012-02-01 13:48:14.848
	
	public static final String PARTITION_ON = "partition_on";
	
	public CEPEventsSendRunnable(EPServiceProvider engine, String eventType, String partitionField, BlockingQueue<EventObject> queue) throws IOException {
		this.engine = engine;
		this.eventType = eventType;
		this.partition_field = partitionField;
		this.version = StorageService.instance.valueFactory.releaseVersion().version;
		this.external_timing = DarkStarNode.isExternal_timing();
		this.messaging = MessagingService.instance();
		if (external_timing) {
			engine.getEPRuntime().sendEvent(new CurrentTimeEvent(0));
		}
	}

	public CEPEventsSendRunnable(EPServiceProvider engine, String eventType, BlockingQueue<EventObject> queue) throws IOException {
		this(engine, eventType, PARTITION_ON, queue);
	}
	
	public static void handleEventObject(EventObject event) {
		try {
			eventQueue.put(event);
		} catch (InterruptedException e) {
			dsLog.error("Error putting event into eventQueue: " + event, e);
		}
	}
	
	private Message eventToMessage(EventObject o) {
		byte[] body = o.toString().getBytes();
		return new Message(GossipUtilities.getLocalAddress(), Verb.MUTATION, body, version);
	}


	public void run() {
		 try {
			dsLog.info("CEPEventsSendRunnable waiting for events:");
			EventObject event;
			while(!isShutdown){
				event = eventQueue.take();
				try {
					if (dsLog.isDebugEnabled()){
						dsLog.debug("CEPEventsSendRunnable received event " + event.getEventName());
						dsLog.debug(event.toString());
					}
					
					// Get the partitioning information 
					String partition_on = event.has(partition_field) ? event.getString(partition_field) : null;

					// If the partitioning field is not present in the message process it locally 
					if (partition_on == null || messagingService.handleLocally(partition_on)) {
						if (dsLog.isDebugEnabled()){
							dsLog.debug("Event " + event.getEventName() + " is being handled locally");
						}

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
						// If Engine is set to external timing advance time manually
						if (external_timing) {
							advanceEngineTime(event);
						}						
						// we have a map, let's send it in
						try {
							if (dsLog.isDebugEnabled()) {
								dsLog.debug("Sending Event into the CEP Engine: " + mapEvent);
							}
							// send to cep engine
							engine.getEPRuntime().sendEvent(mapEvent, event.getEventName());
						} catch (Exception e) {
							dsLog.error("Inner Exception - EventSendRunnable caught *unhandled* exception " 
									+ e.getLocalizedMessage() + "Injecting "+event.getEventName(), e);
						}
					} else { // Do not handle locally

						InetAddress dest = messagingService.getDestinationFor(partition_on);
						
						if (dsLog.isDebugEnabled()) {
							dsLog.debug("This event should be handled by the node at " + dest.toString());
						}
						
						if (dsLog.isDebugEnabled()) {
							dsLog.debug("Sending Event " + event.getEventName() + " to node at " + dest);
						}
						
						// send to correct node
						messaging.sendOneWay(eventToMessage(event), dest);
					}
			} catch (JSONException e) {
				dsLog.error("JSON Exception processing event " + event + "\n"  + e.getMessage(), e);
			} catch (InvalidTokenException e) {
				dsLog.error("InvalidTokenException processing event " + event + "\n"  + e.getMessage(), e);
			} catch (Exception e) {
				dsLog.error("Unhandled Exception processing event " + event + "\n" + e.getMessage(), e);								
			}
			}	 
		} catch (ShutdownSignalException e) {
			dsLog.error("Shutdown signal received by thread " + Thread.currentThread() + "\n" + e.getMessage(), e);
		} catch (EPException e) {
			dsLog.error("EPException caught by thread " + Thread.currentThread() + "\n" + e.getMessage(), e);
		} catch (InterruptedException e) {
			dsLog.error("Thread interrupted: " + Thread.currentThread() + "\n" + e.getMessage(), e);
		}
		dsLog.info(".call Thread " + Thread.currentThread() + " done");
	}

	private void advanceEngineTime(EventObject event) {
		if (event.has("TransactTime")) {
			long timestamp = 0;
			try {
				timestamp = event.getLong("TransactTime");
				if (timestamp > 0) {
					engine.getEPRuntime().sendEvent(new CurrentTimeEvent(timestamp));
				}
			} catch (JSONException e) {
				dsLog.error(e.getMessage(), e);
			}
		}
	}

	public void setShutdown() {
		isShutdown = true;
	}
}
