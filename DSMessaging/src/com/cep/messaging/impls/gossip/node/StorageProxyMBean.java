package com.cep.messaging.impls.gossip.node;

public interface StorageProxyMBean
{
    public long getReadOperations();
    public long getTotalReadLatencyMicros();
    public double getRecentReadLatencyMicros();
    public long[] getTotalReadLatencyHistogramMicros();
    public long[] getRecentReadLatencyHistogramMicros();

    public long getRangeOperations();
    public long getTotalRangeLatencyMicros();
    public double getRecentRangeLatencyMicros();
    public long[] getTotalRangeLatencyHistogramMicros();
    public long[] getRecentRangeLatencyHistogramMicros();

    public long getWriteOperations();
    public long getTotalWriteLatencyMicros();
    public double getRecentWriteLatencyMicros();
    public long[] getTotalWriteLatencyHistogramMicros();
    public long[] getRecentWriteLatencyHistogramMicros();

    public boolean getHintedHandoffEnabled();
    public void setHintedHandoffEnabled(boolean b);
    public int getMaxHintWindow();
    public void setMaxHintWindow(int ms);
}
