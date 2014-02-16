package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.util.Pair;

public class SizeTieredCompactionStrategy extends AbstractCompactionStrategy
{
    private static final Logger logger = LoggerFactory.getLogger(SizeTieredCompactionStrategy.class);
    protected static final long DEFAULT_MIN_SSTABLE_SIZE = 50L * 1024L * 1024L;
    protected static final String MIN_SSTABLE_SIZE_KEY = "min_sstable_size";
    protected static long minSSTableSize;
    protected volatile int estimatedRemainingTasks;

    public SizeTieredCompactionStrategy(ColumnFamilyStore cfs, Map<String, String> options)
    {
       super(cfs, options);
       this.estimatedRemainingTasks = 0;
       String optionValue = options.get(MIN_SSTABLE_SIZE_KEY);
       minSSTableSize = (null != optionValue) ? Long.parseLong(optionValue) : DEFAULT_MIN_SSTABLE_SIZE;
    }

    public List<AbstractCompactionTask> getBackgroundTasks(final int gcBefore)
    {
        if (cfs.isCompactionDisabled())
        {
            logger.debug("Compaction is currently disabled.");
            return Collections.<AbstractCompactionTask>emptyList();
        }

        List<AbstractCompactionTask> tasks = new LinkedList<AbstractCompactionTask>();
        List<List<SSTableReader>> buckets = getBuckets(createSSTableAndLengthPairs(cfs.getSSTables()), minSSTableSize);

        for (List<SSTableReader> bucket : buckets)
        {
            if (bucket.size() < cfs.getMinimumCompactionThreshold())
                continue;

            Collections.sort(bucket, new Comparator<SSTableReader>()
            {
                public int compare(SSTableReader o1, SSTableReader o2)
                {
                    return o1.descriptor.generation - o2.descriptor.generation;
                }
            });
            tasks.add(new CompactionTask(cfs, bucket.subList(0, Math.min(bucket.size(), cfs.getMaximumCompactionThreshold())), gcBefore));
        }

        updateEstimatedCompactionsByTasks(tasks);
        return tasks;
    }

    public List<AbstractCompactionTask> getMaximalTasks(final int gcBefore)
    {
        List<AbstractCompactionTask> tasks = new LinkedList<AbstractCompactionTask>();
        if (!cfs.getSSTables().isEmpty())
            tasks.add(new CompactionTask(cfs, cfs.getSSTables(), gcBefore));
        return tasks;
    }

    public AbstractCompactionTask getUserDefinedTask(Collection<SSTableReader> sstables, final int gcBefore)
    {
        return new CompactionTask(cfs, sstables, gcBefore)
                .isUserDefined(true)
                .compactionFileLocation(cfs.table.getDataFileLocation(1));
    }

    public int getEstimatedRemainingTasks()
    {
        return estimatedRemainingTasks;
    }

    private static List<Pair<SSTableReader, Long>> createSSTableAndLengthPairs(Collection<SSTableReader> collection)
    {
        List<Pair<SSTableReader, Long>> tableLengthPairs = new ArrayList<Pair<SSTableReader, Long>>();
        for(SSTableReader table: collection)
            tableLengthPairs.add(new Pair<SSTableReader, Long>(table, table.length()));
        return tableLengthPairs;
    }

    /*
     * Group files of similar size into buckets.
     */
    static <T> List<List<T>> getBuckets(Collection<Pair<T, Long>> files, long minSSTableSize)
    {
        // Sort the list in order to get deterministic results during the grouping below
        List<Pair<T, Long>> sortedFiles = new ArrayList<Pair<T, Long>>(files);
        Collections.sort(sortedFiles, new Comparator<Pair<T, Long>>()
        {
            public int compare(Pair<T, Long> p1, Pair<T, Long> p2)
            {
                return p1.right.compareTo(p2.right);
            }
        });

        Map<List<T>, Long> buckets = new HashMap<List<T>, Long>();

        for (Pair<T, Long> pair: sortedFiles)
        {
            long size = pair.right;

            boolean bFound = false;
            // look for a bucket containing similar-sized files:
            // group in the same bucket if it's w/in 50% of the average for this bucket,
            // or this file and the bucket are all considered "small" (less than `minSSTableSize`)
            for (Entry<List<T>, Long> entry : buckets.entrySet())
            {
                List<T> bucket = entry.getKey();
                long averageSize = entry.getValue();
                if ((size > (averageSize / 2) && size < (3 * averageSize) / 2)
                    || (size < minSSTableSize && averageSize < minSSTableSize))
                {
                    // remove and re-add because adding changes the hash
                    buckets.remove(bucket);
                    long totalSize = bucket.size() * averageSize;
                    averageSize = (totalSize + size) / (bucket.size() + 1);
                    bucket.add(pair.left);
                    buckets.put(bucket, averageSize);
                    bFound = true;
                    break;
                }
            }
            // no similar bucket found; put it in a new one
            if (!bFound)
            {
                ArrayList<T> bucket = new ArrayList<T>();
                bucket.add(pair.left);
                buckets.put(bucket, size);
            }
        }

        return new LinkedList<List<T>>(buckets.keySet());
    }

    private void updateEstimatedCompactionsByTasks(List<AbstractCompactionTask> tasks)
    {
        int n = 0;
        for (AbstractCompactionTask task: tasks)
        {
            if (!(task instanceof CompactionTask))
                continue;

            Collection<SSTableReader> sstablesToBeCompacted = task.getSSTables();
            if (sstablesToBeCompacted.size() >= cfs.getMinimumCompactionThreshold())
                n += Math.ceil((double)sstablesToBeCompacted.size() / cfs.getMaximumCompactionThreshold());
        }
        estimatedRemainingTasks = n;
    }

    public long getMinSSTableSize()
    {
        return minSSTableSize;
    }

    public String toString()
    {
        return String.format("SizeTieredCompactionStrategy[%s/%s]",
            cfs.getMinimumCompactionThreshold(),
            cfs.getMaximumCompactionThreshold());
    }
}
