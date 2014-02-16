package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;

import org.apache.commons.lang.NotImplementedException;

import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

/** for sorting columns representing row keys in the row ordering as determined by a partitioner.
 * Not intended for user-defined CFs, and will in fact error out if used with such. */
@SuppressWarnings("rawtypes")
public class LocalByPartionerType<T extends Token> extends AbstractType<ByteBuffer>
{
    private final IPartitioner<T> partitioner;

    public LocalByPartionerType(IPartitioner<T> partitioner)
    {
        this.partitioner = partitioner;
    }

    public ByteBuffer compose(ByteBuffer bytes)
    {
        throw new UnsupportedOperationException("You can't do this with a local partitioner.");
    }

    public ByteBuffer decompose(ByteBuffer bytes)
    {
        throw new UnsupportedOperationException("You can't do this with a local partitioner.");
    }

    public String getString(ByteBuffer bytes)
    {
        return ByteBufferUtil.bytesToHex(bytes);
    }

    public String toString(ByteBuffer bb)
    {
        return getString(bb);
    }

    public ByteBuffer fromString(String source)
    {
        throw new NotImplementedException();
    }

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        return partitioner.decorateKey(o1).compareTo(partitioner.decorateKey(o2));
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        throw new IllegalStateException("You shouldn't be validating this.");
    }

    public Class<ByteBuffer> getType()
    {
        return ByteBuffer.class;
    }

    public boolean isSigned()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isCaseSensitive()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isCurrency()
    {
        throw new UnsupportedOperationException();
    }

    public int getPrecision(ByteBuffer obj)
    {
        throw new UnsupportedOperationException();
    }

    public int getScale(ByteBuffer obj)
    {
        throw new UnsupportedOperationException();
    }

    public int getJdbcType()
    {
        throw new UnsupportedOperationException();
    }

    public boolean needsQuotes()
    {
        throw new UnsupportedOperationException();
    }
}
