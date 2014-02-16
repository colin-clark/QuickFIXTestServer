package com.cep.messaging.impls.gossip.node.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

/**
 * This abstraction represents the state associated with a particular node which
 * an application wants to make available to the rest of the nodes in the
 * cluster. Whenever a piece of state needs to be disseminated to the rest of
 * cluster wrap the state in an instance of <i>ApplicationState</i> and add it
 * to the Gossiper.
 * 
 * e.g. if we want to disseminate load information for node A do the following:
 * 
 * ApplicationState loadState = new ApplicationState(<string representation of
 * load>); Gossiper.instance.addApplicationState("LOAD STATE", loadState);
 */

public class VersionedValue implements Comparable<VersionedValue> {
	public static final ICompactSerializer<VersionedValue> serializer = new VersionedValueSerializer();

	// this must be a char that cannot be present in any token
	public final static char DELIMITER = ',';
	public final static String DELIMITER_STR = new String(new char[] { DELIMITER });

	// values for State.STATUS
	public final static String STATUS_BOOTSTRAPPING = "BOOT";
	public final static String STATUS_NORMAL = "NORMAL";
	public final static String STATUS_LEAVING = "LEAVING";
	public final static String STATUS_LEFT = "LEFT";
	public final static String STATUS_MOVING = "MOVING";

	public final static String REMOVING_TOKEN = "removing";
	public final static String REMOVED_TOKEN = "removed";

	public final int version;
	public final String value;

	private VersionedValue(String value, int version) {
		this.value = value;
		this.version = version;
	}

	private VersionedValue(String value) {
		this.value = value;
		version = VersionGenerator.getNextVersion();
	}

	public int compareTo(VersionedValue value) {
		return this.version - value.version;
	}

	@Override
	public String toString() {
		return "Value(" + value + "," + version + ")";
	}

	public static class VersionedValueFactory {
		@SuppressWarnings("rawtypes")
		IPartitioner partitioner;

		@SuppressWarnings("rawtypes")
		public VersionedValueFactory(IPartitioner partitioner) {
			this.partitioner = partitioner;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public VersionedValue bootstrapping(Token token) {
			return new VersionedValue(VersionedValue.STATUS_BOOTSTRAPPING
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(token));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public VersionedValue normal(Token token) {
			return new VersionedValue(VersionedValue.STATUS_NORMAL
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(token));
		}

		public VersionedValue load(double load) {
			return new VersionedValue(String.valueOf(load));
		}

		public VersionedValue migration(UUID newVersion) {
			return new VersionedValue(newVersion.toString());
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public VersionedValue leaving(Token token) {
			return new VersionedValue(VersionedValue.STATUS_LEAVING
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(token));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public VersionedValue left(Token token) {
			return new VersionedValue(VersionedValue.STATUS_LEFT
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(token));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public VersionedValue moving(Token token) {
			return new VersionedValue(VersionedValue.STATUS_MOVING
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(token));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public VersionedValue removingNonlocal(Token localToken, Token token) {
			return new VersionedValue(VersionedValue.STATUS_NORMAL
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(localToken)
					+ VersionedValue.DELIMITER + VersionedValue.REMOVING_TOKEN
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(token));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public VersionedValue removedNonlocal(Token localToken, Token token) {
			return new VersionedValue(VersionedValue.STATUS_NORMAL
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(localToken)
					+ VersionedValue.DELIMITER + VersionedValue.REMOVED_TOKEN
					+ VersionedValue.DELIMITER
					+ partitioner.getTokenFactory().toString(token));
		}

		public VersionedValue datacenter(String dcId) {
			return new VersionedValue(dcId);
		}

		public VersionedValue rack(String rackId) {
			return new VersionedValue(rackId);
		}

		public VersionedValue releaseVersion() {
			return new VersionedValue(GossipUtilities.getReleaseVersionString());
		}
	}

	private static class VersionedValueSerializer implements ICompactSerializer<VersionedValue> {
		public void serialize(VersionedValue value, DataOutputStream dos, int version) throws IOException {
			dos.writeUTF(value.value);
			dos.writeInt(value.version);
		}

		public VersionedValue deserialize(DataInputStream dis, int version) throws IOException {
			String value = dis.readUTF();
			int valVersion = dis.readInt();
			return new VersionedValue(value, valVersion);
		}
	}
}
