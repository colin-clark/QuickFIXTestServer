package com.cep.messaging.impls.gossip.partitioning.token;

import java.nio.ByteBuffer;
import java.util.Comparator;

import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

/**
 * Represents a decorated key, handy for certain operations where just working
 * with strings gets slow.
 * 
 * We do a lot of sorting of DecoratedKeys, so for speed, we assume that tokens
 * correspond one-to-one with keys. This is not quite correct in the case of
 * RandomPartitioner (which uses MD5 to hash keys to tokens); if this matters,
 * you can subclass RP to use a stronger hash, or use a non-lossy tokenization
 * scheme (as in the OrderPreservingPartitioner classes).
 */
@SuppressWarnings("rawtypes")
public class DecoratedKey<T extends Token> implements Comparable<DecoratedKey> {
	private static IPartitioner partitioner = StorageService.getPartitioner();

	public static final Comparator<DecoratedKey> comparator = new Comparator<DecoratedKey>() {
		public int compare(DecoratedKey o1, DecoratedKey o2) {
			return o1.compareTo(o2);
		}
	};

	public final T token;
	public final ByteBuffer key;

	public DecoratedKey(T token, ByteBuffer key) {
		super();
		assert token != null;
		this.token = token;
		this.key = key;
	}

	@Override
	public int hashCode() {
		return token.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		DecoratedKey other = (DecoratedKey) obj;
		return token.equals(other.token);
	}

	@SuppressWarnings("unchecked")
	public int compareTo(DecoratedKey other) {
		return token.compareTo(other.token);
	}

	public boolean isEmpty() {
		return token.equals(partitioner.getMinimumToken());
	}

	@Override
	public String toString() {
		String keystring = key == null ? "null" : ByteBufferUtil.bytesToHex(key);
		return "DecoratedKey(" + token + ", " + keystring + ")";
	}
}
