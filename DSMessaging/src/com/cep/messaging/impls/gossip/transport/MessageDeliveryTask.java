package com.cep.messaging.impls.gossip.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.transport.handlers.IVerbHandler;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.Verb;

public class MessageDeliveryTask implements Runnable {
	@SuppressWarnings("unused")
	private static final Logger logger_ = LoggerFactory.getLogger(MessageDeliveryTask.class);
	private static final int DEFAULT_TIMEOUT = 10000;
	private Message message;
	private final long constructionTime = System.currentTimeMillis();
	private final String id;

	public MessageDeliveryTask(Message message, String id) {
		assert message != null;
		this.message = message;
		this.id = id;
	}

	public void run() {
		Verb verb = message.getVerb();
		switch (verb) {
		case BINARY:
		case MUTATION:
		case READ:
		case RANGE_SLICE:
		case READ_REPAIR:
		case REQUEST_RESPONSE:
		case SAVE:
		case SEND:
			if (System.currentTimeMillis() > constructionTime + DEFAULT_TIMEOUT) {
				MessagingService.instance().incrementDroppedMessages(verb);
				return;
			}
			break;

		// don't bother.
		case UNUSED_3:
			return;

		default:
			break;
		}

		IVerbHandler verbHandler = MessagingService.instance().getVerbHandler(verb);
		assert verbHandler != null : "unknown verb " + verb;
		verbHandler.doVerb(message, id);
	}
}
