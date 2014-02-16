package com.cep.messaging.impls.gossip.transport.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;


public class SchemaCheckVerbHandler implements IVerbHandler
{
    private final Logger logger = LoggerFactory.getLogger(SchemaCheckVerbHandler.class);
    
    public void doVerb(Message message, String id)
    {
        logger.debug("Received schema check request.");
        Message response = message.getInternalReply(NodeDescriptor.getDefsVersion().toString().getBytes(), message.getVersion());
        MessagingService.instance().sendReply(response, id, message.getFrom());
    }
}
