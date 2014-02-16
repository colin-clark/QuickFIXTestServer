package com.cep.messaging.impls.gossip.service;

import java.io.IOError;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.commands.RowMutation;
import com.cep.messaging.impls.gossip.keyspace.filter.IdentityQueryFilter;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryFilter;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.keyspace.iterator.CloseableIterator;
import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.ReadResponse;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

public class RowRepairResolver extends AbstractRowResolver
{
    public RowRepairResolver(String table, ByteBuffer key)
    {
        super(key, table);
    }

    /*
    * This method handles the following scenario:
    *
    * there was a mismatch on the initial read (1a or 1b), so we redid the digest requests
    * as full data reads.  In this case we need to compute the most recent version
    * of each column, and send diffs to out-of-date replicas.
    */
    public Row resolve() throws DigestMismatchException, IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("resolving " + replies.size() + " responses");

        long startTime = System.currentTimeMillis();
		List<ColumnFamily> versions = new ArrayList<ColumnFamily>();
		List<InetAddress> endpoints = new ArrayList<InetAddress>();

        // case 1: validate digests against each other; throw immediately on mismatch.
        // also, collects data results into versions/endpoints lists.
        //
        // results are cleared as we process them, to avoid unnecessary duplication of work
        // when resolve() is called a second time for read repair on responses that were not
        // necessary to satisfy ConsistencyLevel.
        for (Map.Entry<Message, ReadResponse> entry : replies.entrySet())
        {
            Message message = entry.getKey();
            ReadResponse response = entry.getValue();
            assert !response.isDigestQuery();
            versions.add(response.row().cf);
            endpoints.add(message.getFrom());
        }

        ColumnFamily resolved;
        if (versions.size() > 1)
        {
            resolved = resolveSuperset(versions);
            if (logger.isDebugEnabled())
                logger.debug("versions merged");
            maybeScheduleRepairs(resolved, table, key, versions, endpoints);
        }
        else
        {
            resolved = versions.get(0);
        }

        if (logger.isDebugEnabled())
            logger.debug("resolve: " + (System.currentTimeMillis() - startTime) + " ms.");
		return new Row(key, resolved);
	}

    /**
     * For each row version, compare with resolved (the superset of all row versions);
     * if it is missing anything, send a mutation to the endpoint it come from.
     */
    @SuppressWarnings("rawtypes")
	public static void maybeScheduleRepairs(ColumnFamily resolved, String table, DecoratedKey key, List<ColumnFamily> versions, List<InetAddress> endpoints)
    {
        for (int i = 0; i < versions.size(); i++)
        {
            ColumnFamily diffCf = ColumnFamily.diff(versions.get(i), resolved);
            if (diffCf == null) // no repair needs to happen
                continue;

            // create and send the row mutation message based on the diff
            RowMutation rowMutation = new RowMutation(table, key.key);
            rowMutation.add(diffCf);
            Message repairMessage;
            try
            {
                repairMessage = rowMutation.getMessage(Gossiper.instance.getVersion(endpoints.get(i)));
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
            MessagingService.instance().sendOneWay(repairMessage, endpoints.get(i));
        }
    }

    static ColumnFamily resolveSuperset(List<ColumnFamily> versions)
    {
        assert versions.size() > 0;

        ColumnFamily resolved = null;
        for (ColumnFamily cf : versions)
        {
            if (cf == null)
                continue;

            if (resolved == null)
                resolved = cf.cloneMeShallow();
            else
                resolved.delete(cf);
        }
        if (resolved == null)
            return null;

        // mimic the collectCollatedColumn + removeDeleted path that getColumnFamily takes.
        // this will handle removing columns and subcolumns that are supressed by a row or
        // supercolumn tombstone.
        QueryFilter filter = new QueryFilter(null, new QueryPath(resolved.metadata().cfName), new IdentityQueryFilter());
        List<CloseableIterator<IColumn>> iters = new ArrayList<CloseableIterator<IColumn>>();
        for (ColumnFamily version : versions)
        {
            if (version == null)
                continue;
            iters.add(GossipUtilities.closeableIterator(version.getColumnsMap().values().iterator()));
        }
        filter.collateColumns(resolved, iters, resolved.metadata().comparator, Integer.MIN_VALUE);
        return ColumnFamilyStore.removeDeleted(resolved, Integer.MIN_VALUE);
    }

    public Row getData() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public boolean isDataPresent()
	{
        throw new UnsupportedOperationException();
    }
}
