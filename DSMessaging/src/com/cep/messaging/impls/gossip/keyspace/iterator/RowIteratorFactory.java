package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.Memtable;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryFilter;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableScanner;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.MergeIterator;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;

@SuppressWarnings({"rawtypes","unused"})
public class RowIteratorFactory
{

    private static final int RANGE_FILE_BUFFER_SIZE = 256 * 1024;

    private static final Comparator<IColumnIterator> COMPARE_BY_KEY = new Comparator<IColumnIterator>()
    {
        public int compare(IColumnIterator o1, IColumnIterator o2)
        {
            return DecoratedKey.comparator.compare(o1.getKey(), o2.getKey());
        }
    };

    
    /**
     * Get a row iterator over the provided memtables and sstables, between the provided keys
     * and filtered by the queryfilter.
     * @param memtables Memtables pending flush.
     * @param sstables SStables to scan through.
     * @param startWith Start at this key
     * @param stopAt Stop and this key
     * @param filter Used to decide which columns to pull out
     * @param comparator
     * @return A row iterator following all the given restrictions
     */
	public static CloseableIterator<Row> getIterator(final Collection<Memtable> memtables,
                                          final Collection<SSTableReader> sstables,
                                          final DecoratedKey startWith,
                                          final DecoratedKey stopAt,
                                          final QueryFilter filter,
                                          final AbstractType comparator,
                                          final ColumnFamilyStore cfs
    )
    {
        // fetch data from current memtable, historical memtables, and SSTables in the correct order.
        final List<CloseableIterator<IColumnIterator>> iterators = new ArrayList<CloseableIterator<IColumnIterator>>();
        // we iterate through memtables with a priority queue to avoid more sorting than necessary.
        // this predicate throws out the rows before the start of our range.
        Predicate<IColumnIterator> p = new Predicate<IColumnIterator>()
        {
            public boolean apply(IColumnIterator row)
            {
                return startWith.compareTo(row.getKey()) <= 0
                       && (stopAt.isEmpty() || row.getKey().compareTo(stopAt) <= 0);
            }
        };

        // memtables
        for (Memtable memtable : memtables)
        {
            iterators.add(new ConvertToColumnIterator(filter, comparator, p, memtable.getEntryIterator(startWith)));
        }

        // sstables
        for (SSTableReader sstable : sstables)
        {
            final SSTableScanner scanner = sstable.getScanner(RANGE_FILE_BUFFER_SIZE, filter);
            scanner.seekTo(startWith);
            assert scanner instanceof Closeable; // otherwise we leak FDs
            iterators.add(scanner);
        }

        final Memtable firstMemtable = memtables.iterator().next();
        // reduce rows from all sources into a single row
        return MergeIterator.get(iterators, COMPARE_BY_KEY, new MergeIterator.Reducer<IColumnIterator, Row>()
        {
            private final int gcBefore = (int) (System.currentTimeMillis() / 1000) - cfs.metadata.getGcGraceSeconds();
            private final List<IColumnIterator> colIters = new ArrayList<IColumnIterator>();
            private DecoratedKey key;
            private ColumnFamily returnCF;

            @Override
            protected void onKeyChange()
            {
                this.returnCF = ColumnFamily.create(cfs.metadata);
            }

            public void reduce(IColumnIterator current)
            {
                this.colIters.add(current);
                this.key = current.getKey();
                this.returnCF.delete(current.getColumnFamily());
            }

            protected Row getReduced()
            {

                // First check if this row is in the rowCache. If it is we can skip the rest
                ColumnFamily cached = cfs.getRawCachedRow(key);
                if (cached == null)
                    // not cached: collate
                    filter.collateColumns(returnCF, colIters, comparator, gcBefore);
                else
                {
                    QueryFilter keyFilter = new QueryFilter(key, filter.path, filter.filter);
                    returnCF = cfs.filterColumnFamily(cached, keyFilter, gcBefore);
                }

                Row rv = new Row(key, returnCF);
                colIters.clear();
                key = null;
                return rv;
            }
        });
    }

    /**
     * Get a ColumnIterator for a specific key in the memtable.
     */
    private static class ConvertToColumnIterator extends AbstractIterator<IColumnIterator> implements CloseableIterator<IColumnIterator>
    {
        private final QueryFilter filter;
        private final AbstractType comparator;
        private final Predicate<IColumnIterator> pred;
        private final Iterator<Map.Entry<DecoratedKey, ColumnFamily>> iter;

        public ConvertToColumnIterator(QueryFilter filter, AbstractType comparator, Predicate<IColumnIterator> pred, Iterator<Map.Entry<DecoratedKey, ColumnFamily>> iter)
        {
            this.filter = filter;
            this.comparator = comparator;
            this.pred = pred;
            this.iter = iter;
        }

        public IColumnIterator computeNext()
        {
            while (iter.hasNext())
            {
                Map.Entry<DecoratedKey, ColumnFamily> entry = iter.next();
                IColumnIterator ici = filter.getMemtableColumnIterator(entry.getValue(), entry.getKey(), comparator);
                if (pred.apply(ici))
                    return ici;
            }
            return endOfData();
        }

        public void close()
        {
            // pass
        }
    }
}
