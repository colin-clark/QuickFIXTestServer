package com.cep.messaging.impls.gossip.partitioning.token;

public class StringToken extends Token<String> {
	static final long serialVersionUID = 5464084395277974963L;
	public static final StringToken MINIMUM = new StringToken("A");
    public static final StringToken MAXIMUM = new StringToken("ZZZZ");

	public StringToken(String token) {
		super(token);
	}

	public int compareTo(Token<String> o) {
		return token.compareTo(o.token);
	}
}
