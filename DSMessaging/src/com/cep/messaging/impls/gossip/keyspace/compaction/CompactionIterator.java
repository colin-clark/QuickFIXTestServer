package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.iterator.CloseableIterator;
import com.cep.messaging.impls.gossip.keyspace.iterator.IColumnIterator;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableIdentityIterator;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableScanner;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.util.MergeIterator;
import com.google.common.collect.AbstractIterator;

public class CompactionIterator extends AbstractIterator<AbstractCompactedRow>
implements CloseableIterator<AbstractCompactedRow>, CompactionInfo.Holder
{
    private static Logger logger = LoggerFactory.getLogger(CompactionIterator.class);

    public static final int FILE_BUFFER_SIZE = 1024 * 1024;

    private final MergeIterator<IColumnIterator, AbstractCompactedRow> source;
    protected final CompactionType type;
    protected final CompactionController controller;

    private long totalBytes;
    private long bytesRead;
    private long row;

    // the bytes that had been compacted the last time we delayed to throttle,
    // and the time in milliseconds when we last throttled
    private long bytesAtLastDelay;
    private long timeAtLastDelay;

    // current target bytes to compact per millisecond
    private int targetBytesPerMS = -1;

    public CompactionIterator(CompactionType type, Iterable<SSTableReader> sstables, CompactionController controller) throws IOException
    {
        this(type, getScanners(sstables), controller);
    }

    protected CompactionIterator(CompactionType type, List<SSTableScanner> scanners, CompactionController controller)
    {
        this.type = type;
        this.controller = controller;
        this.source = MergeIterator.get(scanners, ICOMP, new Reducer());
        row = 0;
        totalBytes = bytesRead = 0;
        for (SSTableScanner scanner : scanners)
            totalBytes += scanner.getFileLength();
    }

    protected static List<SSTableScanner> getScanners(Iterable<SSTableReader> sstables) throws IOException
    {
        ArrayList<SSTableScanner> scanners = new ArrayList<SSTableScanner>();
        for (SSTableReader sstable : sstables)
            scanners.add(sstable.getDirectScanner(FILE_BUFFER_SIZE));
        return scanners;
    }

    public CompactionInfo getCompactionInfo()
    {
        return new CompactionInfo(controller.getKeyspace(),
                                  controller.getColumnFamily(),
                                  type,
                                  bytesRead,
                                  totalBytes);
    }


    public AbstractCompactedRow computeNext()
    {
        if (!source.hasNext())
            return endOfData();
        return source.next();
    }

    private void throttle()
    {
        if (NodeDescriptor.getCompactionThroughputMbPerSec() < 1 || StorageService.instance.isBootstrapMode())
            // throttling disabled
            return;
        int totalBytesPerMS = NodeDescriptor.getCompactionThroughputMbPerSec() * 1024 * 1024 / 1000;

        // bytes compacted and time passed since last delay
        long bytesSinceLast = bytesRead - bytesAtLastDelay;
        long msSinceLast = System.currentTimeMillis() - timeAtLastDelay;

        // determine the current target
        int newTarget = totalBytesPerMS /
            Math.max(1, CompactionManager.instance.getActiveCompactions());
        if (newTarget != targetBytesPerMS)
            logger.info(String.format("%s now compacting at %d bytes/ms.",
                                      this,
                                      newTarget));
        targetBytesPerMS = newTarget;

        // the excess bytes that were compacted in this period
        long excessBytes = bytesSinceLast - msSinceLast * targetBytesPerMS;

        // the time to delay to recap the deficit
        long timeToDelay = excessBytes / Math.max(1, targetBytesPerMS);
        if (timeToDelay > 0)
        {
            if (logger.isTraceEnabled())
                logger.trace(String.format("Compacted %d bytes in %d ms: throttling for %d ms",
                                           bytesSinceLast, msSinceLast, timeToDelay));
            try { Thread.sleep(timeToDelay); } catch (InterruptedException e) { throw new AssertionError(e); }
        }
        bytesAtLastDelay = bytesRead;
        timeAtLastDelay = System.currentTimeMillis();
    }

    public void close() throws IOException
    {
        source.close();
    }

    @SuppressWarnings("unchecked")
	protected Iterable<SSTableScanner> getScanners()
    {
        return (Iterable<SSTableScanner>)(source.iterators());
    }

    public String toString()
    {
        return this.getCompactionInfo().toString();
    }
    
    protected class Reducer extends MergeIterator.Reducer<IColumnIterator, AbstractCompactedRow>
    {
        protected final List<SSTableIdentityIterator> rows = new ArrayList<SSTableIdentityIterator>();

        public void reduce(IColumnIterator current)
        {
            rows.add((SSTableIdentityIterator)current);
        }

        protected AbstractCompactedRow getReduced()
        {
            assert rows.size() > 0;

            try
            {
                AbstractCompactedRow compactedRow = controller.getCompactedRow(rows);
                if (compactedRow.isEmpty())
                {
                    controller.invalidateCachedRow(compactedRow.key);
                    return null;
                }

                // If the raw is cached, we call removeDeleted on it to have/ coherent query returns. However it would look
                // like some deleted columns lived longer than gc_grace + compaction. This can also free up big amount of
                // memory on long running instances
                controller.removeDeletedInCache(compactedRow.key);

                return compactedRow;
            }
            finally
            {
                rows.clear();
                if ((row++ % 1000) == 0)
                {
                    bytesRead = 0;
                    for (SSTableScanner scanner : getScanners())
                    {
                        bytesRead += scanner.getFilePointer();
                    }
                    throttle();
                }
            }
        }
    }

    public final static Comparator<IColumnIterator> ICOMP = new Comparator<IColumnIterator>()
    {
        public int compare(IColumnIterator i1, IColumnIterator i2)
        {
            return i1.getKey().compareTo(i2.getKey());
        }
    };
}
