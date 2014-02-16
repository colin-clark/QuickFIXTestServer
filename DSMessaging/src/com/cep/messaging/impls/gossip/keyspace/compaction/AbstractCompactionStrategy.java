package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;


/**
 * Pluggable compaction strategy determines how SSTables get merged.
 *
 * There are two main goals:
 *  - perform background compaction constantly as needed; this typically makes a tradeoff between
 *    i/o done by compaction, and merging done at read time.
 *  - perform a full (maximum possible) compaction if requested by the user
 */
public abstract class AbstractCompactionStrategy
{
    protected final ColumnFamilyStore cfs;
    protected final Map<String, String> options;

    protected AbstractCompactionStrategy(ColumnFamilyStore cfs, Map<String, String> options)
    {
        this.cfs = cfs;
        this.options = options;
    }

    /**
     * @return a list of compaction tasks that should run in the background to get the sstable
     * count down to desired parameters. Will not be null, but may be empty.
     * @param gcBefore throw away tombstones older than this
     */
    public abstract List<AbstractCompactionTask> getBackgroundTasks(final int gcBefore);

    /**
     * @return a list of compaction tasks that should be run to compact this columnfamilystore
     * as much as possible.  Will not be null, but may be empty.
     * @param gcBefore throw away tombstones older than this
     */
    public abstract List<AbstractCompactionTask> getMaximalTasks(final int gcBefore);

    /**
     * @return a compaction task corresponding to the requested sstables.
     * Will not be null. (Will throw if user requests an invalid compaction.)
     * @param gcBefore throw away tombstones older than this
     */
    public abstract AbstractCompactionTask getUserDefinedTask(Collection<SSTableReader> sstables, final int gcBefore);

    /**
     * @return the number of background tasks estimated to still be needed for this columnfamilystore
     */
    public abstract int getEstimatedRemainingTasks();
}
