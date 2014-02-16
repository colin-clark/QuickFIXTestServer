package com.cep.messaging.impls.gossip.service;

import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;

public class DigestMismatchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	public DigestMismatchException(DecoratedKey key, ByteBuffer digest,
			ByteBuffer digest2) {
	}

}
