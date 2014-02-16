package com.cep.messaging.impls.gossip.transport.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.serialization.CompactEndpointSerializationHelper;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;
import com.cep.messaging.impls.gossip.util.Verb;

public class Header {
	private static ICompactSerializer<Header> serializer_;

	static {
		serializer_ = new HeaderSerializer();
	}

	static ICompactSerializer<Header> serializer() {
		return serializer_;
	}

	private final InetAddress from_;
	// TODO STAGE can be determined from verb
	private final Verb verb_;
	protected Map<String, byte[]> details_ = new Hashtable<String, byte[]>();

	Header(InetAddress from, Verb verb) {
		assert from != null;
		assert verb != null;

		from_ = from;
		verb_ = verb;
	}

	Header(InetAddress from, Verb verb, Map<String, byte[]> details) {
		this(from, verb);
		details_ = details;
	}

	InetAddress getFrom() {
		return from_;
	}

	Verb getVerb() {
		return verb_;
	}

	byte[] getDetail(String key) {
		return details_.get(key);
	}

	void setDetail(String key, byte[] value) {
		details_.put(key, value);
	}

	void removeDetail(String key) {
		details_.remove(key);
	}
}

class HeaderSerializer implements ICompactSerializer<Header> {
	public void serialize(Header t, DataOutputStream dos, int version) throws IOException {
		CompactEndpointSerializationHelper.serialize(t.getFrom(), dos);
		dos.writeInt(t.getVerb().ordinal());

		/* Serialize the message header */
		int size = t.details_.size();
		dos.writeInt(size);
		Set<String> keys = t.details_.keySet();

		for (String key : keys) {
			dos.writeUTF(key);
			byte[] value = t.details_.get(key);
			dos.writeInt(value.length);
			dos.write(value);
		}
	}

	public Header deserialize(DataInputStream dis, int version) throws IOException {
		InetAddress from = CompactEndpointSerializationHelper.deserialize(dis);
		int verbOrdinal = dis.readInt();

		/* Deserializing the message header */
		int size = dis.readInt();
		Map<String, byte[]> details = new Hashtable<String, byte[]>(size);
		for (int i = 0; i < size; ++i) {
			String key = dis.readUTF();
			int length = dis.readInt();
			byte[] bytes = new byte[length];
			dis.readFully(bytes);
			details.put(key, bytes);
		}

		return new Header(from, StorageService.VERBS[verbOrdinal], details);
	}
}
