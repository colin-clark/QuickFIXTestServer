package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;

public interface IColumnIterator extends CloseableIterator<IColumn>
{
    /**
     * @return An empty CF holding metadata for the row being iterated.
     */
    public abstract ColumnFamily getColumnFamily();

    /**
     * @return the current row key
     */
    @SuppressWarnings("rawtypes")
	public DecoratedKey getKey();

    /** clean up any open resources */
    public void close() throws IOException;
}

