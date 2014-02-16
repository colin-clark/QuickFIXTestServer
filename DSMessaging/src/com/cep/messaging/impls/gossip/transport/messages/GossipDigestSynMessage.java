package com.cep.messaging.impls.gossip.transport.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.node.state.EndpointState;
import com.cep.messaging.impls.gossip.serialization.CompactEndpointSerializationHelper;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;

/**
 * This is the first message that gets sent out as a start of the Gossip
 * protocol in a round.
 */

public class GossipDigestSynMessage {
	private static ICompactSerializer<GossipDigestSynMessage> serializer_;
	static {
		serializer_ = new GossipDigestSynMessageSerializer();
	}

	public String clusterId_;
	List<GossipDigest> gDigests_ = new ArrayList<GossipDigest>();

	public static ICompactSerializer<GossipDigestSynMessage> serializer() {
		return serializer_;
	}

	public GossipDigestSynMessage(String clusterId, List<GossipDigest> gDigests) {
		clusterId_ = clusterId;
		gDigests_ = gDigests;
	}

	public List<GossipDigest> getGossipDigests() {
		return gDigests_;
	}
}

class GossipDigestSerializationHelper {
	@SuppressWarnings("unused")
	private static Logger logger_ = LoggerFactory.getLogger(GossipDigestSerializationHelper.class);

	static void serialize(List<GossipDigest> gDigestList, DataOutputStream dos, int version) throws IOException {
		dos.writeInt(gDigestList.size());
		for (GossipDigest gDigest : gDigestList) {
			GossipDigest.serializer().serialize(gDigest, dos, version);
		}
	}

	static List<GossipDigest> deserialize(DataInputStream dis, int version) throws IOException {
		int size = dis.readInt();
		List<GossipDigest> gDigests = new ArrayList<GossipDigest>(size);

		for (int i = 0; i < size; ++i) {
			assert dis.available() > 0;
			gDigests.add(GossipDigest.serializer().deserialize(dis, version));
		}
		return gDigests;
	}
}

class EndpointStatesSerializationHelper {
	@SuppressWarnings("unused")
	private static final Logger logger_ = LoggerFactory.getLogger(EndpointStatesSerializationHelper.class);

	static void serialize(Map<InetAddress, EndpointState> epStateMap, DataOutputStream dos, int version) throws IOException {
		dos.writeInt(epStateMap.size());
		for (Entry<InetAddress, EndpointState> entry : epStateMap.entrySet()) {
			InetAddress ep = entry.getKey();
			CompactEndpointSerializationHelper.serialize(ep, dos);
			EndpointState.serializer().serialize(entry.getValue(), dos, version);
		}
	}

	static Map<InetAddress, EndpointState> deserialize(DataInputStream dis, int version) throws IOException {
		int size = dis.readInt();
		Map<InetAddress, EndpointState> epStateMap = new HashMap<InetAddress, EndpointState>(size);

		for (int i = 0; i < size; ++i) {
			assert dis.available() > 0;
			InetAddress ep = CompactEndpointSerializationHelper.deserialize(dis);
			EndpointState epState = EndpointState.serializer().deserialize(dis, version);
			epStateMap.put(ep, epState);
		}
		return epStateMap;
	}
}

class GossipDigestSynMessageSerializer implements ICompactSerializer<GossipDigestSynMessage> {
	public void serialize(GossipDigestSynMessage gDigestSynMessage, DataOutputStream dos, int version) throws IOException {
		dos.writeUTF(gDigestSynMessage.clusterId_);
		GossipDigestSerializationHelper.serialize(gDigestSynMessage.gDigests_, dos, version);
	}

	public GossipDigestSynMessage deserialize(DataInputStream dis, int version) throws IOException {
		String clusterId = dis.readUTF();
		List<GossipDigest> gDigests = GossipDigestSerializationHelper.deserialize(dis, version);
		return new GossipDigestSynMessage(clusterId, gDigests);
	}

}
