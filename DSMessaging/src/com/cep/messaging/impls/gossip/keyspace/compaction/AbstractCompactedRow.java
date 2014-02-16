package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.io.DataOutput;
import java.io.IOException;
import java.security.MessageDigest;

import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;

/**
 * a CompactedRow is an object that takes a bunch of rows (keys + columnfamilies)
 * and can write a compacted version of those rows to an output stream.  It does
 * NOT necessarily require creating a merged CF object in memory.
 */

@SuppressWarnings("rawtypes")
public abstract class AbstractCompactedRow
{
	public final DecoratedKey key;

    public AbstractCompactedRow(DecoratedKey key)
    {
        this.key = key;
    }

    /**
     * write the row (size + column index + filter + column data, but NOT row key) to @param out
     */
    public abstract void write(DataOutput out) throws IOException;

    /**
     * update @param digest with the data bytes of the row (not including row key or row size)
     */
    public abstract void update(MessageDigest digest);

    /**
     * @return true if there are no columns in the row AND there are no row-level tombstones to be preserved
     */
    public abstract boolean isEmpty();

    /**
     * @return the number of columns in the row
     */
    public abstract int columnCount();
}
