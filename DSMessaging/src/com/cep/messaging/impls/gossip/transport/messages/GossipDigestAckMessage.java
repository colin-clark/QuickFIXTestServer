package com.cep.messaging.impls.gossip.transport.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;

/**
 * This message gets sent out as a result of the receipt of a
 * GossipDigestSynMessage by an endpoint. This is the 2 stage of the 3 way
 * messaging in the Gossip protocol.
 */

public class GossipDigestAckMessage {
	private static ICompactSerializer<GossipDigestAckMessage> serializer_;
	static {
		serializer_ = new GossipDigestAckMessageSerializer();
	}

	List<GossipDigest> gDigestList_ = new ArrayList<GossipDigest>();
	Map<InetAddress, EndpointState> epStateMap_ = new HashMap<InetAddress, EndpointState>();

	public static ICompactSerializer<GossipDigestAckMessage> serializer() {
		return serializer_;
	}

	public GossipDigestAckMessage(List<GossipDigest> gDigestList, Map<InetAddress, EndpointState> epStateMap) {
		gDigestList_ = gDigestList;
		epStateMap_ = epStateMap;
	}

	public List<GossipDigest> getGossipDigestList() {
		return gDigestList_;
	}

	public Map<InetAddress, EndpointState> getEndpointStateMap() {
		return epStateMap_;
	}
}

class GossipDigestAckMessageSerializer implements ICompactSerializer<GossipDigestAckMessage> {
	public void serialize(GossipDigestAckMessage gDigestAckMessage, DataOutputStream dos, int version) throws IOException {
		GossipDigestSerializationHelper.serialize(gDigestAckMessage.gDigestList_, dos, version);
		dos.writeBoolean(true); // 0.6 compatibility
		EndpointStatesSerializationHelper.serialize(gDigestAckMessage.epStateMap_, dos, version);
	}

	public GossipDigestAckMessage deserialize(DataInputStream dis, int version) throws IOException {
		List<GossipDigest> gDigestList = GossipDigestSerializationHelper.deserialize(dis, version);
		dis.readBoolean(); // 0.6 compatibility
		Map<InetAddress, EndpointState> epStateMap = EndpointStatesSerializationHelper.deserialize(dis, version);
		return new GossipDigestAckMessage(gDigestList, epStateMap);
	}
}
