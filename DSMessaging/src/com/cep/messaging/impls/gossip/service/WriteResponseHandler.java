package com.cep.messaging.impls.gossip.service;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;
import com.cep.messaging.impls.gossip.thrift.UnavailableException;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Handles blocking writes for ONE, ANY, TWO, THREE, QUORUM, and ALL consistency levels.
 */
public class WriteResponseHandler extends AbstractWriteResponseHandler
{
    protected static final Logger logger = LoggerFactory.getLogger(WriteResponseHandler.class);

    protected final AtomicInteger responses;

    protected WriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        super(writeEndpoints, hintedEndpoints, consistencyLevel);
        responses = new AtomicInteger(determineBlockFor(table));
    }

    protected WriteResponseHandler(InetAddress endpoint)
    {
        super(Arrays.asList(endpoint),
              ImmutableMultimap.<InetAddress, InetAddress>builder().put(endpoint, endpoint).build(),
              ConsistencyLevel.ALL);
        responses = new AtomicInteger(1);
    }

    public static IWriteResponseHandler create(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        return new WriteResponseHandler(writeEndpoints, hintedEndpoints, consistencyLevel, table);
    }

    public static IWriteResponseHandler create(InetAddress endpoint)
    {
        return new WriteResponseHandler(endpoint);
    }

    public void response(Message m)
    {
        if (responses.decrementAndGet() == 0)
            condition.signal();
    }

    protected int determineBlockFor(String table)
    {
        switch (consistencyLevel)
        {
            case ONE:
                return 1;
            case ANY:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            case QUORUM:
                return (writeEndpoints.size() / 2) + 1;
            case ALL:
                return writeEndpoints.size();
            default:
                throw new UnsupportedOperationException("invalid consistency level: " + consistencyLevel.toString());
        }
    }

    public void assureSufficientLiveNodes() throws UnavailableException
    {
        if (consistencyLevel == ConsistencyLevel.ANY)
        {
            // ensure there are blockFor distinct living nodes (hints are ok).
            if (hintedEndpoints.keySet().size() < responses.get())
                throw new UnavailableException();
            return;
        }

        // count destinations that are part of the desired target set
        int liveNodes = 0;
        for (InetAddress destination : hintedEndpoints.keySet())
        {
            if (writeEndpoints.contains(destination))
                liveNodes++;
        }
        if (liveNodes < responses.get())
        {
            throw new UnavailableException();
        }
    }

    public boolean isLatencyForSnitch()
    {
        return false;
    }
}
