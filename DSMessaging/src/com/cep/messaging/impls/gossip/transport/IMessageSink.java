package com.cep.messaging.impls.gossip.transport;

import java.net.InetAddress;

import com.cep.messaging.impls.gossip.transport.messages.Message;

public interface IMessageSink {
	public Message handleMessage(Message message, String id, InetAddress to);
}
