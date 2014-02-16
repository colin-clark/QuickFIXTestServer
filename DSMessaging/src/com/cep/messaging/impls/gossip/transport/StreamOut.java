package com.cep.messaging.impls.gossip.transport;

import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.sstable.Descriptor;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTable;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.Pair;
import com.google.common.collect.Iterables;

/**
 * This class handles streaming data from one node to another.
 *
 * The source node is in charge of the streaming session.  It begins the stream by sending
 * a Message with the stream bit flag in the Header turned on.  Part of that Message
 * will include a StreamHeader that includes the files that will be streamed as part
 * of that session, as well as the first file-to-be-streamed. (Combining session list
 * and first file like this is inconvenient, but not as inconvenient as the old
 * three-part send-file-list, wait-for-ack, start-first-file dance.)
 *
 * After each file, the target will send a StreamReply indicating success
 * (FILE_FINISHED) or failure (FILE_RETRY).
 *
 * When all files have been successfully transferred and integrated the source will send
 * SESSION_FINISHED and the session is complete.
 *
 * For Stream requests (for bootstrap), one subtlety is that we always have to
 * create at least one stream reply, even if the list of files is empty, otherwise the
 * target has no way to know that it can stop waiting for an answer.
 *
 */
public class StreamOut
{
    private static Logger logger = LoggerFactory.getLogger(StreamOut.class);

    /**
     * Stream the given ranges to the target endpoint from each CF in the given keyspace.
    */
    public static void transferRanges(InetAddress target, Table table, Collection<Range> ranges, Runnable callback, OperationType type)
    {
        StreamOutSession session = StreamOutSession.create(table.name, target, callback);
        transferRanges(session, table.getColumnFamilyStores(), ranges, type);
    }

    /**
     * Flushes matching column families from the given keyspace, or all columnFamilies
     * if the cf list is empty.
     */
    private static void flushSSTables(Iterable<ColumnFamilyStore> stores) throws IOException
    {
        logger.info("Flushing memtables for {}...", stores);
        List<Future<?>> flushes;
        flushes = new ArrayList<Future<?>>();
        for (ColumnFamilyStore cfstore : stores)
        {
            Future<?> flush = cfstore.forceFlush();
            if (flush != null)
                flushes.add(flush);
        }
        GossipUtilities.waitOnFutures(flushes);
    }

    /**
     * Stream the given ranges to the target endpoint from each of the given CFs.
    */
    public static void transferRanges(StreamOutSession session, Iterable<ColumnFamilyStore> cfses, Collection<Range> ranges, OperationType type)
    {
        assert ranges.size() > 0;

        logger.info("Beginning transfer to {}", session.getHost());
        logger.debug("Ranges are {}", StringUtils.join(ranges, ","));
        try
        {
            flushSSTables(cfses);
            Iterable<SSTableReader> sstables = Collections.emptyList();
            for (ColumnFamilyStore cfStore : cfses)
                sstables = Iterables.concat(sstables, cfStore.getSSTables());
            transferSSTables(session, sstables, ranges, type);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    /**
     * Low-level transfer of matching portions of a group of sstables from a single table to the target endpoint.
     * You should probably call transferRanges instead.
     */
    public static void transferSSTables(StreamOutSession session, Iterable<SSTableReader> sstables, Collection<Range> ranges, OperationType type) throws IOException
    {
        List<PendingFile> pending = createPendingFiles(sstables, ranges, type);

        // Even if the list of pending files is empty, we need to initiate the transfer otherwise
        // the remote end will hang in cases where this was a requested transfer.
        session.addFilesToStream(pending);
        session.begin();
    }

    // called prior to sending anything.
    private static List<PendingFile> createPendingFiles(Iterable<SSTableReader> sstables, Collection<Range> ranges, OperationType type)
    {
        List<PendingFile> pending = new ArrayList<PendingFile>();
        for (SSTableReader sstable : sstables)
        {
            Descriptor desc = sstable.descriptor;
            List<Pair<Long,Long>> sections = sstable.getPositionsForRanges(ranges);
            if (sections.isEmpty())
                continue;
            pending.add(new PendingFile(sstable, desc, SSTable.COMPONENT_DATA, sections, type));
        }
        logger.info("Stream context metadata {}, {} sstables.", pending, Iterables.size(sstables));
        return pending;
    }
}
