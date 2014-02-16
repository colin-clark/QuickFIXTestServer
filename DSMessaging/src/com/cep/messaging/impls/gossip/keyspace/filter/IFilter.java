package com.cep.messaging.impls.gossip.keyspace.filter;

import java.util.Comparator;
import java.util.Iterator;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IColumnContainer;
import com.cep.messaging.impls.gossip.keyspace.SuperColumn;
import com.cep.messaging.impls.gossip.keyspace.iterator.IColumnIterator;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.FileDataInput;

/**
 * Given an implementation-specific description of what columns to look for, provides methods
 * to extract the desired columns from a Memtable, SSTable, or SuperColumn.  Either the get*ColumnIterator
 * methods will be called, or filterSuperColumn, but not both on the same object.  QueryFilter
 * takes care of putting the two together if subcolumn filtering needs to be done, based on the
 * querypath that it knows (but that IFilter implementations are oblivious to).
 */
@SuppressWarnings("rawtypes")
public interface IFilter
{
    /**
     * returns an iterator that returns columns from the given memtable
     * matching the Filter criteria in sorted order.
     */
	public abstract IColumnIterator getMemtableColumnIterator(ColumnFamily cf, DecoratedKey key, AbstractType comparator);

    /**
     * Get an iterator that returns columns from the given SSTable using the opened file
     * matching the Filter criteria in sorted order.
     * @param sstable
     * @param file Already opened file data input, saves us opening another one
     * @param key The key of the row we are about to iterate over
     */
    public abstract IColumnIterator getSSTableColumnIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key);

    /**
     * returns an iterator that returns columns from the given SSTable
     * matching the Filter criteria in sorted order.
     */
    public abstract IColumnIterator getSSTableColumnIterator(SSTableReader sstable, DecoratedKey key);

    /**
     * collects columns from reducedColumns into returnCF.  Termination is determined
     * by the filter code, which should have some limit on the number of columns
     * to avoid running out of memory on large rows.
     */
    public abstract void collectReducedColumns(IColumnContainer container, Iterator<IColumn> reducedColumns, int gcBefore);

    /**
     * subcolumns of a supercolumn are unindexed, so to pick out parts of those we operate in-memory.
     * @param superColumn may be modified by filtering op.
     */
    public abstract SuperColumn filterSuperColumn(SuperColumn superColumn, int gcBefore);

    public Comparator<IColumn> getColumnComparator(AbstractType comparator);
}
