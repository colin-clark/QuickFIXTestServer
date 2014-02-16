package com.cep.messaging.impls.gossip.transport;

import java.io.IOException;

import com.cep.messaging.impls.gossip.transport.messages.Message;

public interface MessageProducer {
	public Message getMessage(Integer version) throws IOException;
}
