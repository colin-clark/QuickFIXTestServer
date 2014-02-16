package com.cep.messaging.impls.gossip.node.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;

/**
 * HeartBeat State associated with any given endpoint.
 */

public class HeartBeatState {
	private static ICompactSerializer<HeartBeatState> serializer;

	static {
		serializer = new HeartBeatStateSerializer();
	}

	private int generation;
	private int version;

	public HeartBeatState(int gen) {
		this(gen, 0);
	}

	HeartBeatState(int gen, int ver) {
		generation = gen;
		version = ver;
	}

	public static ICompactSerializer<HeartBeatState> serializer() {
		return serializer;
	}

	public int getGeneration() {
		return generation;
	}

	public void updateHeartBeat() {
		version = VersionGenerator.getNextVersion();
	}

	public int getHeartBeatVersion() {
		return version;
	}
}

class HeartBeatStateSerializer implements ICompactSerializer<HeartBeatState> {
	public void serialize(HeartBeatState hbState, DataOutputStream dos, int version) throws IOException {
		dos.writeInt(hbState.getGeneration());
		dos.writeInt(hbState.getHeartBeatVersion());
	}

	public HeartBeatState deserialize(DataInputStream dis, int version) throws IOException {
		return new HeartBeatState(dis.readInt(), dis.readInt());
	}
}
