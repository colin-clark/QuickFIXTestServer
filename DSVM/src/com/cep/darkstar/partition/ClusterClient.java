/**
 * Cloud Event Processing, Inc.
 * 
 */
package com.cep.darkstar.partition;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.cep.darkstar.pubsub.sub.SubscribeTopic;
import com.cep.messaging.impls.gossip.node.GossipService;
import com.cep.messaging.interfaces.IMessagingService;
import com.rabbitmq.client.ShutdownSignalException;

public class ClusterClient {
	private static final Logger log = Logger.getLogger(com.cep.darkstar.partition.ClusterClient.class);
	public static final ClusterClient instance = new ClusterClient();
	private SubscribeTopic listen;
	private volatile boolean shouldRun = true;
	private static RoundRobinPartitioner partitioner;
	private static IMessagingService messaging = GossipService.instance();
	
	private ClusterClient() {
		try {
			listen = new SubscribeTopic.Builder().topic("#").build();
			messaging.start(false);
		} catch (IOException e) {
			log.error("Unable to create listener, exiting", e);
			System.exit(1);
		}
	}
	
	public void run(Integer max_threads) {
		partitioner = new RoundRobinPartitioner(max_threads);
		while(shouldRun){
			try {
				EventObject event = new EventObject(new String(listen.nextDelivery()));
				partitioner.sendToCluster(event);
			} catch (ShutdownSignalException e) {
				log.error("ShutdownSignalException: " + e.getMessage(), e);
			} catch (JSONException e) {
				log.error("JSONException: " + e.getMessage(), e);
			} catch (InterruptedException e) {
				log.error("InterruptedException: " + e.getMessage(), e);
			} catch (IOException e) {
				log.error("IOException: " + e.getMessage(), e);
			}
		}
	}

	public static void main(String[] args) {
		try {
			Integer max_threads = GossipService.instance().getMaxClientThreads();
			instance.run(max_threads);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}