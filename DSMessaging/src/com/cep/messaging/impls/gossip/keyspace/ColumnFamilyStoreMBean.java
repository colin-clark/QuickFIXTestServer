package com.cep.messaging.impls.gossip.keyspace;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.cep.messaging.util.exception.ConfigurationException;

/**
 * The MBean interface for ColumnFamilyStore
 */
public interface ColumnFamilyStoreMBean
{
    /**
     * @return the name of the column family
     */
    public String getColumnFamilyName();
    
    /**
     * Returns the total amount of data stored in the memtable, including
     * column related overhead.
     * 
     * @return The size in bytes.
     */
    public long getMemtableDataSize();
    
    /**
     * Returns the total number of columns present in the memtable.
     * 
     * @return The number of columns.
     */
    public long getMemtableColumnsCount();
    
    /**
     * Returns the number of times that a flush has resulted in the
     * memtable being switched out.
     *
     * @return the number of memtable switches
     */
    public int getMemtableSwitchCount();

    /**
     * Triggers an immediate memtable flush.
     */
    public Object forceFlush() throws IOException;

    /**
     * @return a histogram of the number of sstable data files accessed per read: reading this property resets it
     */
    public long[] getRecentSSTablesPerReadHistogram();

    /**
     * @return a histogram of the number of sstable data files accessed per read
     */
    public long[] getSSTablesPerReadHistogram();

    /**
     * @return the number of read operations on this column family
     */
    public long getReadCount();

    /**
     * @return total read latency (divide by getReadCount() for average)
     */
    public long getTotalReadLatencyMicros();

    /**
     * @return an array representing the latency histogram
     */
    public long[] getLifetimeReadLatencyHistogramMicros();

    /**
     * @return an array representing the latency histogram
     */
    public long[] getRecentReadLatencyHistogramMicros();

    /**
     * @return average latency per read operation since the last call
     */
    public double getRecentReadLatencyMicros();

    /**
     * @return the number of write operations on this column family
     */
    public long getWriteCount();
    
    /**
     * @return total write latency (divide by getReadCount() for average)
     */
    public long getTotalWriteLatencyMicros();

    /**
     * @return an array representing the latency histogram
     */
    public long[] getLifetimeWriteLatencyHistogramMicros();

    /**
     * @return an array representing the latency histogram
     */
    public long[] getRecentWriteLatencyHistogramMicros();

    /**
     * @return average latency per write operation since the last call
     */
    public double getRecentWriteLatencyMicros();

    /**
     * @return the estimated number of tasks pending for this column family
     */
    public int getPendingTasks();

    /**
     * @return the number of SSTables on disk for this CF
     */
    public int getLiveSSTableCount();

    /**
     * @return disk space used by SSTables belonging to this CF
     */
    public long getLiveDiskSpaceUsed();

    /**
     * @return total disk space used by SSTables belonging to this CF, including obsolete ones waiting to be GC'd
     */
    public long getTotalDiskSpaceUsed();

    /**
     * force a major compaction of this column family
     */
    public void forceMajorCompaction() throws ExecutionException, InterruptedException;

    /**
     * invalidate the key cache; for use after invalidating row cache
     */
    public void invalidateKeyCache();

    /**
     * invalidate the row cache; for use after bulk loading via BinaryMemtable
     */
    public void invalidateRowCache();


    /**
     * return the size of the smallest compacted row
     * @return
     */
    public long getMinRowSize();

    /**
     * return the size of the largest compacted row
     * @return
     */
    public long getMaxRowSize();

    /**
     * return the mean size of the rows compacted
     * @return
     */
    public long getMeanRowSize();

    public long getBloomFilterFalsePositives();

    public long getRecentBloomFilterFalsePositives();

    public double getBloomFilterFalseRatio();

    public double getRecentBloomFilterFalseRatio();

    /**
     * Gets the minimum number of sstables in queue before compaction kicks off
     */
    public int getMinimumCompactionThreshold();

    /**
     * Sets the minimum number of sstables in queue before compaction kicks off
     */
    public void setMinimumCompactionThreshold(int threshold);

    /**
     * Gets the maximum number of sstables in queue before compaction kicks off
     */
    public int getMaximumCompactionThreshold();

    /**
     * Sets the maximum number of sstables in queue before compaction kicks off
     */
    public void setMaximumCompactionThreshold(int threshold);

    /**
     * Disable automatic compaction.
     */
    public void disableAutoCompaction();

    public int getMemtableThroughputInMB();
    public void setMemtableThroughputInMB(int size) throws ConfigurationException;

    public double getMemtableOperationsInMillions();
    public void setMemtableOperationsInMillions(double ops) throws ConfigurationException;

    public long estimateKeys();

    public long[] getEstimatedRowSizeHistogram();
    public long[] getEstimatedColumnCountHistogram();

    /**
     * Returns a list of the names of the built column indexes for current store
     * @return list of the index names
     */
    public List<String> getBuiltIndexes();

    public int getRowCacheSavePeriodInSeconds();
    public void setRowCacheSavePeriodInSeconds(int rcspis);

    public int getKeyCacheSavePeriodInSeconds();
    public void setKeyCacheSavePeriodInSeconds(int kcspis);
}
