package com.cep.messaging.impls.gossip.util;

import java.util.List;


public final class Resources
{
    public final static String ROOT = "darkstar";
    public final static String KEYSPACES = "keyspaces";

    public static String toString(List<Object> resource)
    {
        StringBuilder buff = new StringBuilder();
        for (Object component : resource)
        {
            buff.append("/");
            if (component instanceof byte[])
                buff.append(GossipUtilities.bytesToHex((byte[])component));
            else
                buff.append(component.toString());
        }
        return buff.toString();
    }
}
