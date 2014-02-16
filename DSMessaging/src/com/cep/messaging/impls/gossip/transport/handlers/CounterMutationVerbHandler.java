package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.commands.CounterMutation;
import com.cep.messaging.impls.gossip.node.StorageProxy;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.WriteResponse;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.exception.UnavailableException;

public class CounterMutationVerbHandler implements IVerbHandler
{
    private static Logger logger = LoggerFactory.getLogger(CounterMutationVerbHandler.class);

    public void doVerb(Message message, String id)
    {
        byte[] bytes = message.getMessageBody();
        ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);

        try
        {
            DataInputStream is = new DataInputStream(buffer); 
            CounterMutation cm = CounterMutation.serializer().deserialize(is, message.getVersion());
            if (logger.isDebugEnabled())
              logger.debug("Applying forwarded " + cm);

            String localDataCenter = NodeDescriptor.getEndpointSnitch().getDatacenter(GossipUtilities.getLocalAddress());
            StorageProxy.applyCounterMutationOnLeader(cm, localDataCenter).get();
            WriteResponse response = new WriteResponse(cm.getTable(), cm.key(), true);
            Message responseMessage = WriteResponse.makeWriteResponseMessage(message, response);
            MessagingService.instance().sendReply(responseMessage, id, message.getFrom());
        }
        catch (UnavailableException e)
        {
            // We check for UnavailableException in the coordinator not. It is
            // hence reasonable to let the coordinator timeout in the very
            // unlikely case we arrive here
        }
        catch (TimeoutException e)
        {
            // The coordinator node will have timeout itself so we let that goes
        }
        catch (IOException e)
        {
            logger.error("Error in counter mutation", e);
        }
    }
}
