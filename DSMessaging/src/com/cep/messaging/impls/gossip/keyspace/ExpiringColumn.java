package com.cep.messaging.impls.gossip.keyspace;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.marshal.MarshalException;
import com.cep.messaging.impls.gossip.serialization.ColumnSerializer;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.util.DataOutputBuffer;

/**
 * Alternative to Column that have an expiring time.
 * ExpiringColumn is immutable (as Column is).
 *
 * Note that ExpiringColumn does not override Column.getMarkedForDeleteAt,
 * which means that it's in the somewhat unintuitive position of being deleted (after its expiration)
 * without having a time-at-which-it-became-deleted.  (Because ttl is a server-side measurement,
 * we can't mix it with the timestamp field, which is client-supplied and whose resolution we
 * can't assume anything about.)
 */
public class ExpiringColumn extends Column
{
    private final int localExpirationTime;
    private final int timeToLive;

    public ExpiringColumn(ByteBuffer name, ByteBuffer value, long timestamp, int timeToLive)
    {
      this(name, value, timestamp, timeToLive, (int) (System.currentTimeMillis() / 1000) + timeToLive);
    }

    public ExpiringColumn(ByteBuffer name, ByteBuffer value, long timestamp, int timeToLive, int localExpirationTime)
    {
        super(name, value, timestamp);
        assert timeToLive > 0 : timeToLive;
        assert localExpirationTime > 0 : localExpirationTime;
        this.timeToLive = timeToLive;
        this.localExpirationTime = localExpirationTime;
    }

    /** @return Either a DeletedColumn, or an ExpiringColumn. */
    public static Column create(ByteBuffer name, ByteBuffer value, long timestamp, int timeToLive, int localExpirationTime, int expireBefore)
    {
        if (localExpirationTime >= expireBefore)
            return new ExpiringColumn(name, value, timestamp, timeToLive, localExpirationTime);
        // the column is now expired, we can safely return a simple tombstone
        return new DeletedColumn(name, localExpirationTime, timestamp);
    }

    public int getTimeToLive()
    {
        return timeToLive;
    }

    @Override
    public boolean isMarkedForDelete()
    {
        return (int) (System.currentTimeMillis() / 1000 ) > localExpirationTime;
    }

    @Override
    public int size()
    {
        /*
         * An expired column adds to a Column : 
         *    4 bytes for the localExpirationTime
         *  + 4 bytes for the timeToLive
        */
        return super.size() + DBConstants.intSize_ + DBConstants.intSize_;
    }

    @Override
    public void updateDigest(MessageDigest digest)
    {
        digest.update(name.duplicate());
        digest.update(value.duplicate());

        DataOutputBuffer buffer = new DataOutputBuffer();
        try
        {
            buffer.writeLong(timestamp);
            buffer.writeByte(serializationFlags());
            buffer.writeInt(timeToLive);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        digest.update(buffer.getData(), 0, buffer.getLength());
    }

    @Override
    public int getLocalDeletionTime()
    {
        return localExpirationTime;
    }

    @Override
    public IColumn localCopy(ColumnFamilyStore cfs)
    {
        return new ExpiringColumn(cfs.internOrCopy(name), ByteBufferUtil.clone(value), timestamp, timeToLive, localExpirationTime);
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public String getString(AbstractType comparator)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getString(comparator));
        sb.append("!");
        sb.append(timeToLive);
        return sb.toString();
    }

    @Override
    public long getMarkedForDeleteAt()
    {
        if (isMarkedForDelete())
        {
            return timestamp;
        }
        else
        {
            throw new IllegalStateException("column is not marked for delete");
        }
    }

    @Override
    public int serializationFlags()
    {
        return ColumnSerializer.EXPIRATION_MASK;
    }

    @Override
    public void validateFields(CFMetaData metadata) throws MarshalException
    {
        super.validateFields(metadata);
        if (timeToLive <= 0)
            throw new MarshalException("A column TTL should be > 0");
        if (localExpirationTime < 0)
            throw new MarshalException("The local expiration time should not be negative");
    }
}
