package com.cep.messaging.impls.gossip.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cep.messaging.impls.gossip.transport.MessageProducer;
import com.cep.messaging.impls.gossip.transport.messages.Message;

public class CachingMessageProducer implements MessageProducer
{
    private final MessageProducer prod;
    private final Map<Integer, Message> messages = new HashMap<Integer, Message>(2);

    public CachingMessageProducer(MessageProducer prod)
    {
        this.prod = prod;    
    }

    public synchronized Message getMessage(Integer version) throws IOException
    {
        Message msg = messages.get(version);
        if (msg == null)
        {
            msg = prod.getMessage(version);
            messages.put(version, msg);
        }
        return msg;
    }
}
