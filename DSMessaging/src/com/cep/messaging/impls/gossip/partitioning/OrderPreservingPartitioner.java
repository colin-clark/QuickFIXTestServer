package com.cep.messaging.impls.gossip.partitioning;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Random;

import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.partitioning.token.StringToken;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.Pair;

public class OrderPreservingPartitioner implements IPartitioner<StringToken> {
	public static final StringToken MINIMUM = new StringToken("");

	public static final BigInteger CHAR_MASK = new BigInteger("65535");

	public DecoratedKey<StringToken> decorateKey(ByteBuffer key) {
		return new DecoratedKey<StringToken>(getToken(key), key);
	}

	public DecoratedKey<StringToken> convertFromDiskFormat(ByteBuffer key) {
		return new DecoratedKey<StringToken>(getToken(key), key);
	}

	@SuppressWarnings("rawtypes")
	public StringToken midpoint(Token ltoken, Token rtoken) {
		int sigchars = Math.max(((StringToken) ltoken).token.length(), ((StringToken) rtoken).token.length());
		BigInteger left = bigForString(((StringToken) ltoken).token, sigchars);
		BigInteger right = bigForString(((StringToken) rtoken).token, sigchars);

		Pair<BigInteger, Boolean> midpair = GossipUtilities.midpoint(left, right, 16 * sigchars);
		return new StringToken(stringForBig(midpair.left, sigchars, midpair.right));
	}

	/**
	 * Copies the characters of the given string into a BigInteger.
	 * 
	 * TODO: Does not acknowledge any codepoints above 0xFFFF... problem?
	 */
	private static BigInteger bigForString(String str, int sigchars) {
		assert str.length() <= sigchars;

		BigInteger big = BigInteger.ZERO;
		for (int i = 0; i < str.length(); i++) {
			int charpos = 16 * (sigchars - (i + 1));
			BigInteger charbig = BigInteger.valueOf(str.charAt(i) & 0xFFFF);
			big = big.or(charbig.shiftLeft(charpos));
		}
		return big;
	}

	/**
	 * Convert a (positive) BigInteger into a String. If remainder is true, an
	 * additional char with the high order bit enabled will be added to the end
	 * of the String.
	 */
	private String stringForBig(BigInteger big, int sigchars, boolean remainder) {
		char[] chars = new char[sigchars + (remainder ? 1 : 0)];
		if (remainder) {
			// remaining bit is the most significant in the last char
			chars[sigchars] |= 0x8000;
		}
		for (int i = 0; i < sigchars; i++) {
			int maskpos = 16 * (sigchars - (i + 1));
			// apply bitmask and get char value
			chars[i] = (char) (big.and(CHAR_MASK.shiftLeft(maskpos)).shiftRight(maskpos).intValue() & 0xFFFF);
		}
		return new String(chars);
	}

	public StringToken getMinimumToken() {
		return MINIMUM;
	}

	public StringToken getRandomToken() {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random r = new Random();
		StringBuilder buffer = new StringBuilder();
		for (int j = 0; j < 16; j++) {
			buffer.append(chars.charAt(r.nextInt(chars.length())));
		}
		return new StringToken(buffer.toString());
	}

	private final Token.TokenFactory<String> tokenFactory = new Token.TokenFactory<String>() {
		public ByteBuffer toByteArray(Token<String> stringToken) {
			return ByteBufferUtil.bytes(stringToken.token);
		}

		public Token<String> fromByteArray(ByteBuffer bytes) {
			try {
				return new StringToken(ByteBufferUtil.string(bytes));
			} catch (CharacterCodingException e) {
				throw new RuntimeException(e);
			}
		}

		public String toString(Token<String> stringToken) {
			return stringToken.token;
		}

		public Token<String> fromString(String string) {
			if (string.equalsIgnoreCase("Z")) {
				return StringToken.MAXIMUM;
			} else {
				return new StringToken(string);
			}
		}
	};

	public Token.TokenFactory<String> getTokenFactory() {
		return tokenFactory;
	}

	public boolean preservesOrder() {
		return true;
	}

	public StringToken getToken(ByteBuffer key) {
		String skey;
		try {
			skey = ByteBufferUtil.string(key);
		} catch (CharacterCodingException e) {
			throw new RuntimeException("The provided key was not UTF8 encoded.", e);
		}
		return new StringToken(skey);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Range getLocalRange(Token rhs, ArrayList<Token> tokens) {
		boolean coverRight = (tokens.indexOf(StringToken.MAXIMUM) < 0) ? true : false;
		int index = tokens.indexOf(rhs);
		if (index < 0) {
			return null;
		}
		if (index == tokens.size()-1 && coverRight) {
			rhs = StringToken.MAXIMUM;
		}
		if (index == 0) {
			return new Range(StringToken.MINIMUM, rhs);
		} else {
			return new Range(tokens.get(index - 1), rhs);
		}
	}

}
