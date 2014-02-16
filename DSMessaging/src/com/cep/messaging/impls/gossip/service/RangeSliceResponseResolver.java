package com.cep.messaging.impls.gossip.service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.iterator.CloseableIterator;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.RangeSliceReply;
import com.cep.messaging.util.MergeIterator;
import com.cep.messaging.util.Pair;
import com.google.common.collect.AbstractIterator;

/**
 * Turns RangeSliceReply objects into row (string -> CF) maps, resolving
 * to the most recent ColumnFamily and setting up read repairs as necessary.
 */
public class RangeSliceResponseResolver implements IResponseResolver<Iterable<Row>>
{
    @SuppressWarnings("unused")
	private static final Logger logger_ = LoggerFactory.getLogger(RangeSliceResponseResolver.class);
    private final String table;
    private final List<InetAddress> sources;
    protected final Collection<Message> responses = new LinkedBlockingQueue<Message>();;

    public RangeSliceResponseResolver(String table, List<InetAddress> sources)
    {
        this.sources = sources;
        this.table = table;
    }

    public List<Row> getData() throws IOException
    {
        Message response = responses.iterator().next();
        RangeSliceReply reply = RangeSliceReply.read(response.getMessageBody(), response.getVersion());
        return reply.rows;
    }

    // Note: this would deserialize the response a 2nd time if getData was called first.
    // (this is not currently an issue since we don't do read repair for range queries.)
    public Iterable<Row> resolve() throws IOException
    {
        ArrayList<RowIterator> iters = new ArrayList<RowIterator>(responses.size());
        int n = 0;
        for (Message response : responses)
        {
            RangeSliceReply reply = RangeSliceReply.read(response.getMessageBody(), response.getVersion());
            n = Math.max(n, reply.rows.size());
            iters.add(new RowIterator(reply.rows.iterator(), response.getFrom()));
        }
        // for each row, compute the combination of all different versions seen, and repair incomplete versions
        MergeIterator<Pair<Row,InetAddress>, Row> iter = MergeIterator.get(iters, new Comparator<Pair<Row,InetAddress>>()
        {
            public int compare(Pair<Row,InetAddress> o1, Pair<Row,InetAddress> o2)
            {
                return o1.left.key.compareTo(o2.left.key);
            }
        }, new MergeIterator.Reducer<Pair<Row,InetAddress>, Row>()
        {
            List<ColumnFamily> versions = new ArrayList<ColumnFamily>(sources.size());
            List<InetAddress> versionSources = new ArrayList<InetAddress>(sources.size());
            @SuppressWarnings("rawtypes")
			DecoratedKey key;

            public void reduce(Pair<Row,InetAddress> current)
            {
                key = current.left.key;
                versions.add(current.left.cf);
                versionSources.add(current.right);
            }

            protected Row getReduced()
            {
                ColumnFamily resolved = versions.size() > 1
                                      ? RowRepairResolver.resolveSuperset(versions)
                                      : versions.get(0);
                if (versions.size() < sources.size())
                {
                    // add placeholder rows for sources that didn't have any data, so maybeScheduleRepairs sees them
                    for (InetAddress source : sources)
                    {
                        if (!versionSources.contains(source))
                        {
                            versions.add(null);
                            versionSources.add(source);
                        }
                    }
                }
                RowRepairResolver.maybeScheduleRepairs(resolved, table, key, versions, versionSources);
                versions.clear();
                versionSources.clear();
                return new Row(key, resolved);
            }
        });

        List<Row> resolvedRows = new ArrayList<Row>(n);
        while (iter.hasNext())
            resolvedRows.add(iter.next());

        return resolvedRows;
    }

    public void preprocess(Message message)
    {
        responses.add(message);
    }

    public boolean isDataPresent()
    {
        return !responses.isEmpty();
    }

    private static class RowIterator extends AbstractIterator<Pair<Row,InetAddress>> implements CloseableIterator<Pair<Row,InetAddress>>
    {
        private final Iterator<Row> iter;
        private final InetAddress source;

        private RowIterator(Iterator<Row> iter, InetAddress source)
        {
            this.iter = iter;
            this.source = source;
        }

        protected Pair<Row,InetAddress> computeNext()
        {
            return iter.hasNext() ? new Pair<Row, InetAddress>(iter.next(), source) : endOfData();
        }

        public void close() {}
    }

    public Iterable<Message> getMessages()
    {
        return responses;
    }
}
