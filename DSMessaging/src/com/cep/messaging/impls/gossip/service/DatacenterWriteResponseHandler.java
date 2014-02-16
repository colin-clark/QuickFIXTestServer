/**
 *
 */
package com.cep.messaging.impls.gossip.service;

import java.net.InetAddress;
import java.util.Collection;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.net.IEndpointSnitch;
import com.cep.messaging.impls.gossip.replication.NetworkTopologyStrategy;
import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;
import com.cep.messaging.impls.gossip.thrift.UnavailableException;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.google.common.collect.Multimap;

/**
 * This class blocks for a quorum of responses _in the local datacenter only_ (CL.LOCAL_QUORUM).
 */
public class DatacenterWriteResponseHandler extends WriteResponseHandler
{
    private static final IEndpointSnitch snitch = NodeDescriptor.getEndpointSnitch();

    private static final String localdc;
    static
    {
        localdc = snitch.getDatacenter(GossipUtilities.getLocalAddress());
    }

    protected DatacenterWriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        super(writeEndpoints, hintedEndpoints, consistencyLevel, table);
        assert consistencyLevel == ConsistencyLevel.LOCAL_QUORUM;
    }

    public static IWriteResponseHandler create(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        return new DatacenterWriteResponseHandler(writeEndpoints, hintedEndpoints, consistencyLevel, table);
    }

    @Override
    protected int determineBlockFor(String table)
    {
        NetworkTopologyStrategy strategy = (NetworkTopologyStrategy) Table.open(table).getReplicationStrategy();
        return (strategy.getReplicationFactor(localdc) / 2) + 1;
    }


    @Override
    public void response(Message message)
    {
        if (message == null || localdc.equals(snitch.getDatacenter(message.getFrom())))
        {
            if (responses.decrementAndGet() == 0)
                condition.signal();
        }
    }
    
    @Override
    public void assureSufficientLiveNodes() throws UnavailableException
    {
        int liveNodes = 0;
        for (InetAddress destination : hintedEndpoints.keySet())
        {
            if (localdc.equals(snitch.getDatacenter(destination)) && writeEndpoints.contains(destination))
                liveNodes++;
        }

        if (liveNodes < responses.get())
        {
            throw new UnavailableException();
        }
    }
}
