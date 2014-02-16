package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigest;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigestAck2Message;
import com.cep.messaging.impls.gossip.transport.messages.GossipDigestAckMessage;
import com.cep.messaging.impls.gossip.transport.messages.Message;

public class GossipDigestAckVerbHandler implements IVerbHandler {
	private static Logger logger_ = LoggerFactory.getLogger(GossipDigestAckVerbHandler.class);

	public void doVerb(Message message, String id) {
		InetAddress from = message.getFrom();
		if (logger_.isTraceEnabled()) {
			logger_.trace("Received a GossipDigestAckMessage from " + from);
		}
		if (!Gossiper.instance.isEnabled()) {
			if (logger_.isTraceEnabled()) {
				logger_.trace("Ignoring GossipDigestAckMessage because gossip is disabled");
			}
			return;
		}

		byte[] bytes = message.getMessageBody();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			GossipDigestAckMessage gDigestAckMessage = GossipDigestAckMessage.serializer().deserialize(dis, message.getVersion());
			List<GossipDigest> gDigestList = gDigestAckMessage.getGossipDigestList();
			Map<InetAddress, EndpointState> epStateMap = gDigestAckMessage.getEndpointStateMap();

			if (epStateMap.size() > 0) {
				/* Notify the Failure Detector */
				Gossiper.instance.notifyFailureDetector(epStateMap);
				Gossiper.instance.applyStateLocally(epStateMap);
			}

			/*
			 * Get the state required to send to this gossipee - construct
			 * GossipDigestAck2Message
			 */
			Map<InetAddress, EndpointState> deltaEpStateMap = new HashMap<InetAddress, EndpointState>();
			for (GossipDigest gDigest : gDigestList) {
				InetAddress addr = gDigest.getEndpoint();
				EndpointState localEpStatePtr = Gossiper.instance.getStateForVersionBiggerThan(addr, gDigest.getMaxVersion());
				if (localEpStatePtr != null) {
					deltaEpStateMap.put(addr, localEpStatePtr);
				}
			}

			GossipDigestAck2Message gDigestAck2 = new GossipDigestAck2Message(deltaEpStateMap);
			Message gDigestAck2Message = Gossiper.instance.makeGossipDigestAck2Message(gDigestAck2, message.getVersion());
			if (logger_.isTraceEnabled()) {
				logger_.trace("Sending a GossipDigestAck2Message to " + from);
			}
			MessagingService.instance().sendOneWay(gDigestAck2Message, from);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
