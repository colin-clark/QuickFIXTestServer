package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class LongType extends AbstractType<Long>
{
    public static final LongType instance = new LongType();

    LongType() {} // singleton

    public Long compose(ByteBuffer bytes)
    {
        return ByteBufferUtil.toLong(bytes);
    }

    public ByteBuffer decompose(Long value)
    {
        return ByteBufferUtil.bytes(value);
    }

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        if (o1.remaining() == 0)
        {
            return o2.remaining() == 0 ? 0 : -1;
        }
        if (o2.remaining() == 0)
        {
            return 1;
        }

        int diff = o1.get(o1.position()) - o2.get(o2.position());
        if (diff != 0)
            return diff;
        
       
        return ByteBufferUtil.compareUnsigned(o1, o2);
    }

    public String getString(ByteBuffer bytes)
    {
        if (bytes.remaining() == 0)
        {
            return "";
        }
        if (bytes.remaining() != 8)
        {
            throw new MarshalException("A long is exactly 8 bytes: "+bytes.remaining());
        }
        
        return String.valueOf(bytes.getLong(bytes.position()));
    }

    public String toString(Long l)
    {
        return l.toString();
    }

    public ByteBuffer fromString(String source) throws MarshalException
    {
        // Return an empty ByteBuffer for an empty string.
        if (source.isEmpty())
            return ByteBufferUtil.EMPTY_BYTE_BUFFER;

        long longType;

        try
        {
            longType = Long.parseLong(source);
        }
        catch (Exception e)
        {
            throw new MarshalException(String.format("unable to make long from '%s'", source), e);
        }

        return decompose(longType);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 8 && bytes.remaining() != 0)
            throw new MarshalException(String.format("Expected 8 or 0 byte long (%d)", bytes.remaining()));
    }

    public Class<Long> getType()
    {
        return Long.class;
    }

    public boolean isSigned()
    {
        return true;
    }

    public boolean isCaseSensitive()
    {
        return false;
    }

    public boolean isCurrency()
    {
        return false;
    }

    public int getPrecision(Long obj)
    {
        return obj.toString().length();
    }

    public int getScale(Long obj)
    {
        return 0;
    }

    public int getJdbcType()
    {
        return Types.INTEGER;
    }

    public boolean needsQuotes()
    {
        return false;
    }
}
