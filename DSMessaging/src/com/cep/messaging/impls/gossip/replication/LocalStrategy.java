package com.cep.messaging.impls.gossip.replication;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.cep.messaging.impls.gossip.net.IEndpointSnitch;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.partitioning.token.TokenMetadata;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.exception.ConfigurationException;

public class LocalStrategy extends AbstractReplicationStrategy
{
    public LocalStrategy(String table, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions)
    {
        super(table, tokenMetadata, snitch, configOptions);
    }

    @SuppressWarnings("rawtypes")
	public List<InetAddress> calculateNaturalEndpoints(Token token, TokenMetadata metadata)
    {
        return Arrays.asList(GossipUtilities.getLocalAddress());
    }

    public int getReplicationFactor()
    {
        return 1;
    }

    public void validateOptions() throws ConfigurationException
    {
        // LocalStrategy doesn't expect any options.
    }
}
