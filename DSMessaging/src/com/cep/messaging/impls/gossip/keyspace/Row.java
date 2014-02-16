package com.cep.messaging.impls.gossip.keyspace;

import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.serialization.RowSerializer;

public class Row
{
    private static RowSerializer serializer = new RowSerializer();

    public static RowSerializer serializer()
    {
        return serializer;
    }

	public final DecoratedKey<?> key;
    public final ColumnFamily cf;

    public Row(DecoratedKey<?> decoratedKey, ColumnFamily cf)
    {
        assert decoratedKey != null;
        // cf may be null, indicating no data
        this.key = decoratedKey;
        this.cf = cf;
    }

    @Override
    public String toString()
    {
        return "Row(" +
               "key=" + key +
               ", cf=" + cf +
               ')';
    }
}

