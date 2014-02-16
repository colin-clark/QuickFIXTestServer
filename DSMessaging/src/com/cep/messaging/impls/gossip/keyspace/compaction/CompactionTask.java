package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.compaction.CompactionManager.CompactionExecutorStatsCollector;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTable;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableWriter;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

public class CompactionTask extends AbstractCompactionTask
{
    private static final Logger logger = LoggerFactory.getLogger(CompactionTask.class);
    protected String compactionFileLocation;
    protected final int gcBefore;
    protected boolean isUserDefined;

    public CompactionTask(ColumnFamilyStore cfs, Collection<SSTableReader> sstables, final int gcBefore)
    {
        super(cfs, sstables);
        compactionFileLocation = null;
        this.gcBefore = gcBefore;
        this.isUserDefined = false;
    }

    /**
     * For internal use and testing only.  The rest of the system should go through the submit* methods,
     * which are properly serialized.
     * Caller is in charge of marking/unmarking the sstables as compacting.
     */
    @SuppressWarnings("rawtypes")
	public int execute(CompactionExecutorStatsCollector collector) throws IOException
    {
        // The collection of sstables passed may be empty (but not null); even if
        // it is not empty, it may compact down to nothing if all rows are deleted.
        assert sstables != null;

        Set<SSTableReader> toCompact = new HashSet<SSTableReader>(sstables);
        if (!isUserDefined)
        {
            if (toCompact.size() < 2)
            {
                logger.info("Nothing to compact in " + cfs.getColumnFamilyName() + "." +
                            "Use forceUserDefinedCompaction if you wish to force compaction of single sstables " +
                            "(e.g. for tombstone collection)");
                return 0;
            }

            if (compactionFileLocation == null)
                compactionFileLocation = cfs.table.getDataFileLocation(cfs.getExpectedCompactedFileSize(toCompact));

            // If the compaction file path is null that means we have no space left for this compaction.
            // Try again w/o the largest one.
            if (compactionFileLocation == null)
            {
                while (compactionFileLocation == null && toCompact.size() > 1)
                {
                    logger.warn("insufficient space to compact all requested files " + StringUtils.join(toCompact, ", "));
                    // Note that we have removed files that are still marked as compacting. This suboptimal but ok since the caller will unmark all
                    // the sstables at the end.
                    toCompact.remove(cfs.getMaxSizeFile(toCompact));
                    compactionFileLocation = cfs.table.getDataFileLocation(cfs.getExpectedCompactedFileSize(toCompact));
                }
            }

            if (compactionFileLocation == null)
            {
                logger.warn("insufficient space to compact even the two smallest files, aborting");
                return 0;
            }
        }

        if (NodeDescriptor.isSnapshotBeforeCompaction())
            cfs.table.snapshot(System.currentTimeMillis() + "-" + "compact-" + cfs.columnFamily);

        // sanity check: all sstables must belong to the same cfs
        for (SSTableReader sstable : toCompact)
            assert sstable.descriptor.cfname.equals(cfs.columnFamily);

        CompactionController controller = new CompactionController(cfs, toCompact, gcBefore, isUserDefined);
        // new sstables from flush can be added during a compaction, but only the compaction can remove them,
        // so in our single-threaded compaction world this is a valid way of determining if we're compacting
        // all the sstables (that existed when we started)
        CompactionType type = controller.isMajor()
                            ? CompactionType.MAJOR
                            : CompactionType.MINOR;
        logger.info("Compacting {}: {}", type, toCompact);

        long startTime = System.currentTimeMillis();
        long totalkeysWritten = 0;

        // TODO the int cast here is potentially buggy
        int expectedBloomFilterSize = Math.max(NodeDescriptor.getIndexInterval(), (int)SSTableReader.getApproximateKeyCount(toCompact));
        if (logger.isDebugEnabled())
            logger.debug("Expected bloom filter size : " + expectedBloomFilterSize);

        SSTableWriter writer;
        CompactionIterator ci = new CompactionIterator(type, toCompact, controller); // retain a handle so we can call close()
        Iterator<AbstractCompactedRow> nni = Iterators.filter(ci, Predicates.notNull());
        Map<DecoratedKey, Long> cachedKeys = new HashMap<DecoratedKey, Long>();

        if (collector != null)
            collector.beginCompaction(ci);
        try
        {
            if (!nni.hasNext())
            {
                // don't mark compacted in the finally block, since if there _is_ nondeleted data,
                // we need to sync it (via closeAndOpen) first, so there is no period during which
                // a crash could cause data loss.
                cfs.markCompacted(toCompact);
                return 0;
            }

            writer = cfs.createCompactionWriter(expectedBloomFilterSize, compactionFileLocation, toCompact);
            while (nni.hasNext())
            {
                AbstractCompactedRow row = nni.next();
                long position = writer.append(row);
                totalkeysWritten++;

                if (NodeDescriptor.getPreheatKeyCache())
                {
                    for (SSTableReader sstable : toCompact)
                    {
                        if (sstable.getCachedPosition(row.key) != null)
                        {
                            cachedKeys.put(row.key, position);
                            break;
                        }
                    }
                }
            }
        }
        finally
        {
            ci.close();
            if (collector != null)
                collector.finishCompaction(ci);
        }

        SSTableReader ssTable = writer.closeAndOpenReader(getMaxDataAge(toCompact));
        cfs.replaceCompactedSSTables(toCompact, Arrays.asList(ssTable));
        for (Entry<DecoratedKey, Long> entry : cachedKeys.entrySet()) // empty if preheat is off
            ssTable.cacheKey(entry.getKey(), entry.getValue());
        CompactionManager.instance.submitBackground(cfs);

        long dTime = System.currentTimeMillis() - startTime;
        long startsize = SSTable.getTotalBytes(toCompact);
        long endsize = ssTable.length();
        double ratio = (double)endsize / (double)startsize;
        logger.info(String.format("Compacted to %s.  %,d to %,d (~%d%% of original) bytes for %,d keys.  Time: %,dms.",
                ssTable.getFilename(), startsize, endsize, (int) (ratio * 100), totalkeysWritten, dTime));
        return toCompact.size();
    }

    public static long getMaxDataAge(Collection<SSTableReader> sstables)
    {
        long max = 0;
        for (SSTableReader sstable : sstables)
        {
            if (sstable.maxDataAge > max)
                max = sstable.maxDataAge;
        }
        return max;
    }

    public CompactionTask compactionFileLocation(String compactionFileLocation)
    {
        this.compactionFileLocation = compactionFileLocation;
        return this;
    }

    public CompactionTask isUserDefined(boolean isUserDefined)
    {
        this.isUserDefined = isUserDefined;
        return this;
    }
}
