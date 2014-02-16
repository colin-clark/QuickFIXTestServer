package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.Column;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.CounterColumn;
import com.cep.messaging.impls.gossip.keyspace.CounterUpdateColumn;
import com.cep.messaging.impls.gossip.keyspace.DeletedColumn;
import com.cep.messaging.impls.gossip.keyspace.ExpiringColumn;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class ColumnSerializer implements IColumnSerializer
{
    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ColumnSerializer.class);
    
    public final static int DELETION_MASK       = 0x01;
    public final static int EXPIRATION_MASK     = 0x02;
    public final static int COUNTER_MASK        = 0x04;
    public final static int COUNTER_UPDATE_MASK = 0x08;

    public void serialize(IColumn column, DataOutput dos)
    {
        assert column.name().remaining() > 0;
        ByteBufferUtil.writeWithShortLength(column.name(), dos);
        try
        {
            dos.writeByte(column.serializationFlags());
            if (column instanceof CounterColumn)
            {
                dos.writeLong(((CounterColumn)column).timestampOfLastDelete());
            }
            else if (column instanceof ExpiringColumn)
            {
                dos.writeInt(((ExpiringColumn) column).getTimeToLive());
                dos.writeInt(column.getLocalDeletionTime());
            }
            dos.writeLong(column.timestamp());
            ByteBufferUtil.writeWithLength(column.value(), dos);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Column deserialize(DataInput dis) throws IOException
    {
        return deserialize(dis, null, false);
    }

    /*
     * For counter columns, we must know when we deserialize them if what we
     * deserialize comes from a remote host. If it does, then we must clear
     * the delta.
     */
    public Column deserialize(DataInput dis, ColumnFamilyStore interner, boolean fromRemote) throws IOException
    {
        return deserialize(dis, interner, fromRemote, (int) (System.currentTimeMillis() / 1000));
    }

    public Column deserialize(DataInput dis, ColumnFamilyStore interner, boolean fromRemote, int expireBefore) throws IOException
    {
        ByteBuffer name = ByteBufferUtil.readWithShortLength(dis);
        if (name.remaining() <= 0)
            throw new CorruptColumnException("invalid column name length " + name.remaining());
        if (interner != null)
            name = interner.maybeIntern(name);

        int b = dis.readUnsignedByte();
        if ((b & COUNTER_MASK) != 0)
        {
            long timestampOfLastDelete = dis.readLong();
            long ts = dis.readLong();
            ByteBuffer value = ByteBufferUtil.readWithLength(dis);
            return CounterColumn.create(name, value, ts, timestampOfLastDelete, fromRemote);
        }
        else if ((b & EXPIRATION_MASK) != 0)
        {
            int ttl = dis.readInt();
            int expiration = dis.readInt();
            long ts = dis.readLong();
            ByteBuffer value = ByteBufferUtil.readWithLength(dis);
            return ExpiringColumn.create(name, value, ts, ttl, expiration, expireBefore);
        }
        else
        {
            long ts = dis.readLong();
            ByteBuffer value = ByteBufferUtil.readWithLength(dis);
            return (b & COUNTER_UPDATE_MASK) != 0
                   ? new CounterUpdateColumn(name, value, ts)
                   : ((b & DELETION_MASK) == 0
                      ? new Column(name, value, ts)
                      : new DeletedColumn(name, value, ts));
        }
    }

    private static class CorruptColumnException extends IOException
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CorruptColumnException(String s)
        {
            super(s);
        }
    }
}
