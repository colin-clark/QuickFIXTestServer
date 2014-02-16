package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOError;
import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.commands.RowMutation;
import com.cep.messaging.impls.gossip.transport.messages.Message;

public class ReadRepairVerbHandler implements IVerbHandler
{    
    public void doVerb(Message message, String id)
    {          
        byte[] body = message.getMessageBody();
        ByteArrayInputStream buffer = new ByteArrayInputStream(body);
        
        try
        {
            RowMutation rm = RowMutation.serializer().deserialize(new DataInputStream(buffer), message.getVersion());
            rm.apply();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
}
