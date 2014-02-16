package com.cep.messaging.impls.gossip.transport;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.cep.messaging.impls.gossip.transport.messages.Message;

public class SinkManager {
	private static List<IMessageSink> sinks = new ArrayList<IMessageSink>();

	public static void add(IMessageSink ms) {
		sinks.add(ms);
	}

	public static void clear() {
		sinks.clear();
	}

	public static Message processClientMessage(Message message, String id, InetAddress to) {
		if (sinks.isEmpty()) {
			return message;
		}
		for (IMessageSink ms : sinks) {
			message = ms.handleMessage(message, id, to);
			if (message == null) {
				return null;
			}
		}
		return message;
	}

	public static Message processServerMessage(Message message, String id) {
		if (sinks.isEmpty()) {
			return message;
		}
		for (IMessageSink ms : sinks) {
			message = ms.handleMessage(message, id, null);
			if (message == null) {
				return null;
			}
		}
		return message;
	}
}
