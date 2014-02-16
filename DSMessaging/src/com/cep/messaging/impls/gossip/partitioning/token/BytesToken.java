package com.cep.messaging.impls.gossip.partitioning.token;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

public class BytesToken extends Token<byte[]>
{
    static final long serialVersionUID = -2630749093733680626L;

    public BytesToken(ByteBuffer token)
    {
        this(ByteBufferUtil.getArray(token));
    }

    public BytesToken(byte[] token)
    {
        super(token);
    }

    @Override
    public String toString()
    {
        return "Token(bytes[" + GossipUtilities.bytesToHex(token) + "])";
    }

    public int compareTo(Token<byte[]> o)
    {   
        return GossipUtilities.compareUnsigned(token, o.token, 0, 0, token.length, o.token.length);
    }
    

    @Override
    public int hashCode()
    {
        final int prime = 31;
        return prime + Arrays.hashCode(token);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof BytesToken))
            return false;
        BytesToken other = (BytesToken) obj;
           
        return Arrays.equals(token, other.token);
    }
}
