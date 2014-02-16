package com.cep.messaging.impls.gossip.service;

import java.io.IOException;

import com.cep.messaging.impls.gossip.thrift.DarkStarMessagingDaemon;

public class EmbeddedDarkStarMessagingService
{

    DarkStarMessagingDaemon darkStarDaemon;

    public void start() throws IOException
    {
        darkStarDaemon = new DarkStarMessagingDaemon();
        darkStarDaemon.init(null);
        darkStarDaemon.start();
    }
}
