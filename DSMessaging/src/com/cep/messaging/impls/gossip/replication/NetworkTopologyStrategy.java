package com.cep.messaging.impls.gossip.replication;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.net.IEndpointSnitch;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.partitioning.token.TokenMetadata;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.exception.ConfigurationException;

/**
 * This Replication Strategy takes a property file that gives the intended
 * replication factor in each datacenter.  The sum total of the datacenter
 * replication factor values should be equal to the keyspace replication
 * factor.
 * <p/>
 * So for example, if the keyspace replication factor is 6, the
 * datacenter replication factors could be 3, 2, and 1 - so 3 replicas in
 * one datacenter, 2 in another, and 1 in another - totalling 6.
 * <p/>
 * This class also caches the Endpoints and invalidates the cache if there is a
 * change in the number of tokens.
 */
public class NetworkTopologyStrategy extends AbstractReplicationStrategy
{
    private IEndpointSnitch snitch;
    private Map<String, Integer> datacenters;
    private static final Logger logger = LoggerFactory.getLogger(NetworkTopologyStrategy.class);

    public NetworkTopologyStrategy(String table, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions) throws ConfigurationException
    {
        super(table, tokenMetadata, snitch, configOptions);
        this.snitch = snitch;

        Map<String, Integer> newDatacenters = new HashMap<String, Integer>();
        if (configOptions != null)
        {
            for (Entry<String, String> entry : configOptions.entrySet())
            {
                String dc = entry.getKey();
                if (dc.equalsIgnoreCase("replication_factor"))
                    continue; 
                Integer replicas = Integer.valueOf(entry.getValue());
                newDatacenters.put(dc, replicas);
            }
        }

        datacenters = Collections.unmodifiableMap(newDatacenters);
        logger.debug("Configured datacenter replicas are {}", GossipUtilities.toString(datacenters));
    }

    @SuppressWarnings("rawtypes")
	public List<InetAddress> calculateNaturalEndpoints(Token searchToken, TokenMetadata tokenMetadata)
    {
        List<InetAddress> endpoints = new ArrayList<InetAddress>(getReplicationFactor());

        for (Entry<String, Integer> dcEntry : datacenters.entrySet())
        {
            String dcName = dcEntry.getKey();
            int dcReplicas = dcEntry.getValue();

            // collect endpoints in this DC
            TokenMetadata dcTokens = new TokenMetadata();
            for (Entry<Token, InetAddress> tokenEntry : tokenMetadata.entrySet())
            {
                if (snitch.getDatacenter(tokenEntry.getValue()).equals(dcName))
                    dcTokens.updateNormalToken(tokenEntry.getKey(), tokenEntry.getValue());
            }

            List<InetAddress> dcEndpoints = new ArrayList<InetAddress>(dcReplicas);
            Set<String> racks = new HashSet<String>();
            // first pass: only collect replicas on unique racks
            for (Iterator<Token> iter = TokenMetadata.ringIterator(dcTokens.sortedTokens(), searchToken, false);
                 dcEndpoints.size() < dcReplicas && iter.hasNext(); )
            {
                Token token = iter.next();
                InetAddress endpoint = dcTokens.getEndpoint(token);
                String rack = snitch.getRack(endpoint);
                if (!racks.contains(rack))
                {
                    dcEndpoints.add(endpoint);
                    racks.add(rack);
                }
            }

            // second pass: if replica count has not been achieved from unique racks, add nodes from duplicate racks
            for (Iterator<Token> iter = TokenMetadata.ringIterator(dcTokens.sortedTokens(), searchToken, false);
                 dcEndpoints.size() < dcReplicas && iter.hasNext(); )
            {
                Token token = iter.next();
                InetAddress endpoint = dcTokens.getEndpoint(token);
                if (!dcEndpoints.contains(endpoint))
                    dcEndpoints.add(endpoint);
            }

            if (dcEndpoints.size() < dcReplicas)
                throw new IllegalStateException(String.format("datacenter (%s) has no more endpoints, (%s) replicas still needed",
                                                              dcName, dcReplicas - dcEndpoints.size()));
            if (logger.isDebugEnabled())
                logger.debug("{} endpoints in datacenter {} for token {} ",
                             new Object[] { StringUtils.join(dcEndpoints, ","), dcName, searchToken});
            endpoints.addAll(dcEndpoints);
        }

        return endpoints;
    }

    public int getReplicationFactor()
    {
        int total = 0;
        for (int repFactor : datacenters.values())
            total += repFactor;
        return total;
    }

    public int getReplicationFactor(String dc)
    {
        return datacenters.get(dc);
    }

    public Set<String> getDatacenters()
    {
        return datacenters.keySet();
    }

    public void validateOptions() throws ConfigurationException
    {
        for (Entry<String, String> e : this.configOptions.entrySet())
        {
            validateReplicationFactor(e.getValue());
        }

    }
}
