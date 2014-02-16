package com.cep.messaging.impls.gossip.service;

import java.net.InetAddress;
import java.util.List;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.net.IEndpointSnitch;
import com.cep.messaging.impls.gossip.replication.NetworkTopologyStrategy;
import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;
import com.cep.messaging.impls.gossip.thrift.UnavailableException;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.ReadResponse;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

/**
 * Datacenter Quorum response handler blocks for a quorum of responses from the local DC
 */
public class DatacenterReadCallback<T> extends ReadCallback<T>
{
    private static final IEndpointSnitch snitch = NodeDescriptor.getEndpointSnitch();
    private static final String localdc = snitch.getDatacenter(GossipUtilities.getLocalAddress());

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public DatacenterReadCallback(IResponseResolver resolver, ConsistencyLevel consistencyLevel, IReadCommand command, List<InetAddress> endpoints)
    {
        super(resolver, consistencyLevel, command, endpoints);
    }

    @Override
    protected boolean waitingFor(Message message)
    {
        return localdc.equals(snitch.getDatacenter(message.getFrom()));
    }

    @Override
    protected boolean waitingFor(ReadResponse response)
    {
        // cheat and leverage our knowledge that a local read is the only way the ReadResponse
        // version of this method gets called
        return true;
    }
        
    @Override
    public int determineBlockFor(ConsistencyLevel consistency_level, String table)
	{
        NetworkTopologyStrategy stategy = (NetworkTopologyStrategy) Table.open(table).getReplicationStrategy();
		return (stategy.getReplicationFactor(localdc) / 2) + 1;
	}

    @Override
    public void assureSufficientLiveNodes() throws UnavailableException
    {
        int localEndpoints = 0;
        for (InetAddress endpoint : endpoints)
        {
            if (localdc.equals(snitch.getDatacenter(endpoint)))
                localEndpoints++;
        }
        
        if(localEndpoints < blockfor)
            throw new UnavailableException();
    }
}
