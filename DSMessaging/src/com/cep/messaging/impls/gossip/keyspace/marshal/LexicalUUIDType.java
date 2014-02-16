package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.UUIDGen;

public class LexicalUUIDType extends AbstractUUIDType
{
    public static final LexicalUUIDType instance = new LexicalUUIDType();

    LexicalUUIDType() {} // singleton

    public UUID compose(ByteBuffer bytes)
    {
        return UUIDGen.getUUID(bytes);
    }

    public ByteBuffer decompose(UUID value)
    {
        return ByteBuffer.wrap(UUIDGen.decompose(value));
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

        return UUIDGen.getUUID(o1).compareTo(UUIDGen.getUUID(o2));
    }

    public String getString(ByteBuffer bytes)
    {
        if (bytes.remaining() == 0)
        {
            return "";
        }
        if (bytes.remaining() != 16)
        {
            throw new MarshalException("UUIDs must be exactly 16 bytes");
        }
        return UUIDGen.getUUID(bytes).toString();
    }

    public String toString(UUID uuid)
    {
        return uuid.toString();
    }

    public ByteBuffer fromString(String source) throws MarshalException
    {
        // Return an empty ByteBuffer for an empty string.
        if (source.isEmpty())
            return ByteBufferUtil.EMPTY_BYTE_BUFFER;

        try
        {
            return decompose(UUID.fromString(source));
        }
        catch (IllegalArgumentException e)
        {
            throw new MarshalException(String.format("unable to make UUID from '%s'", source), e);
        }
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 16 && bytes.remaining() != 0)
            throw new MarshalException(String.format("LexicalUUID should be 16 or 0 bytes (%d)", bytes.remaining()));
        // not sure what the version should be for this.
    }

    public Class<UUID> getType()
    {
        return UUID.class;
    }
}
