package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class CompactEndpointSerializationHelper {
	public static void serialize(InetAddress endpoint, DataOutputStream dos) throws IOException {
		byte[] buf = endpoint.getAddress();
		dos.writeByte(buf.length);
		dos.write(buf);
	}

	public static InetAddress deserialize(DataInputStream dis) throws IOException {
		byte[] bytes = new byte[dis.readByte()];
		dis.readFully(bytes, 0, bytes.length);
		return InetAddress.getByAddress(bytes);
	}
}
