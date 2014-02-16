package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.UUIDGen;

/**
 * Compares UUIDs using the following criteria:<br>
 * - if count of supplied bytes is less than 16, compare counts<br>
 * - compare UUID version fields<br>
 * - nil UUID is always lesser<br>
 * - compare timestamps if both are time-based<br>
 * - compare lexically, unsigned msb-to-lsb comparison<br>
 * 
 * @see "com.fasterxml.uuid.UUIDComparator"
 */
public class UUIDType extends AbstractUUIDType
{
    public static final UUIDType instance = new UUIDType();

    UUIDType()
    {
    }

    public int compare(ByteBuffer b1, ByteBuffer b2)
    {

        // Compare for length

        if ((b1 == null) || (b1.remaining() < 16))
        {
            return ((b2 == null) || (b2.remaining() < 16)) ? 0 : -1;
        }
        if ((b2 == null) || (b2.remaining() < 16))
        {
            return 1;
        }

        int s1 = b1.position();
        int s2 = b2.position();

        // Compare versions

        int v1 = (b1.get(s1 + 6) >> 4) & 0x0f;
        int v2 = (b2.get(s2 + 6) >> 4) & 0x0f;

        if (v1 != v2)
        {
            return v1 - v2;
        }

        // Compare timestamps for version 1

        if (v1 == 1)
        {
            // if both time-based, compare as timestamps
            int c = compareTimestampBytes(b1, b2);
            if (c != 0)
            {
                return c;
            }
        }

        // Compare the two byte arrays starting from the first
        // byte in the sequence until an inequality is
        // found. This should provide equivalent results
        // to the comparison performed by the RFC 4122
        // Appendix A - Sample Implementation.
        // Note: java.util.UUID.compareTo is not a lexical
        // comparison
        for (int i = 0; i < 16; i++)
        {
            int c = ((b1.get(s1 + i)) & 0xFF) - ((b2.get(s2 + i)) & 0xFF);
            if (c != 0)
            {
                return c;
            }
        }

        return 0;
    }

    private static int compareTimestampBytes(ByteBuffer o1, ByteBuffer o2)
    {
        int o1Pos = o1.position();
        int o2Pos = o2.position();

        int d = (o1.get(o1Pos + 6) & 0xF) - (o2.get(o2Pos + 6) & 0xF);
        if (d != 0)
        {
            return d;
        }

        d = (o1.get(o1Pos + 7) & 0xFF) - (o2.get(o2Pos + 7) & 0xFF);
        if (d != 0)
        {
            return d;
        }

        d = (o1.get(o1Pos + 4) & 0xFF) - (o2.get(o2Pos + 4) & 0xFF);
        if (d != 0)
        {
            return d;
        }

        d = (o1.get(o1Pos + 5) & 0xFF) - (o2.get(o2Pos + 5) & 0xFF);
        if (d != 0)
        {
            return d;
        }

        d = (o1.get(o1Pos) & 0xFF) - (o2.get(o2Pos) & 0xFF);
        if (d != 0)
        {
            return d;
        }

        d = (o1.get(o1Pos + 1) & 0xFF) - (o2.get(o2Pos + 1) & 0xFF);
        if (d != 0)
        {
            return d;
        }

        d = (o1.get(o1Pos + 2) & 0xFF) - (o2.get(o2Pos + 2) & 0xFF);
        if (d != 0)
        {
            return d;
        }

        return (o1.get(o1Pos + 3) & 0xFF) - (o2.get(o2Pos + 3) & 0xFF);
    }

    public UUID compose(ByteBuffer bytes)
    {

        bytes = bytes.slice();
        if (bytes.remaining() < 16)
            return new UUID(0, 0);
        return new UUID(bytes.getLong(), bytes.getLong());
    }

    public String toString(UUID uuid)
    {
        return uuid.toString();
    }

    public Class<UUID> getType()
    {
        return UUID.class;
    }

    public void validate(ByteBuffer bytes)
    {
        if ((bytes.remaining() != 0) && (bytes.remaining() != 16))
        {
            throw new MarshalException("UUIDs must be exactly 16 bytes");
        }
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
        UUID uuid = compose(bytes);
        return uuid.toString();
    }

    public ByteBuffer decompose(UUID value)
    {
        return ByteBuffer.wrap(UUIDGen.decompose(value));
    }

    @Override
    public ByteBuffer fromString(String source) throws MarshalException
    {
        // Return an empty ByteBuffer for an empty string.
        if (source.isEmpty())
            return ByteBufferUtil.EMPTY_BYTE_BUFFER;

        ByteBuffer idBytes = null;

        // ffffffff-ffff-ffff-ffff-ffffffffff
        if (TimeUUIDType.regexPattern.matcher(source).matches())
        {
            UUID uuid;
            try
            {
                uuid = UUID.fromString(source);
                idBytes = ByteBuffer.wrap(UUIDGen.decompose(uuid));
            }
            catch (IllegalArgumentException e)
            {
                throw new MarshalException(String.format("unable to make UUID from '%s'", source), e);
            }
        }
        else if (source.toLowerCase().equals("now"))
        {
            idBytes = ByteBuffer.wrap(UUIDGen.decompose(UUIDGen.makeType1UUIDFromHost(GossipUtilities.getLocalAddress())));
        }
        // Milliseconds since epoch?
        else if (source.matches("^\\d+$"))
        {
            try
            {
                idBytes = ByteBuffer.wrap(UUIDGen.getTimeUUIDBytes(Long.parseLong(source)));
            }
            catch (NumberFormatException e)
            {
                throw new MarshalException(String.format("unable to make version 1 UUID from '%s'", source), e);
            }
        }
        // Last chance, attempt to parse as date-time string
        else
        {
            try
            {
                long timestamp = DateUtils.parseDate(source, TimeUUIDType.iso8601Patterns).getTime();
                idBytes = ByteBuffer.wrap(UUIDGen.getTimeUUIDBytes(timestamp));
            }
            catch (ParseException e1)
            {
                throw new MarshalException(String.format("unable to coerce '%s' to version 1 UUID", source), e1);
            }
        }

        return idBytes;
    }
}
