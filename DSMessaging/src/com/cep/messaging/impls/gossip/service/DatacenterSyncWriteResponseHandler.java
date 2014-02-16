/**
 *
 */
package com.cep.messaging.impls.gossip.service;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
 * This class blocks for a quorum of responses _in all datacenters_ (CL.EACH_QUORUM).
 */
public class DatacenterSyncWriteResponseHandler extends AbstractWriteResponseHandler
{
    private static final IEndpointSnitch snitch = NodeDescriptor.getEndpointSnitch();

    private static final String localdc;
    static
    {
        localdc = snitch.getDatacenter(GossipUtilities.getLocalAddress());
    }

	private final NetworkTopologyStrategy strategy;
    private HashMap<String, AtomicInteger> responses = new HashMap<String, AtomicInteger>();

    protected DatacenterSyncWriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        // Response is been managed by the map so make it 1 for the superclass.
        super(writeEndpoints, hintedEndpoints, consistencyLevel);
        assert consistencyLevel == ConsistencyLevel.EACH_QUORUM;

        strategy = (NetworkTopologyStrategy) Table.open(table).getReplicationStrategy();

        for (String dc : strategy.getDatacenters())
        {
            int rf = strategy.getReplicationFactor(dc);
            responses.put(dc, new AtomicInteger((rf / 2) + 1));
        }
    }

    public static IWriteResponseHandler create(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel, String table)
    {
        return new DatacenterSyncWriteResponseHandler(writeEndpoints, hintedEndpoints, consistencyLevel, table);
    }

    public void response(Message message)
    {
        String dataCenter = message == null
                            ? localdc
                            : snitch.getDatacenter(message.getFrom());

        responses.get(dataCenter).getAndDecrement();

        for (AtomicInteger i : responses.values())
        {
            if (0 < i.get())
                return;
        }

        // all the quorum conditions are met
        condition.signal();
    }

    public void assureSufficientLiveNodes() throws UnavailableException
    {   
		Map<String, AtomicInteger> dcEndpoints = new HashMap<String, AtomicInteger>();
        for (String dc: strategy.getDatacenters())
            dcEndpoints.put(dc, new AtomicInteger());
        for (InetAddress destination : hintedEndpoints.keySet())
        {
            if (writeEndpoints.contains(destination))
            {
                // figure out the destination dc
                String destinationDC = snitch.getDatacenter(destination);
                dcEndpoints.get(destinationDC).incrementAndGet();
            }
        }

        // Throw exception if any of the DC doesn't have livenodes to accept write.
        for (String dc: strategy.getDatacenters())
        {
        	if (dcEndpoints.get(dc).get() != responses.get(dc).get())
                throw new UnavailableException();
        }
    }

    public boolean isLatencyForSnitch()
    {
        return false;
    }
}
