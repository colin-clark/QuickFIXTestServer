package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

public class BytesType extends AbstractType<ByteBuffer>
{
    public static final BytesType instance = new BytesType();

    BytesType() {} // singleton

    public ByteBuffer compose(ByteBuffer bytes)
    {
        return bytes.duplicate();
    }

    public ByteBuffer decompose(ByteBuffer value)
    {
        return value;
    }
    
    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        return BytesType.bytesCompare(o1, o2);
    }

    public static int bytesCompare(ByteBuffer o1, ByteBuffer o2)
    {
        if(null == o1){
            if(null == o2) return 0;
            else return -1;
        }
              
        return ByteBufferUtil.compareUnsigned(o1, o2);
    }

    public String getString(ByteBuffer bytes)
    {
        return ByteBufferUtil.bytesToHex(bytes);
    }

    public String toString(ByteBuffer byteBuffer)
    {
        return getString(byteBuffer);
    }

    public ByteBuffer fromString(String source)
    {
        try
        {
            return ByteBuffer.wrap(GossipUtilities.hexToBytes(source));
        }
        catch (NumberFormatException e)
        {
            throw new MarshalException(String.format("cannot parse '%s' as hex bytes", source), e);
        }
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        // all bytes are legal.
    }

    public Class<ByteBuffer> getType()
    {
        return ByteBuffer.class;
    }

    public boolean isSigned()
    {
        return false;
    }

    public boolean isCaseSensitive()
    {
        return false;
    }

    public boolean isCurrency()
    {
        return false;
    }

    public int getPrecision(ByteBuffer obj)
    {
        return -1;
    }

    public int getScale(ByteBuffer obj)
    {
        return -1;
    }

    public int getJdbcType()
    {
        return Types.BINARY;
    }

    public boolean needsQuotes()
    {
        return true;
    }
}
