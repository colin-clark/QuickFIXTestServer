package com.cep.messaging.impls.gossip.keyspace;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.lang.ArrayUtils;
import org.cliffc.high_scale_lib.NonBlockingHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.commands.RangeSliceCommand;
import com.cep.messaging.impls.gossip.keyspace.commands.RowMutation;
import com.cep.messaging.impls.gossip.keyspace.compaction.CompactionManager;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryFilter;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.net.FailureDetector;
import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.node.StorageProxy;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.node.state.ApplicationState;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.bounds.Range;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.service.DigestMismatchException;
import com.cep.messaging.impls.gossip.service.IWriteResponseHandler;
import com.cep.messaging.impls.gossip.service.WriteResponseHandler;
import com.cep.messaging.impls.gossip.thrift.ColumnParent;
import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;
import com.cep.messaging.impls.gossip.thrift.InvalidRequestException;
import com.cep.messaging.impls.gossip.thrift.SlicePredicate;
import com.cep.messaging.impls.gossip.thrift.SliceRange;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.JMXEnabledThreadPoolExecutor;
import com.cep.messaging.util.WrappedRunnable;



/**
 * For each endpoint for which we have hints, there is a row in the system hints CF.
 * The key for this row is ByteBuffer.wrap(string), i.e. "127.0.0.1".
 *
 * SuperColumns in that row are keys for which we have hinted data.
 * Subcolumns names within that supercolumn are keyspace+CF, concatenated with SEPARATOR.
 * Subcolumn values are always empty; instead, we store the row data "normally"
 * in the application table it belongs in.
 *
 * When FailureDetector signals that a node that was down is back up, we read its
 * hints row to see what rows we need to forward data for, then reach each row in its
 * entirety and send it over.
 *
 * deliverHints is also exposed to JMX so it can be run manually if FD ever misses
 * its cue somehow.
 *
 * HHM never deletes the row from Application tables; usually (but not for CL.ANY!)
 * the row belongs on this node, as well.  instead, we rely on cleanup compactions
 * to remove data that doesn't belong.  (Cleanup compactions may be started manually
 * -- on a per node basis -- with "nodeprobe cleanup.")
 *
 * TODO this avoids our hint rows from growing excessively large by offloading the
 * message data into application tables.  But, this means that cleanup compactions
 * will nuke HH data.  Probably better would be to store the RowMutation messages
 * in a HHData (non-super) CF, modifying the above to store a UUID value in the
 * HH subcolumn value, which we use as a key to a [standard] HHData system CF
 * that would contain the message bytes.
 */

public class HintedHandOffManager implements HintedHandOffManagerMBean
{
    public static final HintedHandOffManager instance = new HintedHandOffManager();
    public static final String HINTS_CF = "HintsColumnFamily";

    private static final Logger logger_ = LoggerFactory.getLogger(HintedHandOffManager.class);
    private static final int PAGE_SIZE = 10000;
    private static final String SEPARATOR = "-";
    private static final int LARGE_NUMBER = 65536; // 64k nodes ought to be enough for anybody.

    private final NonBlockingHashSet<InetAddress> queuedDeliveries = new NonBlockingHashSet<InetAddress>();

    private final ExecutorService executor_ = new JMXEnabledThreadPoolExecutor("HintedHandoff", NodeDescriptor.getCompactionThreadPriority());

    public HintedHandOffManager()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            mbs.registerMBean(this, new ObjectName("com.cep.messaging.impls.gossip.keyspace:type=HintedHandoffManager"));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public void registerMBean()
    {
        logger_.debug("Created HHOM instance, registered MBean.");
    }

