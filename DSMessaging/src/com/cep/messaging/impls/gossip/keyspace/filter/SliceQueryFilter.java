package com.cep.messaging.impls.gossip.keyspace.filter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IColumnContainer;
import com.cep.messaging.impls.gossip.keyspace.Memtable;
import com.cep.messaging.impls.gossip.keyspace.SuperColumn;
import com.cep.messaging.impls.gossip.keyspace.iterator.IColumnIterator;
import com.cep.messaging.impls.gossip.keyspace.iterator.SSTableSliceIterator;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.FileDataInput;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

@SuppressWarnings({"rawtypes","unchecked"})
public class SliceQueryFilter implements IFilter
{
    private static Logger logger = LoggerFactory.getLogger(SliceQueryFilter.class);

    public final ByteBuffer start;
    public final ByteBuffer finish;
    public final boolean reversed;
    public final int count;

    public SliceQueryFilter(ByteBuffer start, ByteBuffer finish, boolean reversed, int count)
    {
        this.start = start;
        this.finish = finish;
        this.reversed = reversed;
        this.count = count;
    }

    public IColumnIterator getMemtableColumnIterator(ColumnFamily cf, DecoratedKey key, AbstractType comparator)
    {
        return Memtable.getSliceIterator(key, cf, this, comparator);
    }

    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable, DecoratedKey key)
    {
        return new SSTableSliceIterator(sstable, key, start, finish, reversed);
    }
    
    public IColumnIterator getSSTableColumnIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key)
    {
        return new SSTableSliceIterator(sstable, file, key, start, finish, reversed);
    }

    public SuperColumn filterSuperColumn(SuperColumn superColumn, int gcBefore)
    {
        // we clone shallow, then add, under the theory that generally we're interested in a relatively small number of subcolumns.
        // this may be a poor assumption.
        SuperColumn scFiltered = superColumn.cloneMeShallow();
        Iterator<IColumn> subcolumns;
        if (reversed)
        {
            List<IColumn> columnsAsList = new ArrayList<IColumn>(superColumn.getSubColumns());
            subcolumns = Lists.reverse(columnsAsList).iterator();
        }
        else
        {
            subcolumns = superColumn.getSubColumns().iterator();
        }

        // iterate until we get to the "real" start column
        Comparator<ByteBuffer> comparator = reversed ? superColumn.getComparator().reverseComparator : superColumn.getComparator();
        while (subcolumns.hasNext())
        {
            IColumn column = subcolumns.next();
            if (comparator.compare(column.name(), start) >= 0)
            {
                subcolumns = Iterators.concat(Iterators.singletonIterator(column), subcolumns);
                break;
            }
        }
        // subcolumns is either empty now, or has been redefined in the loop above.  either is ok.
        collectReducedColumns(scFiltered, subcolumns, gcBefore);
        return scFiltered;
    }

    public Comparator<IColumn> getColumnComparator(AbstractType comparator)
    {
        return reversed ? comparator.columnReverseComparator : comparator.columnComparator;
    }

    public void collectReducedColumns(IColumnContainer container, Iterator<IColumn> reducedColumns, int gcBefore)
    {
        int liveColumns = 0;
        AbstractType comparator = container.getComparator();

        while (reducedColumns.hasNext())
        {
            if (liveColumns >= count)
                break;

            IColumn column = reducedColumns.next();
            if (logger.isDebugEnabled())
                logger.debug(String.format("collecting %s of %s: %s",
                                           liveColumns, count, column.getString(comparator)));

            if (finish.remaining() > 0
                && ((!reversed && comparator.compare(column.name(), finish) > 0))
                    || (reversed && comparator.compare(column.name(), finish) < 0))
                break;
 
            // only count live columns towards the `count` criteria
            if (column.isLive() 
                && (!container.isMarkedForDelete()
                    || column.mostRecentLiveChangeAt() > container.getMarkedForDeleteAt()))
            {
                liveColumns++;
            }

            // but we need to add all non-gc-able columns to the result for read repair:
            if (QueryFilter.isRelevant(column, container, gcBefore))
                container.addColumn(column);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "start=" + start +
               ", finish=" + finish +
               ", reversed=" + reversed +
               ", count=" + count + "]";
    }
}
