package com.cep.messaging.impls.gossip.configuration;

import java.net.InetAddress;
import java.util.List;

public interface SeedProvider {
	List<InetAddress> getSeeds();
}
