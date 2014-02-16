package com.cep.messaging.impls.gossip.transport.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;

/**
 * This message gets sent out as a result of the receipt of a
 * GossipDigestAckMessage. This the last stage of the 3 way messaging of the
 * Gossip protocol.
 */

public class GossipDigestAck2Message {
	private static ICompactSerializer<GossipDigestAck2Message> serializer_;
	static {
		serializer_ = new GossipDigestAck2MessageSerializer();
	}

	Map<InetAddress, EndpointState> epStateMap_ = new HashMap<InetAddress, EndpointState>();

	public static ICompactSerializer<GossipDigestAck2Message> serializer() {
		return serializer_;
	}

	public GossipDigestAck2Message(Map<InetAddress, EndpointState> epStateMap) {
		epStateMap_ = epStateMap;
	}

	public Map<InetAddress, EndpointState> getEndpointStateMap() {
		return epStateMap_;
	}
}

class GossipDigestAck2MessageSerializer implements ICompactSerializer<GossipDigestAck2Message> {
	public void serialize(GossipDigestAck2Message gDigestAck2Message, DataOutputStream dos, int version) throws IOException {
		/* Use the EndpointState */
		EndpointStatesSerializationHelper.serialize(gDigestAck2Message.epStateMap_, dos, version);
	}

	public GossipDigestAck2Message deserialize(DataInputStream dis, int version) throws IOException {
		Map<InetAddress, EndpointState> epStateMap = EndpointStatesSerializationHelper.deserialize(dis, version);
		return new GossipDigestAck2Message(epStateMap);
	}
}
