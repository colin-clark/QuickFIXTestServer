package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.sql.Types;

import com.cep.messaging.impls.gossip.keyspace.Column;
import com.cep.messaging.impls.gossip.keyspace.CounterContext;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public abstract class AbstractCommutativeType extends AbstractType<Long>
{
    public boolean isCommutative()
    {
        return true;
    }

    public Long compose(ByteBuffer bytes)
    {
        return CounterContext.instance().total(bytes);
    }

    public ByteBuffer decompose(Long value)
    {
        return ByteBufferUtil.bytes(value);
    }

    /**
     * create commutative column
     */
    public abstract Column createColumn(ByteBuffer name, ByteBuffer value, long timestamp);

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
