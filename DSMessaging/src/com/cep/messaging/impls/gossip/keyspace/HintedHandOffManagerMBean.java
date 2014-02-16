package com.cep.messaging.impls.gossip.keyspace;

import java.util.List;
import java.util.Map;

public interface HintedHandOffManagerMBean
{
    /**
     * Nuke all hints from this node to `ep`.
     * @param epaddr String rep. of endpoint address to delete hints for, either ip address ("127.0.0.1") or hostname
     */
    public void deleteHintsForEndpoint(final String epaddr);

    /**
     * List all the endpoints that this node has hints for.
     * @return set of endpoints; as Strings
     */
    public List<String> listEndpointsPendingHints();

    /**
     * List all the endpoints that this node has hints for, and
     *  count the number of hints for each such endpoint.
     *
     * @return map of endpoint -> hint count
     */
    public Map<String, Integer> countPendingHints();
}

