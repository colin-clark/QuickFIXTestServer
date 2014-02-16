package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.keyspace.Column;
import com.cep.messaging.impls.gossip.keyspace.CounterUpdateColumn;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class CounterColumnType extends AbstractCommutativeType
{
    public static final CounterColumnType instance = new CounterColumnType();

    CounterColumnType() {} // singleton

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        if (o1 == null)
            return null == o2 ?  0 : -1;

        return ByteBufferUtil.compareUnsigned(o1, o2);
    }

    public String getString(ByteBuffer bytes)
    {
        return ByteBufferUtil.bytesToHex(bytes);
    }

    public String toString(Long l)
    {
        return l.toString();
    }

    /**
     * create commutative column
     */
    public Column createColumn(ByteBuffer name, ByteBuffer value, long timestamp)
    {
        return new CounterUpdateColumn(name, value, timestamp);
    }

    public ByteBuffer fromString(String source)
    {
        return ByteBufferUtil.hexToBytes(source);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 8 && bytes.remaining() != 0)
            throw new MarshalException(String.format("Expected 8 or 0 byte long (%d)", bytes.remaining()));
    }
}
