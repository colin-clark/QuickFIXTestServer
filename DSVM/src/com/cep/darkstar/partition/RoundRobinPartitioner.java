package com.cep.darkstar.partition;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.commons.EventObject;

public class RoundRobinPartitioner {
	private static final Logger logger = LoggerFactory.getLogger(RoundRobinPartitioner.class);
	
	private int throttle;	
	private static ExecutorService service;
	private final ConcurrentLinkedQueue<EventObject> queue;
	
	int queues_size = 0;
	int queues_index = -1;

	public RoundRobinPartitioner(Integer max_threads) {
		throttle = max_threads;
		service = Executors.newFixedThreadPool(throttle);
		queue = new ConcurrentLinkedQueue<EventObject>();
		
		logger.info("Round Robin Partitioner creating dispatchers...");
		createDispatchers();
	}

	private void createDispatchers() {
		for (int i = 0; i < throttle; i++) {
			service.execute(new Dispatcher(queue));
		}
		logger.info("Round Robin Partitioner finished creating dispatchers with max threads of " + throttle);
	}

	private void queue(EventObject e) {
		queue.add(e);
	}

	public void sendToCluster(EventObject event) {
		queue(event);
	}
	
}
