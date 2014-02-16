package com.cep.messaging.impls.gossip.keyspace.filter;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IColumnContainer;
import com.cep.messaging.impls.gossip.keyspace.Memtable;
import com.cep.messaging.impls.gossip.keyspace.SuperColumn;
import com.cep.messaging.impls.gossip.keyspace.iterator.IColumnIterator;
import com.cep.messaging.impls.gossip.keyspace.iterator.SSTableNamesIterator;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.FileDataInput;

@SuppressWarnings("rawtypes")
public class NamesQueryFilter implements IFilter
{
    public final SortedSet<ByteBuffer> columns;

    public NamesQueryFilter(SortedSet<ByteBuffer> columns)
    {
        this.columns = columns;
    }

    public NamesQueryFilter(ByteBuffer column)
    {
        this(GossipUtilities.singleton(column));
    }

	public IColumnIterator getMemtableColumnIterator(ColumnFamily cf, DecoratedKey key, AbstractType comparator)
    {
        return Memtable.getNamesIterator(key, cf, this);
    }

    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable, DecoratedKey key)
    {
        return new SSTableNamesIterator(sstable, key, columns);
    }
    
    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key)
    {
        return new SSTableNamesIterator(sstable, file, key, columns);
    }

    public SuperColumn filterSuperColumn(SuperColumn superColumn, int gcBefore)
    {
        for (IColumn column : superColumn.getSubColumns())
        {
            if (!columns.contains(column.name()) || !QueryFilter.isRelevant(column, superColumn, gcBefore))
            {
                superColumn.remove(column.name());
            }
        }
        return superColumn;
    }

    public void collectReducedColumns(IColumnContainer container, Iterator<IColumn> reducedColumns, int gcBefore)
    {
        while (reducedColumns.hasNext())
        {
            IColumn column = reducedColumns.next();
            if (QueryFilter.isRelevant(column, container, gcBefore))
                container.addColumn(column);
        }
    }

    @SuppressWarnings("unchecked")
	public Comparator<IColumn> getColumnComparator(AbstractType comparator)
    {
        return comparator.columnComparator;
    }

    @Override
    public String toString()
    {
        return "NamesQueryFilter(" +
               "columns=" + StringUtils.join(columns, ",") +
               ')';
    }
}
