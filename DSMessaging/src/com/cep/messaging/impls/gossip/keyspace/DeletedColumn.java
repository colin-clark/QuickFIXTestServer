package com.cep.messaging.impls.gossip.keyspace;

import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.keyspace.marshal.MarshalException;
import com.cep.messaging.impls.gossip.serialization.ColumnSerializer;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class DeletedColumn extends Column
{    
    public DeletedColumn(ByteBuffer name, int localDeletionTime, long timestamp)
    {
        this(name, ByteBufferUtil.bytes(localDeletionTime), timestamp);
    }

    public DeletedColumn(ByteBuffer name, ByteBuffer value, long timestamp)
    {
        super(name, value, timestamp);
    }

    @Override
    public boolean isMarkedForDelete()
    {
        return true;
    }

    @Override
    public long getMarkedForDeleteAt()
    {
        return timestamp;
    }

    @Override
    public int getLocalDeletionTime()
    {
       return value.getInt(value.position());
    }

    @Override
    public IColumn reconcile(IColumn column)
    {
        if (column instanceof DeletedColumn)
            return super.reconcile(column);
        return column.reconcile(this);
    }
    
    @Override
    public IColumn localCopy(ColumnFamilyStore cfs)
    {
        return new DeletedColumn(cfs.internOrCopy(name), ByteBufferUtil.clone(value), timestamp);
    }

    @Override
    public int serializationFlags()
    {
        return ColumnSerializer.DELETION_MASK;
    }

    @Override
    public void validateFields(CFMetaData metadata) throws MarshalException
    {
        validateName(metadata);
        if (value().remaining() != 4)
            throw new MarshalException("A tombstone value should be 4 bytes long");
        if (getLocalDeletionTime() < 0)
            throw new MarshalException("The local deletion time should not be negative");
    }
}
