package com.cep.messaging.impls.gossip.keyspace;

import java.nio.ByteBuffer;
import java.util.Collection;

import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;

public interface IColumnContainer
{
    public void addColumn(IColumn column);
    public void remove(ByteBuffer columnName);

    public boolean isMarkedForDelete();
    public long getMarkedForDeleteAt();

    @SuppressWarnings("rawtypes")
	public AbstractType getComparator();

    public Collection<IColumn> getSortedColumns();
}
