package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.MergeIterator;

@SuppressWarnings("rawtypes")
public class ReducingKeyIterator implements CloseableIterator<DecoratedKey>
{
    private final MergeIterator<DecoratedKey,DecoratedKey> mi;

    public ReducingKeyIterator(Collection<SSTableReader> sstables)
    {
        ArrayList<KeyIterator> iters = new ArrayList<KeyIterator>();
        for (SSTableReader sstable : sstables)
            iters.add(new KeyIterator(sstable.descriptor));
        mi = MergeIterator.get(iters, DecoratedKey.comparator, new MergeIterator.Reducer<DecoratedKey,DecoratedKey>()
        {
            DecoratedKey reduced = null;

            public void reduce(DecoratedKey current)
            {
                reduced = current;
            }

            protected DecoratedKey getReduced()
            {
                return reduced;
            }
        });
    }

    public void close() throws IOException
    {
        for (Object o : mi.iterators())
        {
            ((CloseableIterator)o).close();
        }
    }

    public long getTotalBytes()
    {
        long m = 0;
        for (Object o : mi.iterators())
        {
            m += ((KeyIterator) o).getTotalBytes();
        }
        return m;
    }

    public long getBytesRead()
    {
        long m = 0;
        for (Object o : mi.iterators())
        {
            m += ((KeyIterator) o).getBytesRead();
        }
        return m;
    }

    public String getTaskType()
    {
        return "Secondary index build";
    }

    public boolean hasNext()
    {
        return mi.hasNext();
    }

    public DecoratedKey next()
    {
        return mi.next();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
