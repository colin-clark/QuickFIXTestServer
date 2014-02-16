package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOError;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.commands.Truncation;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.TruncateResponse;

public class TruncateVerbHandler implements IVerbHandler
{
    private static Logger logger = LoggerFactory.getLogger(TruncateVerbHandler.class);

    public void doVerb(Message message, String id)
    {
        byte[] bytes = message.getMessageBody();
        ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);

        try
        {
            Truncation t = Truncation.serializer().deserialize(new DataInputStream(buffer), message.getVersion());
            logger.debug("Applying {}", t);

            try
            {
                ColumnFamilyStore cfs = Table.open(t.keyspace).getColumnFamilyStore(t.columnFamily);
                cfs.truncate().get();
            }
            catch (Exception e)
            {
                logger.error("Error in truncation", e);
                respondError(t, message);
            }
            logger.debug("Truncate operation succeeded at this host");

            TruncateResponse response = new TruncateResponse(t.keyspace, t.columnFamily, true);
            Message responseMessage = TruncateResponse.makeTruncateResponseMessage(message, response);
            logger.debug("{} applied.  Sending response to {}@{} ", new Object[]{ t, id, message.getFrom()});
            MessagingService.instance().sendReply(responseMessage, id, message.getFrom());
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    private static void respondError(Truncation t, Message truncateRequestMessage) throws IOException
    {
        TruncateResponse response = new TruncateResponse(t.keyspace, t.columnFamily, false);
        Message responseMessage = TruncateResponse.makeTruncateResponseMessage(truncateRequestMessage, response);
        MessagingService.instance().sendOneWay(responseMessage, truncateRequestMessage.getFrom());
    }
}
