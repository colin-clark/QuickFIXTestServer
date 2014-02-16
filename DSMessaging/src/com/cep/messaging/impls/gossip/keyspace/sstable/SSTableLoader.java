package com.cep.messaging.impls.gossip.keyspace.sstable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.impls.gossip.transport.OperationType;
import com.cep.messaging.impls.gossip.transport.PendingFile;
import com.cep.messaging.impls.gossip.transport.StreamOut;
import com.cep.messaging.impls.gossip.transport.StreamOutSession;
import com.cep.messaging.util.Pair;

/**
 * SSTable bulk loader.
 * Load an externally created sstable into a cluster.
 */
public class SSTableLoader
{
    private final File directory;
    private final String keyspace;
    private final Client client;
    private final OutputHandler outputHandler;

    public SSTableLoader(File directory, Client client, OutputHandler outputHandler)
    {
        this.directory = directory;
        this.keyspace = directory.getName();
        this.client = client;
        this.outputHandler = outputHandler;
    }

    protected Collection<SSTableReader> openSSTables()
    {
        final List<SSTableReader> sstables = new LinkedList<SSTableReader>();

        directory.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                Pair<Descriptor, Component> p = SSTable.tryComponentFromFilename(dir, name);
                Descriptor desc = p == null ? null : p.left;
                if (p == null || !p.right.equals(Component.DATA) || desc.temporary)
                    return false;

                if (!new File(desc.filenameFor(Component.PRIMARY_INDEX)).exists())
                {
                    outputHandler.output(String.format("Skipping file %s because index is missing", name));
                    return false;
                }

                if (!client.validateColumnFamily(keyspace, desc.cfname))
                {
                    outputHandler.output(String.format("Skipping file %s: column family %s.%s doesn't exist", name, keyspace, desc.cfname));
                    return false;
                }

                Set<Component> components = new HashSet<Component>();
                components.add(Component.DATA);
                components.add(Component.PRIMARY_INDEX);

                try
                {
                    sstables.add(SSTableReader.open(desc, components, null, StorageService.getPartitioner()));
                }
                catch (IOException e)
                {
                    outputHandler.output(String.format("Skipping file %s, error opening it: %s", name, e.getMessage()));
                }
                return false;
            }
        });
        return sstables;
    }

    public LoaderFuture stream() throws IOException
    {
        return stream(Collections.<InetAddress>emptySet());
    }

    public LoaderFuture stream(Set<InetAddress> toIgnore) throws IOException
    {
        client.init();

        Collection<SSTableReader> sstables = openSSTables();
        if (sstables.isEmpty())
        {
            outputHandler.output("No sstables to stream");
            return new LoaderFuture(0);
        }

        Map<InetAddress, Collection<Range>> endpointToRanges = client.getEndpointToRangesMap();
        outputHandler.output(String.format("Streaming revelant part of %sto %s", names(sstables), endpointToRanges.keySet()));

        // There will be one streaming session by endpoint
        LoaderFuture future = new LoaderFuture(endpointToRanges.size());
        for (Map.Entry<InetAddress, Collection<Range>> entry : endpointToRanges.entrySet())
        {
            InetAddress remote = entry.getKey();
            if (toIgnore.contains(remote))
            {
                future.latch.countDown();
                continue;
            }
            Collection<Range> ranges = entry.getValue();
            StreamOutSession session = StreamOutSession.create(keyspace, remote, new CountDownCallback(future.latch, remote));
            StreamOut.transferSSTables(session, sstables, ranges, OperationType.BULK_LOAD);
            future.setPendings(remote, session.getFiles());
        }
        return future;
    }

    public static class LoaderFuture implements Future<Void>
    {
        final CountDownLatch latch;
        final Map<InetAddress, Collection<PendingFile>> pendingFiles;

        private LoaderFuture(int request)
        {
            latch = new CountDownLatch(request);
            pendingFiles = new HashMap<InetAddress, Collection<PendingFile>>();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
		private void setPendings(InetAddress remote, Collection<PendingFile> files)
        {
            pendingFiles.put(remote, new ArrayList(files));
        }

        public boolean cancel(boolean mayInterruptIfRunning)
        {
            throw new UnsupportedOperationException("Cancellation is not yet supported");
        }

        public Void get() throws InterruptedException
        {
            latch.await();
            return null;
        }

        public Void get(long timeout, TimeUnit unit) throws InterruptedException
        {
            latch.await(timeout, unit);
            return null;
        }

        public boolean isCancelled()
        {
            // For now, cancellation is not supported, maybe one day...
            return false;
        }

        public boolean isDone()
        {
            return latch.getCount() == 0;
        }

        public Map<InetAddress, Collection<PendingFile>> getPendingFiles()
        {
            return pendingFiles;
        }
    }

    private String names(Collection<SSTableReader> sstables)
    {
        StringBuilder builder = new StringBuilder();
        for (SSTableReader sstable : sstables)
            builder.append(sstable.descriptor.filenameFor(Component.DATA)).append(" ");
        return builder.toString();
    }

    private class CountDownCallback implements Runnable
    {
        private final InetAddress endpoint;
        private final CountDownLatch latch;

        CountDownCallback(CountDownLatch latch, InetAddress endpoint)
        {
            this.latch = latch;
            this.endpoint = endpoint;
        }

        public void run()
        {
            latch.countDown();
            outputHandler.debug(String.format("Streaming session to %s completed (waiting on %d outstanding sessions)", endpoint, latch.getCount()));

            // There could be race with stop being called twice but it should be ok
            if (latch.getCount() == 0)
                client.stop();
        }
    }

    public interface OutputHandler
    {
        // called when an important info need to be displayed
        public void output(String msg);

        // called when a less important info need to be displayed
        public void debug(String msg);
    }

    public static abstract class Client
    {
        private final Map<InetAddress, Collection<Range>> endpointToRanges = new HashMap<InetAddress, Collection<Range>>();

        /**
         * Initialize the client.
         * Perform any step necessary so that after the call to the this
         * method:
         *   * StorageService is correctly initialized (so that gossip and
         *     messaging service is too)
         *   * getEndpointToRangesMap() returns a correct map
         * This method is guaranted to be called before any other method of a
         * client.
         */
        public abstract void init();

        /**
         * Stop the client.
         */
        public void stop() {}

        /**
         * Validate that {@code keyspace} is an existing keyspace and {@code
         * cfName} one of its existing column family.
         */
        public abstract boolean validateColumnFamily(String keyspace, String cfName);

        public Map<InetAddress, Collection<Range>> getEndpointToRangesMap()
        {
            return endpointToRanges;
        }

        protected void addRangeForEndpoint(Range range, InetAddress endpoint)
        {
            Collection<Range> ranges = endpointToRanges.get(endpoint);
            if (ranges == null)
            {
                ranges = new HashSet<Range>();
                endpointToRanges.put(endpoint, ranges);
            }
            ranges.add(range);
        }
    }
}
