package com.cep.messaging.impls.gossip.keyspace.sstable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import com.cep.messaging.impls.gossip.keyspace.iterator.IColumnIterator;
import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.util.Pair;

/**
 * A SSTableScanner that only reads key in a given range (for validation compaction).
 */
public class SSTableBoundedScanner extends SSTableScanner
{
    private final Iterator<Pair<Long, Long>> rangeIterator;
    private Pair<Long, Long> currentRange;

    SSTableBoundedScanner(SSTableReader sstable, int bufferSize, boolean skipCache, Range range)
    {
        super(sstable, bufferSize, skipCache);
        this.rangeIterator = sstable.getPositionsForRanges(Collections.singletonList(range)).iterator();
        if (rangeIterator.hasNext())
        {
            currentRange = rangeIterator.next();
            try
            {
                file.seek(currentRange.left);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            exhausted = true;
        }
    }

    @Override
    public boolean hasNext()
    {
        if (iterator == null)
            iterator = exhausted ? Arrays.asList(new IColumnIterator[0]).iterator() : new BoundedKeyScanningIterator();
        return iterator.hasNext();
    }

    @Override
    public IColumnIterator next()
    {
        if (iterator == null)
            iterator = exhausted ? Arrays.asList(new IColumnIterator[0]).iterator() : new BoundedKeyScanningIterator();
        return iterator.next();
    }

    protected class BoundedKeyScanningIterator extends KeyScanningIterator
    {
        @Override
        public boolean hasNext()
        {
            if (!super.hasNext())
                return false;

            if (finishedAt < currentRange.right)
                return true;

            if (rangeIterator.hasNext())
            {
                currentRange = rangeIterator.next();
                finishedAt = currentRange.left; // next() will seek for us
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
