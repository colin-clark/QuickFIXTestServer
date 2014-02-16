package com.cep.messaging.util;

import java.util.concurrent.atomic.AtomicLong;

import com.cep.messaging.impls.gossip.util.EstimatedHistogram;

public class LatencyTracker
{
    private final AtomicLong opCount = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);
    private long lastLatency = 0;
    private long lastOpCount = 0;
    private EstimatedHistogram totalHistogram = new EstimatedHistogram();
    private EstimatedHistogram recentHistogram = new EstimatedHistogram();

    /** takes nanoseconds **/
    public void addNano(long nanos)
    {
        // convert to microseconds.  1 millionth
        addMicro(nanos / 1000);
    }

    public void addMicro(long micros)
    {
        opCount.incrementAndGet();
        totalLatency.addAndGet(micros);
        totalHistogram.add(micros);
        recentHistogram.add(micros);
    }

    public long getOpCount()
    {
        return opCount.get();
    }

    /** returns  microseconds */
    public long getTotalLatencyMicros()
    {
        return totalLatency.get();
    }

    /** returns microseconds */
    public double getRecentLatencyMicros()
    {
        long ops = opCount.get();
        long n = totalLatency.get();
        try
        {
            return ((double)n - lastLatency) / (ops - lastOpCount);
        }
        finally
        {
            lastLatency = n;
            lastOpCount = ops;
        }
    }

    public long[] getTotalLatencyHistogramMicros()
    {
        return totalHistogram.getBuckets(false);
    }

    public long[] getRecentLatencyHistogramMicros()
    {
        return recentHistogram.getBuckets(true);
    }
}
