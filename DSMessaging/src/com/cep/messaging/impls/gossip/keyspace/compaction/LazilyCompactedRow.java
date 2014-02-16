package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.io.DataOutput;
import java.io.IOError;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.ColumnIndexer;
import com.cep.messaging.impls.gossip.keyspace.CounterColumn;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.iterator.IIterableColumns;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableIdentityIterator;
import com.cep.messaging.util.DataOutputBuffer;
import com.cep.messaging.util.MergeIterator;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

/**
 * LazilyCompactedRow only computes the row bloom filter and column index in memory
 * (at construction time); it does this by reading one column at a time from each
 * of the rows being compacted, and merging them as it does so.  So the most we have
 * in memory at a time is the bloom filter, the index, and one column from each
 * pre-compaction row.
 *
 * When write() or update() is called, a second pass is made over the pre-compaction
 * rows to write the merged columns or update the hash, again with at most one column
 * from each row deserialized at a time.
 */
public class LazilyCompactedRow extends AbstractCompactedRow implements IIterableColumns
{
    private final List<SSTableIdentityIterator> rows;
    private final CompactionController controller;
    private final boolean shouldPurge;
    private final DataOutputBuffer headerBuffer;
    private ColumnFamily emptyColumnFamily;
    private Reducer reducer;
    private int columnCount;
    private long columnSerializedSize;

    public LazilyCompactedRow(CompactionController controller, List<SSTableIdentityIterator> rows)
    {
        super(rows.get(0).getKey());
        this.controller = controller;
        this.shouldPurge = controller.shouldPurge(key);
        this.rows = new ArrayList<SSTableIdentityIterator>(rows);

        for (SSTableIdentityIterator row : rows)
        {
            ColumnFamily cf = row.getColumnFamily();

            if (emptyColumnFamily == null)
                emptyColumnFamily = cf;
            else
                emptyColumnFamily.delete(cf);
        }

        // initialize row header so isEmpty can be called
        headerBuffer = new DataOutputBuffer();
        ColumnIndexer.serialize(this, headerBuffer);
        // reach into the reducer used during iteration to get column count and size
        columnCount = reducer.size;
        columnSerializedSize = reducer.serializedSize;
        reducer = null;
    }

    public void write(DataOutput out) throws IOException
    {
        DataOutputBuffer clockOut = new DataOutputBuffer();
        ColumnFamily.serializer().serializeCFInfo(emptyColumnFamily, clockOut);

        long dataSize = headerBuffer.getLength() + clockOut.getLength() + columnSerializedSize;
        assert dataSize > 0;
        out.writeLong(dataSize);
        out.write(headerBuffer.getData(), 0, headerBuffer.getLength());
        out.write(clockOut.getData(), 0, clockOut.getLength());
        out.writeInt(columnCount);

        Iterator<IColumn> iter = iterator();
        while (iter.hasNext())
        {
            IColumn column = iter.next();
            emptyColumnFamily.getColumnSerializer().serialize(column, out);
        }
    }

    public void update(MessageDigest digest)
    {
        // no special-case for rows.size == 1, we're actually skipping some bytes here so just
        // blindly updating everything wouldn't be correct
        DataOutputBuffer out = new DataOutputBuffer();

        try
        {
            ColumnFamily.serializer().serializeCFInfo(emptyColumnFamily, out);
            out.writeInt(columnCount);
            digest.update(out.getData(), 0, out.getLength());
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }

        Iterator<IColumn> iter = iterator();
        while (iter.hasNext())
        {
            iter.next().updateDigest(digest);
        }
    }

    public boolean isEmpty()
    {
        boolean cfIrrelevant = ColumnFamilyStore.removeDeletedCF(emptyColumnFamily, controller.gcBefore) == null;
        return cfIrrelevant && columnCount == 0;
    }

    public int getEstimatedColumnCount()
    {
        int n = 0;
        for (SSTableIdentityIterator row : rows)
            n += row.columnCount;
        return n;
    }

    @SuppressWarnings("rawtypes")
	public AbstractType getComparator()
    {
        return emptyColumnFamily.getComparator();
    }

    @SuppressWarnings("unchecked")
	public Iterator<IColumn> iterator()
    {
        for (SSTableIdentityIterator row : rows)
            row.reset();
        reducer = new Reducer();
        Iterator<IColumn> iter = MergeIterator.get(rows, getComparator().columnComparator, reducer);
        return Iterators.filter(iter, Predicates.notNull());
    }

    public int columnCount()
    {
        return columnCount;
    }

    private class Reducer extends MergeIterator.Reducer<IColumn, IColumn>
    {
        ColumnFamily container = emptyColumnFamily.cloneMeShallow();
        long serializedSize = 4; // int for column count
        int size = 0;

        public void reduce(IColumn current)
        {
            container.addColumn(current);
        }

        protected IColumn getReduced()
        {
            assert container != null;
            IColumn reduced = container.iterator().next();
            ColumnFamily purged = shouldPurge ? ColumnFamilyStore.removeDeleted(container, controller.gcBefore) : container;
            if (purged != null && purged.metadata().getDefaultValidator().isCommutative())
            {
                CounterColumn.removeOldShards(purged, controller.gcBefore);
            }
            if (purged == null || !purged.iterator().hasNext())
            {
                container.clear();
                return null;
            }
            container.clear();
            serializedSize += reduced.serializedSize();
            size++;
            return reduced;
        }
    }
}
