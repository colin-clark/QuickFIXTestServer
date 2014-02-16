package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.io.Serializable;

/** Implements serializable to allow structured info to be returned via JMX. */
public final class CompactionInfo implements Serializable
{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String ksname;
    private final String cfname;
    private final CompactionType tasktype;
    private final long bytesComplete;
    private final long totalBytes;

    public CompactionInfo(String ksname, String cfname, CompactionType tasktype, long bytesComplete, long totalBytes)
    {
        this.ksname = ksname;
        this.cfname = cfname;
        this.tasktype = tasktype;
        this.bytesComplete = bytesComplete;
        this.totalBytes = totalBytes;
    }

    /** @return A copy of this CompactionInfo with updated progress. */
    public CompactionInfo forProgress(long bytesComplete, long totalBytes)
    {
        return new CompactionInfo(ksname, cfname, tasktype, bytesComplete, totalBytes);
    }

    public String getKeyspace()
    {
        return ksname;
    }

    public String getColumnFamily()
    {
        return cfname;
    }

    public long getBytesComplete()
    {
        return bytesComplete;
    }

    public long getTotalBytes()
    {
        return totalBytes;
    }

    public CompactionType getTaskType()
    {
        return tasktype;
    }

    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(getTaskType()).append('@').append(hashCode());
        buff.append('(').append(getKeyspace()).append(", ").append(getColumnFamily());
        buff.append(", ").append(getBytesComplete()).append('/').append(getTotalBytes());
        return buff.append(')').toString();
    }

    public interface Holder
    {
        public CompactionInfo getCompactionInfo();
    }
}
