package com.cep.darkstar.onramp.appia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.onramp.AbstractOnRamp;
import com.cep.darkstar.onramp.configuration.appia.AppiaConfig;
import com.cep.darkstar.onramp.configuration.appia.AppiaConfiguration;
import com.cep.darkstar.onramp.configuration.appia.FixEventMapping;
import com.javtech.appia.javatoolkit.middleware.MiddlewareConfig;
import com.javtech.appia.javatoolkit.middleware.MiddlewareEvent;
import com.javtech.appia.javatoolkit.middleware.MiddlewareEventListener;
import com.javtech.appia.javatoolkit.middleware.MiddlewareInterface;
import com.javtech.appia.javatoolkit.middleware.MiddlewareInterfaceFactory;
import com.javtech.appia.protocols.fix.adapter.FixMessageObject;
import com.javtech.javatoolkit.message.MessageData;

public class AppiaOnrampMiddleware extends AbstractOnRamp {
	final static Logger logger = Logger.getLogger("com.cep.darkstar.onramp.appia.AppiaOnramp");
	
    private MiddlewareInterface appia_;
    
    // Properties
    private String host;
    private int port;
    private String uniqueID;
    private boolean globalMessages;
    private boolean applicationMessages;
    private boolean sessionMessages;
    private boolean sessionEvents;
    private boolean messageValidatedEvents;
    private boolean messageCommitEvents;
    private boolean messageSentEvents;
    private String partition_key;
    private String firm;
    private Map<String,String> messageTypeMapping = new HashMap<String,String>();
    
    private boolean keepRunning = true;
    private LinkedBlockingQueue<EventObject> eventQueue = new LinkedBlockingQueue<EventObject>();
    private boolean connectedToAppia = false;
    
	public AppiaOnrampMiddleware() {
	}

