package com.cep.darkstar.partition;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.commons.EventObject;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;
import com.cep.messaging.util.exception.UnavailableException;

public class Dispatcher implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	private final int version;
	private ConcurrentLinkedQueue<EventObject> queue;
	
	public Dispatcher(ConcurrentLinkedQueue<EventObject> queue) {
		this.queue = queue;
		version = StorageService.instance.valueFactory.releaseVersion().version;
	}

	@Override
	public void run() {
		while(true) {
			dispatch();
		}
	}
	
	private void dispatch() {
		EventObject o = queue.poll();
		if (o != null) {
			try {
				sendToNode(o);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}		
	}

	private void sendToNode(EventObject o) throws UnavailableException, TimeoutException {
		//ThriftServerClient.send(EventToMessage(o));
	}

	@SuppressWarnings("unused")
	private Message EventToMessage(EventObject o) {
		byte[] body = o.toString().getBytes();
		return new Message(GossipUtilities.getLocalAddress(), Verb.MUTATION, body, version);
	}

	@Override
	protected void finalize() {
		while (!queue.isEmpty()) {
			dispatch();
		}
	}

}
