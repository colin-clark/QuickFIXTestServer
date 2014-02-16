package com.cep.messaging.impls.gossip.transport.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import com.cep.messaging.impls.gossip.serialization.CompactEndpointSerializationHelper;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;

/**
 * Contains information about a specified list of Endpoints and the largest
 * version of the state they have generated as known by the local endpoint.
 */

public class GossipDigest implements Comparable<GossipDigest> {
	private static ICompactSerializer<GossipDigest> serializer;
	static {
		serializer = new GossipDigestSerializer();
	}

	public InetAddress endpoint;
	int generation;
	int maxVersion;

	public static ICompactSerializer<GossipDigest> serializer() {
		return serializer;
	}

	public GossipDigest(InetAddress ep, int gen, int version) {
		endpoint = ep;
		generation = gen;
		maxVersion = version;
	}

	public InetAddress getEndpoint() {
		return endpoint;
	}

	public int getGeneration() {
		return generation;
	}

	public int getMaxVersion() {
		return maxVersion;
	}

	public int compareTo(GossipDigest gDigest) {
		if (generation != gDigest.generation) {
			return (generation - gDigest.generation);
		}
		return (maxVersion - gDigest.maxVersion);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(endpoint);
		sb.append(":");
		sb.append(generation);
		sb.append(":");
		sb.append(maxVersion);
		return sb.toString();
	}
}

class GossipDigestSerializer implements ICompactSerializer<GossipDigest> {
	public void serialize(GossipDigest gDigest, DataOutputStream dos, int version) throws IOException {
		CompactEndpointSerializationHelper.serialize(gDigest.endpoint, dos);
		dos.writeInt(gDigest.generation);
		dos.writeInt(gDigest.maxVersion);
	}

	public GossipDigest deserialize(DataInputStream dis, int version) throws IOException {
		InetAddress endpoint = CompactEndpointSerializationHelper.deserialize(dis);
		int generation = dis.readInt();
		int maxVersion = dis.readInt();
		return new GossipDigest(endpoint, generation, maxVersion);
	}
}