    /**
     * Public constructor. Creates a non-transactional JTK  MiddlewareInterface;
     *                     adds a new MiddlewareEventListener & sets MiddlewareConfig properties.
     *
     * @param host a hostname or IP address
     * @param port the socket port for the Appia Socket MiddlewareInterface
     * @param uniqueID the uniqueID for this MiddlewareInterface connection
     */
    @SuppressWarnings("rawtypes")
	public void configureMiddleware() throws Exception {
        try {
            appia_ = MiddlewareInterfaceFactory.getSocketMiddlewareInterface(host, port, uniqueID, false);
            logger.info("got Appia MiddlewareInterface \"" + uniqueID + "\"  at " + host + ":" + port + "\n");

            // Connect to the Appia server
            connectToAppia();
            
            //set middleware event listener
            appia_.setMiddlewareEventListener(new SocketMiddlewareEventListener());

            MiddlewareConfig mc = appia_.getMiddlewareConfig();

            mc.InMsgFormat = MiddlewareConfig.ATTRIBUTE_VALUE_OBJECT;
            mc.GlobalMessages = globalMessages;
            mc.ApplicationMessages = applicationMessages;
            mc.SessionMessages = sessionMessages;
            mc.SessionEvents = sessionEvents;

            mc.MessageValidatedEvents = messageValidatedEvents;
            mc.MessageCommitEvents = messageCommitEvents;
            mc.MessageSentEvents = messageSentEvents;

            //JTK uses array lists
            mc.CheckRemote = new ArrayList();
            mc.SessionIDs = new ArrayList();
            mc.MessageTypes = new ArrayList();

            // set the new MiddlewareConfig for the MiddlewareInterface
            appia_.setMiddlewareConfig(mc);

            // start MiddlewareEvent delivery
            appia_.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void connectToAppia() {
        while (!connectedToAppia) {
        	logger.info("Attempting to connect to Appia host " + host + ":" + port);
        	try {
        		appia_.open();
        		connectedToAppia = true;
        	} catch (Exception e) {
        		logger.warn("Could not connect to " + host + ":" + port);
        	}
        	if (!connectedToAppia) {
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Just swallow this Exception
				}
        	}
        }  
        logger.info("Connected to Appia host " + host + ":" + port);
	}

	/**
     * Inner class for processing Appia events
     */
    private class SocketMiddlewareEventListener extends MiddlewareEventListener {
        /**
         * onMiddlewareEvent method.  Reacts to the entire range of Appia middleware event types.
         *                            Based on the event type, different actions are taken.
         */
        public void onMiddlewareEvent(MiddlewareEvent me) {
            switch (me.getEventType()) {
                case MiddlewareEvent.APPLICATION_MESSAGE_RECEIVED:
                	if (logger.isDebugEnabled()) { 
                		logger.debug("Application message received: " + me.getEventData());
                	}
					FixMessageObject msg = (FixMessageObject) me.getEventData();
					String stream = messageTypeMapping.get(msg.getMessageType());
					if (stream != null) {
						iterateMessage(msg, stream);
					}
                	break;
                case MiddlewareEvent.SESSION_MESSAGE_RECEIVED:
                    if (logger.isDebugEnabled()) { 
                    	logger.debug("Session message received: " + me.getEventData());
                    }
                    break;
                case MiddlewareEvent.SESSION_CONNECTED:
                    logger.info("Session connected: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_DISCONNECTED:
                    logger.info("Session disconnected: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_CONNECT_FAILURE:
                    logger.warn("Session connect failure: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_CONNECT_TIMEOUT:
                    logger.warn("Session connect timeout: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_CREATED:
                    logger.info("New session created: " + me.getSessionID());
                    break;  
                case MiddlewareEvent.SESSION_DELETED:
                    logger.info("Session deleted: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_HOLD:
                   	logger.info("Session Hold: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_RELEASE:
                   	logger.info("Session Release: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_SERVICE_DOWN:
                    logger.warn("Session Service Down: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_SERVICE_UP:
                    logger.info("Session connect failure: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_SUSPEND:
                    logger.warn("Session Suspended: " + me.getSessionID());
                    break;
                case MiddlewareEvent.SESSION_RESUME:
                   	logger.info("Session Resumed: " + me.getSessionID());
                    break;
                case MiddlewareEvent.GLOBAL_MESSAGE:
                   	logger.info("Global message received: " + me.getEventData());
                    break;
            }
        }

		private void iterateMessage(FixMessageObject msg, String stream) {
			EventObject event = new EventObject();
		    MessageData data = msg.getMessageData();
		    for (MessageData.EntryIterator it = data.iterator(); it.hasNext();) {
		    	MessageData.Entry entry = it.nextEntry();
		    	if (logger.isDebugEnabled()) {
		    		logger.debug(entry.getAttribute() + " = " + entry.getValue());
		    	}
		    	try {
		    		event.put(entry.getAttribute().getName(), entry.getValue());
		    	} catch (JSONException e) {
		    		logger.error(e.getMessage(), e);
		    	}
		    }
			try {
				event.put("firm", firm);
				event.put("event_name", stream);
				event.put("partition_on", event.getString(partition_key));
			} catch (JSONException e) {
				logger.error(e.getMessage(), e);
			}

			eventQueue.add(event);
		}

    }
    
    public void setProperties(String properties) {
		AppiaConfig info = null;
		try {
			info = AppiaConfiguration.getConfiguration(properties);
			super.setProperties(info);
			//PropertyConfigurator.configure(log4j_file);
			
		    host = info.getHost();
		    port = info.getPort();
		    uniqueID = info.getUniqueID();
		    globalMessages = info.isGlobalMessages();
		    applicationMessages = info.isApplicationMessages();
		    sessionMessages = info.isSessionMessages();
		    sessionEvents = info.isSessionEvents();
		    messageValidatedEvents = info.isMessageValidatedEvents();
		    messageCommitEvents = info.isMessageCommitEvents();
		    messageSentEvents = info.isMessageSentEvents();
		    partition_key = info.getPartition_key();
		    
		    List<FixEventMapping> mapping = info.getEventMapping();
		    for (FixEventMapping type : mapping) {
		    	messageTypeMapping.put(type.getMsgType(), type.getStream());
		    }
		    /*messageTypeMapping.put(FixConstants.ExecutionReport.getCode(),"ExecutionReports");
		    messageTypeMapping.put(FixConstants.NewOrderList.getCode(),"FIXNewOrderSingle");
		    messageTypeMapping.put(FixConstants.NewOrderSingle.getCode(),"FIXNewOrderSingle");
		    messageTypeMapping.put(FixConstants.Order.getCode(),"FIXNewOrderSingle");
		    messageTypeMapping.put(FixConstants.OrderCancelReplace.getCode(),"FIXNewOrderSingle");
		    messageTypeMapping.put(FixConstants.OrderCancelRequest.getCode(),"FIXNewOrderSingle");
		    messageTypeMapping.put(FixConstants.CrossOrder.getCode(),"FIXNewOrderSingle");
		    messageTypeMapping.put(FixConstants.CrossOrderCancelReplace.getCode(),"FIXNewOrderSingle");
		    messageTypeMapping.put(FixConstants.CrossOrderCancelRequest.getCode(),"FIXNewOrderSingle");*/
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    }
    
	private void processMessages() {
		while(keepRunning) {
			try {
				send(eventQueue.take());
			} catch (InterruptedException e) {
				logger.error("InterruptedException: " + e.getMessage(), e);
			} catch (JSONException e) {
				logger.error("JSONException: " + e.getMessage(), e);
			}
		}
	}

	/**
     * Main method
     * @param args command line arguments
     */
    public static void main(String[] args) {
        AppiaOnrampMiddleware onramp = new AppiaOnrampMiddleware();
		try {
			onramp.setProperties(args[0]);
			onramp.configureMiddleware();
			onramp.init();
			onramp.processMessages();
		} catch (Exception e1) {
			System.out.println("Unable to use "+args[0]+" as a valid configuration file.");
			e1.printStackTrace();
		}
    }

}
