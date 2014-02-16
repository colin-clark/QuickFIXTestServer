package com.cep.messaging.impls.gossip.partitioning;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.impls.gossip.partitioning.token.BytesToken;
import com.cep.messaging.impls.gossip.partitioning.token.Token;

public class ByteOrderedPartitioner extends AbstractByteOrderedPartitioner
{
    public BytesToken getToken(ByteBuffer key)
    {
        if (key.remaining() == 0)
            return MINIMUM;
        return new BytesToken(key);
    }

	@SuppressWarnings("rawtypes")
	@Override
	public Range getLocalRange(Token token, ArrayList<Token> sortedTokens) {
		return null;
	}
}
