package com.cep.messaging.impls.gossip.transport.handlers;

import java.net.UnknownHostException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.commons.EventObject;
import com.cep.messaging.impls.gossip.node.GossipService;
import com.cep.messaging.impls.gossip.transport.messages.Message;

public class SendVerbHandler implements IVerbHandler {
    private static Logger logger = LoggerFactory.getLogger(SaveVerbHandler.class);

    @Override
    public void doVerb(Message message, String id) {
    	try {
			SendToCEP(message.getMessageBody());
		} catch (UnknownHostException e) {
			logger.error(e.getMessage(), e);
		}
    }  
    
    private void SendToCEP(byte[] forwardBytes) throws UnknownHostException
    {
    	try {
    		EventObject event = new EventObject(new String(forwardBytes));
    		event.put("function", "com.cep.darkstar.functions.Send");
			GossipService.instance().handleEventObject(event);
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}
    }

}
