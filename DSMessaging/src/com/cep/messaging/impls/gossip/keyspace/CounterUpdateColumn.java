package com.cep.messaging.impls.gossip.keyspace;

import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.serialization.ColumnSerializer;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

/**
 * A counter update while it hasn't been applied yet by the leader replica.
 *
 * Contains a single counter update. When applied by the leader replica, this
 * is transformed to a relevant CounterColumn. This Column is a temporary data
 * structure that should never be stored inside a memtable or an sstable.
 */
public class CounterUpdateColumn extends Column
{
    public CounterUpdateColumn(ByteBuffer name, long value, long timestamp)
    {
        this(name, ByteBufferUtil.bytes(value), timestamp);
    }

    public CounterUpdateColumn(ByteBuffer name, ByteBuffer value, long timestamp)
    {
        super(name, value, timestamp);
    }

    public long delta()
    {
        return value().getLong(value().position());
    }

    @Override
    public IColumn diff(IColumn column)
    {
        // Diff is used during reads, but we should never read those columns
        throw new UnsupportedOperationException("This operation is unsupported on CounterUpdateColumn.");
    }

    @Override
    public IColumn reconcile(IColumn column)
    {
        // The only time this could happen is if a batchAdd ships two
        // increment for the same column. Hence we simply sums the delta.

        assert (column instanceof CounterUpdateColumn) || (column instanceof DeletedColumn) : "Wrong class type.";

        // tombstones take precedence
        if (column.isMarkedForDelete())
            return timestamp() > column.timestamp() ? this : column;

        // neither is tombstoned
        CounterUpdateColumn c = (CounterUpdateColumn)column;
        return new CounterUpdateColumn(name(), delta() + c.delta(), Math.max(timestamp(), c.timestamp()));
    }

    @Override
    public int serializationFlags()
    {
        return ColumnSerializer.COUNTER_UPDATE_MASK;
    }

    @Override
    public CounterColumn localCopy(ColumnFamilyStore cfs)
    {
        return new CounterColumn(cfs.internOrCopy(name),
                                 CounterContext.instance().create(delta()),
                                 timestamp(),
                                 Long.MIN_VALUE);
    }
}
