package com.cep.messaging.impls.gossip.net;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

/**
 * This interface helps determine location of node in the data center relative to another node.
 * Give a node A and another node B it can tell if A and B are on the same rack or in the same
 * data center.
 */

public interface IEndpointSnitch
{
    /**
     * returns a String repesenting the rack this endpoint belongs to
     */
    public String getRack(InetAddress endpoint);

    /**
     * returns a String representing the datacenter this endpoint belongs to
     */
    public String getDatacenter(InetAddress endpoint);

    /**
     * returns a new <tt>List</tt> sorted by proximity to the given endpoint
     */
    public List<InetAddress> getSortedListByProximity(InetAddress address, Collection<InetAddress> unsortedAddress);

    /**
     * This method will sort the <tt>List</tt> by proximity to the given address.
     */
    public void sortByProximity(InetAddress address, List<InetAddress> addresses);

    /**
     * compares two endpoints in relation to the target endpoint, returning as Comparator.compare would
     */
    public int compareEndpoints(InetAddress target, InetAddress a1, InetAddress a2);

    /**
     * called after Gossiper instance exists immediately before it starts gossiping
     */
    public void gossiperStarting();
}