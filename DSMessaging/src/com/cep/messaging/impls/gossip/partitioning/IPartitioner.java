package com.cep.messaging.impls.gossip.partitioning;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.partitioning.token.Token;

@SuppressWarnings("rawtypes")
public interface IPartitioner<T extends Token> {
	/**
	 * @Deprecated: Used by SSTables before version 'e'.
	 * 
	 *              Convert the on disk representation to a DecoratedKey object
	 * @param key
	 *            On disk representation
	 * @return DecoratedKey object
	 */
	public DecoratedKey<T> convertFromDiskFormat(ByteBuffer key);

	/**
	 * Transform key to object representation of the on-disk format.
	 * 
	 * @param key
	 *            the raw, client-facing key
	 * @return decorated version of key
	 */
	public DecoratedKey<T> decorateKey(ByteBuffer key);

	/**
	 * Calculate a Token representing the approximate "middle" of the given
	 * range.
	 * 
	 * @return The approximate midpoint between left and right.
	 */
	public Token midpoint(Token left, Token right);

	/**
	 * @return The minimum possible Token in the range that is being
	 *         partitioned.
	 */
	public T getMinimumToken();

	/**
	 * @return a Token that can be used to route a given key (This is NOT a
	 *         method to create a Token from its string representation; for
	 *         that, use TokenFactory.fromString.)
	 */
	public T getToken(ByteBuffer key);

	/**
	 * @return a randomly generated token
	 */
	public T getRandomToken();

	public Token.TokenFactory getTokenFactory();

	/**
	 * @return True if the implementing class preserves key order in the Tokens
	 *         it generates.
	 */
	public boolean preservesOrder();

	/**
	 * @param token
	 * @param sortedTokens
	 * @return the range handled by the local node
	 */
	public Range getLocalRange(Token token, ArrayList<Token> sortedTokens);

}
