package com.cep.messaging.impls.gossip.node;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.cep.commons.EventObject;
import com.cep.darkstar.query.ICEPEngine;
import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.service.EmbeddedDarkStarMessagingService;
import com.cep.messaging.interfaces.IMessagingService;
import com.cep.messaging.util.exception.InvalidTokenException;

public class GossipService implements IMessagingService {
	private CountDownLatch startedLatch = new CountDownLatch(1);
	private static Logger logger = Logger.getLogger(GossipService.class);
	private volatile boolean isRunning = false;
	private boolean isServer = true;
	private ICEPEngine engine;
	EmbeddedDarkStarMessagingService clientService;

	private static GossipService instance = new GossipService();

	private GossipService() {
	}

	public static GossipService instance() {
		return instance;
	}
	
	@Override
	public void start() {
		this.start(true);
	}
	
	public void handleEventObject(EventObject event) {
		engine.handleEventObject(event);
	}

	@Override
	public synchronized void start(boolean isServer) {
		if (isRunning) {
			logger.warn("GossipService start method called after service has already been started, ignoring.");
		} else {
			logger.info("GossipService starting in " + (isServer ? " server" : " client") + " mode");
			this.isServer = isServer;
			startedLatch.countDown();
			try {
				if (this.isServer) {
					StorageService.instance.initServer();
					clientService = new EmbeddedDarkStarMessagingService();
					clientService.start();
				}
				else {
					StorageService.instance.initClient();
				}
				isRunning = true;
			} catch (Exception e) {
				logger.error("Fatal error during startup", e);
				System.exit(1);
			}	
		}
	}

	public CountDownLatch getStartedLatch() {
		return startedLatch;
	}

	// For testing purposes
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("resources/log4j.configuration");
			System.out.println("Starting Gossip Service...");
			GossipService.instance().start(true);
			System.out.println("Gossip Service Running");
			// keep it running...
			System.in.read();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public InetAddress getDestinationFor(String token) throws InvalidTokenException {
		return StorageService.instance.getDestinationFor(token);
	}

	public boolean handleLocally(String token) {
		return StorageService.instance.handleLocally(token);
	}

	@SuppressWarnings("rawtypes")
	public Set<Map.Entry<Token, InetAddress>> getNodeInfo() {
		return StorageService.instance.getTokenMetadata().entrySet();
	}
	
	public Integer getMaxClientThreads() {
		return NodeDescriptor.getMaxClientThreads();
	}
	
	public IPartitioner<?> getPartitioner() {
		return NodeDescriptor.getPartitioner();
	}

	public String getInitialToken() {
		return NodeDescriptor.getInitialToken();
	}

	public String getClusterName() {
		return NodeDescriptor.getClusterName();
	}

	public int getStoragePort() {
		return NodeDescriptor.getStoragePort();
	}

	public Set<InetAddress> getSeeds() {
		return NodeDescriptor.getSeeds();
	}

	public InetAddress getListenAddress() {
		return NodeDescriptor.getListenAddress();
	}

	@Override
	public void registerCEPEngine(ICEPEngine engine) {
		this.engine = engine;
	}

	@Override
	public void setLog4jFile(String filename) {
		PropertyConfigurator.configure(filename);
	}

}
