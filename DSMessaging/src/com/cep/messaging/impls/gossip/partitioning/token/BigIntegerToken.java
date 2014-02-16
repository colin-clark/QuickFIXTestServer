package com.cep.messaging.impls.gossip.partitioning.token;

import java.math.BigInteger;

public class BigIntegerToken extends Token<BigInteger> {
	static final long serialVersionUID = -5833589141319293006L;

	public BigIntegerToken(BigInteger token) {
		super(token);
	}

	// convenience method for testing
	public BigIntegerToken(String token) {
		this(new BigInteger(token));
	}

	public int compareTo(Token<BigInteger> o) {
		return token.compareTo(o.token);
	}
}