    private static boolean sendRow(InetAddress endpoint, String tableName, String cfName, ByteBuffer key) throws IOException
    {
        if (!Gossiper.instance.isKnownEndpoint(endpoint))
        {
            logger_.warn("Hints found for endpoint " + endpoint + " which is not part of the gossip network.  discarding.");
            return true;
        }
        if (!FailureDetector.instance.isAlive(endpoint))
        {
            return false;
        }

        if (CFMetaData.getId(tableName, cfName) == null)
        {
            logger_.debug("Discarding hints for dropped keyspace or columnfamily {}/{}", tableName, cfName);
            return true;
        }
        Table table = Table.open(tableName);
        ColumnFamilyStore cfs = table.getColumnFamilyStore(cfName);

        int pageSize = PAGE_SIZE;
        // send less columns per page if they are very large
        if (cfs.getMeanColumns() > 0)
        {
            int averageColumnSize = (int) (cfs.getMeanRowSize() / cfs.getMeanColumns());
            pageSize = (int) Math.min(PAGE_SIZE, NodeDescriptor.getInMemoryCompactionLimit() / averageColumnSize);
            pageSize = Math.max(2, pageSize); // page size of 1 does not allow actual paging b/c of >= behavior on startColumn
            logger_.debug("average hinted-row column size is {}; using pageSize of {}", averageColumnSize, pageSize);
        }

        DecoratedKey<?> dkey = StorageService.getPartitioner().decorateKey(key);
        ByteBuffer startColumn = ByteBufferUtil.EMPTY_BYTE_BUFFER;
        while (true)
        {
            QueryFilter filter = QueryFilter.getSliceFilter(dkey, new QueryPath(cfs.getColumnFamilyName()), startColumn, ByteBufferUtil.EMPTY_BYTE_BUFFER, false, pageSize);
            ColumnFamily cf = cfs.getColumnFamily(filter);
            if (pagingFinished(cf, startColumn))
                break;
            if (cf.getColumnNames().isEmpty())
            {
                logger_.debug("Nothing to hand off for {}", dkey);
                break;
            }

            startColumn = cf.getColumnNames().last();
            RowMutation rm = new RowMutation(tableName, key);
            rm.add(cf);
            IWriteResponseHandler responseHandler =  WriteResponseHandler.create(endpoint);
            MessagingService.instance().sendRR(rm, endpoint, responseHandler);
            try
            {
                responseHandler.get();
            }
            catch (TimeoutException e)
            {
                return false;
            }

            try
            {
                Thread.sleep(NodeDescriptor.getHintedHandoffThrottleDelay());
            }
            catch (InterruptedException e)
            {
                throw new AssertionError(e);
            }
        }
        return true;
    }

    private static void deleteHintKey(ByteBuffer endpointAddress, ByteBuffer key, ByteBuffer tableCF, long timestamp) throws IOException
    {
        RowMutation rm = new RowMutation(Table.SYSTEM_TABLE, endpointAddress);
        rm.delete(new QueryPath(HINTS_CF, key, tableCF), timestamp);
        rm.apply();
    }

    public void deleteHintsForEndpoint(final String ipOrHostname)
    {
        try
        {
            InetAddress endpoint = InetAddress.getByName(ipOrHostname);
            deleteHintsForEndpoint(endpoint);
        }
        catch (UnknownHostException e)
        {
            logger_.warn("Unable to find "+ipOrHostname+", not a hostname or ipaddr of a node?:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void deleteHintsForEndpoint(final InetAddress endpoint)
    {
        final String ipaddr = endpoint.getHostAddress();
        final ColumnFamilyStore hintStore = Table.open(Table.SYSTEM_TABLE).getColumnFamilyStore(HINTS_CF);
        final RowMutation rm = new RowMutation(Table.SYSTEM_TABLE, ByteBufferUtil.bytes(ipaddr));
        rm.delete(new QueryPath(HINTS_CF), System.currentTimeMillis());

        // execute asynchronously to avoid blocking caller (which may be processing gossip)
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    logger_.info("Deleting any stored hints for " + ipaddr);
                    rm.apply();
                    hintStore.forceFlush();
                    CompactionManager.instance.submitMaximal(hintStore, Integer.MAX_VALUE);
                }
                catch (Exception e)
                {
                    logger_.warn("Could not delete hints for " + ipaddr + ": " + e);
                }
            }
        };
        StorageService.tasks.execute(runnable);
    }

    private static boolean pagingFinished(ColumnFamily hintColumnFamily, ByteBuffer startColumn)
    {
        // done if no hints found or the start column (same as last column processed in previous iteration) is the only one
        return hintColumnFamily == null
               || (hintColumnFamily.getSortedColumns().size() == 1 && hintColumnFamily.getColumn(startColumn) != null);
    }

