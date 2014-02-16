package com.cep.messaging.impls.gossip.keyspace;

import java.util.ArrayList;
import java.util.List;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;

/**
 * Two approaches to building an IndexSummary:
 * 1. Call maybeAddEntry with every potential index entry
 * 2. Call shouldAddEntry, [addEntry,] incrementRowid
 */

@SuppressWarnings("rawtypes")
public class IndexSummary
{
    private ArrayList<KeyPosition> indexPositions;
    private long keysWritten = 0;

    public IndexSummary(long expectedKeys)
    {
        long expectedEntries = expectedKeys / NodeDescriptor.getIndexInterval();
        if (expectedEntries > Integer.MAX_VALUE)
            // TODO: that's a _lot_ of keys, or a very low interval
            throw new RuntimeException("Cannot use index_interval of " + NodeDescriptor.getIndexInterval() + " with " + expectedKeys + " (expected) keys.");
        indexPositions = new ArrayList<KeyPosition>((int)expectedEntries);
    }

    public void incrementRowid()
    {
        keysWritten++;
    }

    public boolean shouldAddEntry()
    {
        return keysWritten % NodeDescriptor.getIndexInterval() == 0;
    }

	public void addEntry(DecoratedKey decoratedKey, long indexPosition)
    {
        indexPositions.add(new KeyPosition(decoratedKey, indexPosition));
    }

    public void maybeAddEntry(DecoratedKey decoratedKey, long indexPosition)
    {
        if (shouldAddEntry())
            addEntry(decoratedKey, indexPosition);
        incrementRowid();
    }

    public List<KeyPosition> getIndexPositions()
    {
        return indexPositions;
    }

    public void complete()
    {
        indexPositions.trimToSize();
    }

    /**
     * This is a simple container for the index Key and its corresponding position
     * in the index file. Binary search is performed on a list of these objects
     * to find where to start looking for the index entry containing the data position
     * (which will be turned into a PositionSize object)
     */
    public static final class KeyPosition implements Comparable<KeyPosition>
    {
        public final DecoratedKey key;
        public final long indexPosition;

        public KeyPosition(DecoratedKey key, long indexPosition)
        {
            this.key = key;
            this.indexPosition = indexPosition;
        }

        public int compareTo(KeyPosition kp)
        {
            return key.compareTo(kp.key);
        }

        public String toString()
        {
            return key + ":" + indexPosition;
        }
    }
}
