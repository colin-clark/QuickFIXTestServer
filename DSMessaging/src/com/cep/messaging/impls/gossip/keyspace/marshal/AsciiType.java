package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.google.common.base.Charsets;

public class AsciiType extends AbstractType<String>
{
    public static final AsciiType instance = new AsciiType();

    AsciiType() {} // singleton

    public String getString(ByteBuffer bytes)
    {
        try
        {
            return ByteBufferUtil.string(bytes, Charsets.US_ASCII);
        }
        catch (CharacterCodingException e)
        {
            throw new MarshalException("Invalid ascii bytes " + ByteBufferUtil.bytesToHex(bytes));
        }
    }

    public String toString(String s)
    {
        return s;
    }

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        return BytesType.bytesCompare(o1, o2);
    }

    public String compose(ByteBuffer bytes)
    {
        return getString(bytes);
    }

    public ByteBuffer decompose(String value)
    {
        return ByteBufferUtil.bytes(value, Charsets.US_ASCII);
    }

    public ByteBuffer fromString(String source)
    {
        return decompose(source);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        // 0-127
        for (int i = bytes.position(); i < bytes.limit(); i++)
        {
            byte b = bytes.get(i);
            if (b < 0 || b > 127)
                throw new MarshalException("Invalid byte for ascii: " + Byte.toString(b));
        }
    }

    public Class<String> getType()
    {
        return String.class;
    }

    public boolean isSigned()
    {
        return false;
    }

    public boolean isCaseSensitive()
    {
        return true;
    }

    public boolean isCurrency()
    {
        return false;
    }

    public int getPrecision(String obj)
    {
        return -1;
    }

    public int getScale(String obj)
    {
        return -1;
    }

    public int getJdbcType()
    {
        return Types.VARCHAR;
    }

    public boolean needsQuotes()
    {
        return true;
    }
}
