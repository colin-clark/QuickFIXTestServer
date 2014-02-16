package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.commands.RowMutation;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.WriteResponse;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;


public class RowMutationVerbHandler implements IVerbHandler
{
    private static Logger logger_ = LoggerFactory.getLogger(RowMutationVerbHandler.class);

    public void doVerb(Message message, String id)
    {
        try
        {
            RowMutation rm = RowMutation.fromBytes(message.getMessageBody(), message.getVersion());
            if (logger_.isDebugEnabled())
              logger_.debug("Applying " + rm);

            /* Check if there were any hints in this message */
            byte[] hintedBytes = message.getHeader(RowMutation.HINT);
            if (hintedBytes != null)
            {
                assert hintedBytes.length > 0;
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(hintedBytes));
                while (dis.available() > 0)
                {
                    ByteBuffer addressBytes = ByteBufferUtil.readWithShortLength(dis);
                    if (logger_.isDebugEnabled())
                        logger_.debug("Adding hint for " + InetAddress.getByName(ByteBufferUtil.string(addressBytes)));
                    RowMutation hintedMutation = new RowMutation(Table.SYSTEM_TABLE, addressBytes);
                    hintedMutation.addHints(rm);
                    hintedMutation.apply();
                }
            }
        
            // Check if there were any forwarding headers in this message
            byte[] forwardBytes = message.getHeader(RowMutation.FORWARD_HEADER);
            if (forwardBytes != null)
                forwardToLocalNodes(message, forwardBytes);

            rm.apply();

            WriteResponse response = new WriteResponse(rm.getTable(), rm.key(), true);
            Message responseMessage = WriteResponse.makeWriteResponseMessage(message, response);
            if (logger_.isDebugEnabled())
              logger_.debug(rm + " applied.  Sending response to " + id + "@" + message.getFrom());
            MessagingService.instance().sendReply(responseMessage, id, message.getFrom());
        }
        catch (IOException e)
        {
            logger_.error("Error in row mutation", e);
        }
    }  
    
    private void forwardToLocalNodes(Message message, byte[] forwardBytes) throws UnknownHostException
    {
        // remove fwds from message to avoid infinite loop
        message.removeHeader(RowMutation.FORWARD_HEADER);

        int bytesPerInetAddress = GossipUtilities.getLocalAddress().getAddress().length;
        assert forwardBytes.length >= bytesPerInetAddress;
        assert forwardBytes.length % bytesPerInetAddress == 0;

        int offset = 0;
        byte[] addressBytes = new byte[bytesPerInetAddress];

        // Send a message to each of the addresses on our Forward List
        while (offset < forwardBytes.length)
        {
            System.arraycopy(forwardBytes, offset, addressBytes, 0, bytesPerInetAddress);
            InetAddress address = InetAddress.getByAddress(addressBytes);

            if (logger_.isDebugEnabled())
                logger_.debug("Forwarding message to " + address);

            // Send the original message to the address specified by the FORWARD_HINT
            // Let the response go back to the coordinator
            MessagingService.instance().sendOneWay(message, address);

            offset += bytesPerInetAddress;
        }
    }
}
