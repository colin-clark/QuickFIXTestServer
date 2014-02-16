package com.cep.messaging.impls.gossip.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSeedProvider implements SeedProvider {
	private static final Logger logger = LoggerFactory.getLogger(SimpleSeedProvider.class);

	private List<InetAddress> seeds;

	public SimpleSeedProvider(Map<String, String> args) {
		seeds = new ArrayList<InetAddress>();
		String[] hosts = args.get("seeds").split(",", -1);
		for (String host : hosts) {
			try {
				seeds.add(InetAddress.getByName(host));
			} catch (UnknownHostException ex) {
				// not fatal... DD will bark if there end up being zero seeds.
				logger.warn("Seed provider couldn't lookup host " + host);
			}
		}
	}

	public List<InetAddress> getSeeds() {
		return Collections.unmodifiableList(seeds);
	}

	// future planning?
	public void addSeed(InetAddress addr) {
		if (!seeds.contains(addr)) {
			seeds.add(addr);
		}
	}
}
