package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.commands.ReadCommand;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.ReadResponse;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.DataOutputBuffer;

public class ReadVerbHandler implements IVerbHandler
{
    protected static class ReadContext
    {
        protected ByteArrayInputStream bufIn_;
        protected DataOutputBuffer bufOut_ = new DataOutputBuffer();
    }

    private static Logger logger_ = LoggerFactory.getLogger( ReadVerbHandler.class );
    /* We use this so that we can reuse readcontext objects */
    private static ThreadLocal<ReadVerbHandler.ReadContext> tls_ = new InheritableThreadLocal<ReadVerbHandler.ReadContext>();

    public void doVerb(Message message, String id)
    {
        byte[] body = message.getMessageBody();
        /* Obtain a Read Context from TLS */
        ReadContext readCtx = tls_.get();
        if ( readCtx == null )
        {
            readCtx = new ReadContext();
            tls_.set(readCtx);
        }
        readCtx.bufIn_ = new ByteArrayInputStream(body);

        try
        {
            if (StorageService.instance.isBootstrapMode())
            {
                /* Don't service reads! */
                throw new RuntimeException("Cannot service reads while bootstrapping!");
            }
            ReadCommand command = ReadCommand.serializer().deserialize(new DataInputStream(readCtx.bufIn_), message.getVersion());
            Table table = Table.open(command.table);
            Row row = command.getRow(table);
            ReadResponse readResponse = getResponse(command, row);
            /* serialize the ReadResponseMessage. */
            readCtx.bufOut_.reset();

            ReadResponse.serializer().serialize(readResponse, readCtx.bufOut_, message.getVersion());

            byte[] bytes = new byte[readCtx.bufOut_.getLength()];
            System.arraycopy(readCtx.bufOut_.getData(), 0, bytes, 0, bytes.length);

            Message response = message.getReply(GossipUtilities.getLocalAddress(), bytes, message.getVersion());
            if (logger_.isDebugEnabled())
              logger_.debug(String.format("Read key %s; sending response to %s@%s",
                                          ByteBufferUtil.bytesToHex(command.key), id, message.getFrom()));
            MessagingService.instance().sendReply(response, id, message.getFrom());
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static ReadResponse getResponse(ReadCommand command, Row row)
    {
        if (command.isDigestQuery())
        {
            if (logger_.isDebugEnabled())
                logger_.debug("digest is " + ByteBufferUtil.bytesToHex(ColumnFamily.digest(row.cf)));
            return new ReadResponse(ColumnFamily.digest(row.cf));
        }
        else
        {
            return new ReadResponse(row);
        }
    }
}
