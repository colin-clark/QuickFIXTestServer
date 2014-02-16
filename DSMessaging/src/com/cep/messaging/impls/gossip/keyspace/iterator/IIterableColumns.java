package com.cep.messaging.impls.gossip.keyspace.iterator;

import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;

public interface IIterableColumns extends Iterable<IColumn>
{
    public int getEstimatedColumnCount();

    @SuppressWarnings("rawtypes")
	AbstractType getComparator();
}
