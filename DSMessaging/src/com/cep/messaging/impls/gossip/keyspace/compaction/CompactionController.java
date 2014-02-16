package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.EchoedRow;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableIdentityIterator;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

/**
 * Manage compaction options.
 */

@SuppressWarnings("rawtypes")
public class CompactionController
{
    private static Logger logger = LoggerFactory.getLogger(CompactionController.class);

    private final ColumnFamilyStore cfs;
    private final Set<SSTableReader> sstables;
    private final boolean forceDeserialize;

    public final boolean isMajor;
    public final int gcBefore;

    public CompactionController(ColumnFamilyStore cfs, Collection<SSTableReader> sstables, int gcBefore, boolean forceDeserialize)
    {
        assert cfs != null;
        this.cfs = cfs;
        this.sstables = new HashSet<SSTableReader>(sstables);
        this.gcBefore = gcBefore;
        this.forceDeserialize = forceDeserialize;
        isMajor = cfs.isCompleteSSTables(this.sstables);
    }

    /** @return the keyspace name */
    public String getKeyspace()
    {
        return cfs.table.name;
    }

    /** @return the column family name */
    public String getColumnFamily()
    {
        return cfs.columnFamily;
    }

	public boolean shouldPurge(DecoratedKey key)
    {
        return isMajor || !cfs.isKeyInRemainingSSTables(key, sstables);
    }

    public boolean needDeserialize()
    {
        if (forceDeserialize)
            return true;

        for (SSTableReader sstable : sstables)
            if (!sstable.descriptor.isLatestVersion)
                return true;

        return false;
    }

    public void invalidateCachedRow(DecoratedKey key)
    {
        cfs.invalidateCachedRow(key);
    }

    public void removeDeletedInCache(DecoratedKey key)
    {
        ColumnFamily cachedRow = cfs.getRawCachedRow(key);
        if (cachedRow != null)
            ColumnFamilyStore.removeDeleted(cachedRow, gcBefore);
    }

    public boolean isMajor()
    {
        return isMajor;
    }

    /**
     * @return an AbstractCompactedRow implementation to write the merged rows in question.
     *
     * If there is a single source row, the data is from a current-version sstable, we don't
     * need to purge and we aren't forcing deserialization for scrub, write it unchanged.
     * Otherwise, we deserialize, purge tombstones, and reserialize in the latest version.
     */
    public AbstractCompactedRow getCompactedRow(List<SSTableIdentityIterator> rows)
    {
        if (rows.size() == 1 && !needDeserialize() && !shouldPurge(rows.get(0).getKey()))
            return new EchoedRow(rows.get(0));

        long rowSize = 0;
        for (SSTableIdentityIterator row : rows)
            rowSize += row.dataSize;

        if (rowSize > NodeDescriptor.getInMemoryCompactionLimit())
        {
            logger.info(String.format("Compacting large row %s (%d bytes) incrementally",
                                      ByteBufferUtil.bytesToHex(rows.get(0).getKey().key), rowSize));
            return new LazilyCompactedRow(this, rows);
        }
        return new PrecompactedRow(this, rows);
    }

    /** convenience method for single-sstable compactions */
    public AbstractCompactedRow getCompactedRow(SSTableIdentityIterator row)
    {
        return getCompactedRow(Collections.singletonList(row));
    }
}