    public static ByteBuffer makeCombinedName(String tableName, String columnFamily)
    {
        byte[] withsep = ArrayUtils.addAll(tableName.getBytes(UTF_8), SEPARATOR.getBytes(UTF_8));
        return ByteBuffer.wrap(ArrayUtils.addAll(withsep, columnFamily.getBytes(UTF_8)));
    }

    private static String[] getTableAndCFNames(ByteBuffer joined)
    {
        int index = ByteBufferUtil.lastIndexOf(joined, SEPARATOR.getBytes(UTF_8)[0], joined.limit());

        if (index == -1 || index < (joined.position() + 1))
            throw new RuntimeException("Corrupted hint name " + ByteBufferUtil.bytesToHex(joined));

        try
        {
            return new String[] { ByteBufferUtil.string(joined, joined.position(), index - joined.position()),
                                  ByteBufferUtil.string(joined, index + 1, joined.limit() - (index + 1)) };
        }
        catch (CharacterCodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private int waitForSchemaAgreement(InetAddress endpoint) throws InterruptedException
    {
        Gossiper gossiper = Gossiper.instance;
        int waited = 0;
        // first, wait for schema to be gossiped.
        while (gossiper.getEndpointStateForEndpoint(endpoint).getApplicationState(ApplicationState.SCHEMA) == null) {
            Thread.sleep(1000);
            waited += 1000;
            if (waited > 2 * StorageService.RING_DELAY)
                throw new RuntimeException("Didin't receive gossiped schema from " + endpoint + " in " + 2 * StorageService.RING_DELAY + "ms");
        }
        waited = 0;
        // then wait for the correct schema version.
        while (!gossiper.getEndpointStateForEndpoint(endpoint).getApplicationState(ApplicationState.SCHEMA).value.equals(
                gossiper.getEndpointStateForEndpoint(GossipUtilities.getLocalAddress()).getApplicationState(ApplicationState.SCHEMA).value))
        {
            Thread.sleep(1000);
            waited += 1000;
            if (waited > 2 * StorageService.RING_DELAY)
                throw new RuntimeException("Could not reach schema agreement with " + endpoint + " in " + 2 * StorageService.RING_DELAY + "ms");
        }
        logger_.debug("schema for {} matches local schema", endpoint);
        return waited;
    }
            
    private void deliverHintsToEndpoint(InetAddress endpoint) throws IOException, DigestMismatchException, InvalidRequestException, TimeoutException, InterruptedException
    {
        try
        {
            logger_.debug("Checking remote schema before delivering hints");
            int waited = waitForSchemaAgreement(endpoint);
            // sleep a random amount to stagger handoff delivery from different replicas.
            // (if we had to wait, then gossiper randomness took care of that for us already.)
            if (waited == 0) {
                int sleep = new Random().nextInt(60000);
                logger_.debug("Sleeping {}ms to stagger hint delivery", sleep);
                Thread.sleep(sleep);
            }
            if (!Gossiper.instance.getEndpointStateForEndpoint(endpoint).isAlive())
            {
                logger_.info("Endpoint {} died before hint delivery, aborting", endpoint);
                return;
            }
        }
        finally
        {
            queuedDeliveries.remove(endpoint);
        }

        logger_.info("Started hinted handoff for endpoint " + endpoint);

        // 1. Get the key of the endpoint we need to handoff
        // 2. For each column read the list of rows: subcolumns are KS + SEPARATOR + CF
        // 3. Delete the subcolumn if the write was successful
        // 4. Force a flush
        // 5. Do major compaction to clean up all deletes etc.
        ByteBuffer endpointAsUTF8 = ByteBufferUtil.bytes(endpoint.getHostAddress()); // keys have to be UTF8 to make OPP happy
        DecoratedKey<?> epkey =  StorageService.getPartitioner().decorateKey(endpointAsUTF8);
        int rowsReplayed = 0;
        ColumnFamilyStore hintStore = Table.open(Table.SYSTEM_TABLE).getColumnFamilyStore(HINTS_CF);
        ByteBuffer startColumn = ByteBufferUtil.EMPTY_BYTE_BUFFER;

        delivery:
        while (true)
        {
            QueryFilter filter = QueryFilter.getSliceFilter(epkey, new QueryPath(HINTS_CF), startColumn, ByteBufferUtil.EMPTY_BYTE_BUFFER, false, PAGE_SIZE);
            ColumnFamily hintColumnFamily = ColumnFamilyStore.removeDeleted(hintStore.getColumnFamily(filter), Integer.MAX_VALUE);
            if (pagingFinished(hintColumnFamily, startColumn))
                break;
            for (IColumn keyColumn : hintColumnFamily.getSortedColumns())
            {
                startColumn = keyColumn.name();
                Collection<IColumn> tableCFs = keyColumn.getSubColumns();
                for (IColumn tableCF : tableCFs)
                {
                    String[] parts = getTableAndCFNames(tableCF.name());
                    if (sendRow(endpoint, parts[0], parts[1], keyColumn.name()))
                    {
                        deleteHintKey(endpointAsUTF8, keyColumn.name(), tableCF.name(), tableCF.timestamp());
                        rowsReplayed++;
                    }
                    else
                    {
                        logger_.info("Could not complete hinted handoff to " + endpoint);
                        break delivery;
                    }

                    startColumn = keyColumn.name();
                }
            }
        }

        if (rowsReplayed > 0)
        {
            hintStore.forceFlush();
            try
            {
                CompactionManager.instance.submitMaximal(hintStore, Integer.MAX_VALUE).get();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        logger_.info(String.format("Finished hinted handoff of %s rows to endpoint %s",
                                   rowsReplayed, endpoint));
    }

    /*
     * This method is used to deliver hints to a particular endpoint.
     * When we learn that some endpoint is back up we deliver the data
     * to him via an event driven mechanism.
    */
    public void deliverHints(final InetAddress to)
    {
        if (!queuedDeliveries.add(to))
            return;

        Runnable r = new WrappedRunnable()
        {
            public void runMayThrow() throws Exception
            {
                deliverHintsToEndpoint(to);
            }
        };
    	executor_.execute(r);
    }

    public void deliverHints(String to) throws UnknownHostException
    {
        deliverHints(InetAddress.getByName(to));
    }

    public List<String> listEndpointsPendingHints()
    {
        List<Row> rows = getHintsSlice(1);

        // Extract the keys as strings to be reported.
        LinkedList<String> result = new LinkedList<String>();
        for (Row r : rows)
        {
            if (r.cf != null) //ignore removed rows
                result.addFirst(new String(r.key.key.array()));
        }
        return result;
    }

    public Map<String, Integer> countPendingHints()
    {
        List<Row> rows = getHintsSlice(Integer.MAX_VALUE);

        Map<String, Integer> result = new HashMap<String, Integer>();
        for (Row r : rows)
        {
            if (r.cf != null) //ignore removed rows
                result.put(new String(r.key.key.array()), r.cf.getColumnCount());
        }
        return result;
    }

    private List<Row> getHintsSlice(int column_count)
    {
        // ColumnParent for HintsCF...
        ColumnParent parent = new ColumnParent(HINTS_CF);

        // Get count # of columns...
        SlicePredicate predicate = new SlicePredicate();
        SliceRange sliceRange = new SliceRange();
        sliceRange.setStart(new byte[0]).setFinish(new byte[0]);
        sliceRange.setCount(column_count);
        predicate.setSlice_range(sliceRange);

        // From keys "" to ""...
        IPartitioner<?> partitioner = StorageService.getPartitioner();
        ByteBuffer empty = ByteBufferUtil.EMPTY_BYTE_BUFFER;
        Range range = new Range(partitioner.getToken(empty), partitioner.getToken(empty));

        // Get a bunch of rows!
        List<Row> rows;
        try
        {
            rows = StorageProxy.getRangeSlice(new RangeSliceCommand("system", parent, predicate, range, LARGE_NUMBER), ConsistencyLevel.ONE);
        }
        catch (Exception e)
        {
            logger_.info("HintsCF getEPPendingHints timed out.");
            throw new RuntimeException(e);
        }
        return rows;
    }
}
