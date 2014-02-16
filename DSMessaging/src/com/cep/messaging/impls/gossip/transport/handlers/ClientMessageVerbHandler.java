package com.cep.messaging.impls.gossip.transport.handlers;

import java.net.UnknownHostException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.commons.EventObject;
import com.cep.messaging.impls.gossip.node.GossipService;
import com.cep.messaging.impls.gossip.transport.messages.Message;

public class ClientMessageVerbHandler implements IVerbHandler
{
    private static Logger logger = LoggerFactory.getLogger(ClientMessageVerbHandler.class);

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
    	/*
    	 * Send to CEPEventsSendRunnable through callback to be added to DarkStarVM api
        */
    	try {
			GossipService.instance().handleEventObject(new EventObject(new String(forwardBytes)));
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}
    }
}
//GossipService.instance().handleEventObject(new EventObject(new String(message.getMessageBody())));
