package com.cep.messaging.impls.gossip.node.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;

/**
 * This abstraction represents both the HeartBeatState and the ApplicationState
 * in an EndpointState instance. Any state for a given endpoint can be retrieved
 * from this instance.
 */

public class EndpointState {
	protected static Logger logger = LoggerFactory.getLogger(EndpointState.class);

	private final static ICompactSerializer<EndpointState> serializer = new EndpointStateSerializer();

	private volatile HeartBeatState hbState;
	public final Map<ApplicationState, VersionedValue> applicationState = new NonBlockingHashMap<ApplicationState, VersionedValue>();

	/* fields below do not get serialized */
	private volatile long updateTimestamp;
	private volatile boolean isAlive;

	// whether this endpoint has token associated with it or not. Initially set
	// false for all
	// endpoints. After certain time of inactivity, gossiper will examine if
	// this node has a
	// token or not and will set this true if token is found. If there is no
	// token, this is a
	// fat client and will be removed automatically from gossip.
	private volatile boolean hasToken;

	public static ICompactSerializer<EndpointState> serializer() {
		return serializer;
	}

	public EndpointState(HeartBeatState initialHbState) {
		hbState = initialHbState;
		updateTimestamp = System.currentTimeMillis();
		isAlive = true;
		hasToken = false;
	}

	public HeartBeatState getHeartBeatState() {
		return hbState;
	}

	public void setHeartBeatState(HeartBeatState newHbState) {
		updateTimestamp();
		hbState = newHbState;
	}

	public VersionedValue getApplicationState(ApplicationState key) {
		return applicationState.get(key);
	}

	@Deprecated
	public Map<ApplicationState, VersionedValue> getApplicationStateMap() {
		return applicationState;
	}

	public void addApplicationState(ApplicationState key, VersionedValue value) {
		applicationState.put(key, value);
	}

	/* getters and setters */
	public long getUpdateTimestamp() {
		return updateTimestamp;
	}

	void updateTimestamp() {
		updateTimestamp = System.currentTimeMillis();
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void markAlive() {
		isAlive = true;
	}

	public void markDead() {
		isAlive = false;
	}

	public void setHasToken(boolean value) {
		hasToken = value;
	}

	public boolean hasToken() {
		return hasToken;
	}
}

class EndpointStateSerializer implements ICompactSerializer<EndpointState> {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(EndpointStateSerializer.class);

	public void serialize(EndpointState epState, DataOutputStream dos, int version) throws IOException {
		/* serialize the HeartBeatState */
		HeartBeatState hbState = epState.getHeartBeatState();
		HeartBeatState.serializer().serialize(hbState, dos, version);

		/* serialize the map of ApplicationState objects */
		int size = epState.applicationState.size();
		dos.writeInt(size);
		for (Map.Entry<ApplicationState, VersionedValue> entry : epState.applicationState.entrySet()) {
			VersionedValue value = entry.getValue();
			if (value != null) {
				dos.writeInt(entry.getKey().ordinal());
				VersionedValue.serializer.serialize(value, dos, version);
			}
		}
	}

	public EndpointState deserialize(DataInputStream dis, int version) throws IOException {
		HeartBeatState hbState = HeartBeatState.serializer().deserialize(dis, version);
		EndpointState epState = new EndpointState(hbState);

		int appStateSize = dis.readInt();
		for (int i = 0; i < appStateSize; ++i) {
			if (dis.available() == 0) {
				break;
			}

			int key = dis.readInt();
			VersionedValue value = VersionedValue.serializer.deserialize(dis, version);
			epState.addApplicationState(Gossiper.getStates()[key], value);
		}
		return epState;
	}
}
