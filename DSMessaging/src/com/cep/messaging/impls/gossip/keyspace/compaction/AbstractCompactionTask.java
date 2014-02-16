package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.compaction.CompactionManager.CompactionExecutorStatsCollector;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;

public abstract class AbstractCompactionTask
{
    protected ColumnFamilyStore cfs;
    protected Collection<SSTableReader> sstables;

    public AbstractCompactionTask(ColumnFamilyStore cfs, Collection<SSTableReader> sstables)
    {
        this.cfs = cfs;
        this.sstables = sstables;
    }

    public abstract int execute(CompactionExecutorStatsCollector collector) throws IOException;

    public ColumnFamilyStore getColumnFamilyStore()
    {
        return cfs;
    }

    public Collection<SSTableReader> getSSTables()
    {
        return sstables;
    }

    /**
     * Try to mark the sstable to compact as compacting.
     * It returns true if some sstables have been marked for compaction, false
     * otherwise.
     * This *must* be called before calling execute(). Moreover,
     * unmarkSSTables *must* always be called after execute() if this
     * method returns true.
     */
    public boolean markSSTablesForCompaction()
    {
        return markSSTablesForCompaction(cfs.getMinimumCompactionThreshold(), cfs.getMaximumCompactionThreshold());
    }

    public boolean markSSTablesForCompaction(int min, int max)
    {
        Set<SSTableReader> marked = cfs.getDataTracker().markCompacting(sstables, min, max);

        if (marked == null || marked.isEmpty())
            return false;

        this.sstables = marked;
        return true;
    }

    public void unmarkSSTables()
    {
        cfs.getDataTracker().unmarkCompacting(sstables);
    }
}
