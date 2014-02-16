package com.cep.messaging.impls.gossip.partitioning.token;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer2;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public abstract class Token<T> implements Comparable<Token<T>>, Serializable {
	private static final long serialVersionUID = 1L;

	private static final TokenSerializer serializer = new TokenSerializer();

	public static TokenSerializer serializer() {
		return serializer;
	}

	public final T token;

	protected Token(T token) {
		this.token = token;
	}

	/**
	 * This determines the comparison for node destination purposes.
	 */
	abstract public int compareTo(Token<T> o);

	public String toString() {
		return token.toString();
	}

	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (!(obj instanceof Token)) {
			return false;
		}
		return token.equals(((Token) obj).token);
	}

	public int hashCode() {
		return token.hashCode();
	}

	public static abstract class TokenFactory<T> {
		public abstract ByteBuffer toByteArray(Token<T> token);

		public abstract Token<T> fromByteArray(ByteBuffer bytes);
		// serialize as string, not necessarily human-readable
		public abstract String toString(Token<T> token); 

		public abstract Token<T> fromString(String string); // deserialize
	}

	@SuppressWarnings("rawtypes")
	public static class TokenSerializer implements ICompactSerializer2<Token> {
		@SuppressWarnings("unchecked")
		public void serialize(Token token, DataOutput dos) throws IOException {
			IPartitioner p = StorageService.getPartitioner();
			ByteBuffer b = p.getTokenFactory().toByteArray(token);
			ByteBufferUtil.writeWithLength(b, dos);
		}

		public Token deserialize(DataInput dis) throws IOException {
			IPartitioner p = StorageService.getPartitioner();
			int size = dis.readInt();
			byte[] bytes = new byte[size];
			dis.readFully(bytes);
			return p.getTokenFactory().fromByteArray(ByteBuffer.wrap(bytes));
		}
	}
}
