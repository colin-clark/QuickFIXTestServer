package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.util.List;

public interface CompactionManagerMBean
{
    /** List of running compaction objects. */
    public List<CompactionInfo> getCompactions();

    /** List of running compaction summary strings. */
    public List<String> getCompactionSummary();

    /**
     * @return estimated number of compactions remaining to perform
     */
    public int getPendingTasks();

    /**
     * @return number of completed compactions since server [re]start
     */
    public long getCompletedTasks();

    /**
     * Triggers the compaction of user specified sstables.
     *
     * @param ksname the keyspace for the sstables to compact
     * @param dataFiles a comma separated list of sstable filename to compact
     */
    public void forceUserDefinedCompaction(String ksname, String dataFiles);
}
