package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigestAck2Message;
import com.cep.messaging.impls.gossip.transport.messages.Message;

public class GossipDigestAck2VerbHandler implements IVerbHandler {
	private static Logger logger_ = LoggerFactory.getLogger(GossipDigestAck2VerbHandler.class);

	public void doVerb(Message message, String id) {
		InetAddress from = message.getFrom();
		if (logger_.isTraceEnabled()) {
			logger_.trace("Received a GossipDigestAck2Message from " + from);
		}
		byte[] bytes = message.getMessageBody();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
		GossipDigestAck2Message gDigestAck2Message;
		try {
			gDigestAck2Message = GossipDigestAck2Message.serializer().deserialize(dis, message.getVersion());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Map<InetAddress, EndpointState> remoteEpStateMap = gDigestAck2Message.getEndpointStateMap();
		/* Notify the Failure Detector */
		Gossiper.instance.notifyFailureDetector(remoteEpStateMap);
		Gossiper.instance.applyStateLocally(remoteEpStateMap);
	}
}
