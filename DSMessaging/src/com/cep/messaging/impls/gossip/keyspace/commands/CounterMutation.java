package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.CounterColumn;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.SuperColumn;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.serialization.CounterMutationSerializer;
import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;

public class CounterMutation implements IMutation
{
    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CounterMutation.class);
    private static final CounterMutationSerializer serializer = new CounterMutationSerializer();

    private final RowMutation rowMutation;
    private final ConsistencyLevel consistency;

    private static final ThreadLocal<Random> random = new ThreadLocal<Random>()
    {
        @Override
        protected Random initialValue()
        {
            return new Random();
        }
    };

    public CounterMutation(RowMutation rowMutation, ConsistencyLevel consistency)
    {
        this.rowMutation = rowMutation;
        this.consistency = consistency;
    }

    public String getTable()
    {
        return rowMutation.getTable();
    }

    public Collection<Integer> getColumnFamilyIds()
    {
        return rowMutation.getColumnFamilyIds();
    }

    public ByteBuffer key()
    {
        return rowMutation.key();
    }

    public RowMutation rowMutation()
    {
        return rowMutation;
    }

    public ConsistencyLevel consistency()
    {
        return consistency;
    }

    public static CounterMutationSerializer serializer()
    {
        return serializer;
    }

    public RowMutation makeReplicationMutation() throws IOException
    {
        List<ReadCommand> readCommands = new LinkedList<ReadCommand>();
        for (ColumnFamily columnFamily : rowMutation.getColumnFamilies())
        {
            if (!columnFamily.metadata().getReplicateOnWrite())
                continue;
            addReadCommandFromColumnFamily(rowMutation.getTable(), rowMutation.key(), columnFamily, readCommands);
        }

        // create a replication RowMutation
        RowMutation replicationMutation = new RowMutation(rowMutation.getTable(), rowMutation.key());
        for (ReadCommand readCommand : readCommands)
        {
            Table table = Table.open(readCommand.table);
            Row row = readCommand.getRow(table);
            if (row == null || row.cf == null)
                continue;

            row = mergeOldShards(readCommand.table, row);
            replicationMutation.add(row.cf);
        }
        return replicationMutation;
    }

    private void addReadCommandFromColumnFamily(String table, ByteBuffer key, ColumnFamily columnFamily, List<ReadCommand> commands)
    {
        // CF type: regular
        if (!columnFamily.isSuper())
        {
            QueryPath queryPath = new QueryPath(columnFamily.metadata().cfName);
            commands.add(new SliceByNamesReadCommand(table, key, queryPath, columnFamily.getColumnNames()));
        }
        else
        {
            // CF type: super
            for (IColumn superColumn : columnFamily.getSortedColumns())
            {
                QueryPath queryPath = new QueryPath(columnFamily.metadata().cfName, superColumn.name());

                // construct set of sub-column names
                Collection<IColumn> subColumns = superColumn.getSubColumns();
                Collection<ByteBuffer> subColNames = new HashSet<ByteBuffer>(subColumns.size());
                for (IColumn subCol : subColumns)
                {
                    subColNames.add(subCol.name());
                }

                commands.add(new SliceByNamesReadCommand(table, key, queryPath, subColNames));
            }
        }
    }

    private Row mergeOldShards(String table, Row row) throws IOException
    {
        ColumnFamily cf = row.cf;
        // random check for merging to allow lessening the performance impact
        if (cf.metadata().getMergeShardsChance() > random.get().nextDouble())
        {
            ColumnFamily merger = computeShardMerger(cf);
            if (merger != null)
            {
                RowMutation localMutation = new RowMutation(table, row.key.key);
                localMutation.add(merger);
                localMutation.apply();

                cf.addAll(merger);
            }
        }
        return row;
    }

    private ColumnFamily computeShardMerger(ColumnFamily cf)
    {
        ColumnFamily merger = null;

        // CF type: regular
        if (!cf.isSuper())
        {
            for (IColumn column : cf.getSortedColumns())
            {
                if (!(column instanceof CounterColumn))
                    continue;
                IColumn c = ((CounterColumn)column).computeOldShardMerger();
                if (c != null)
                {
                    if (merger == null)
                        merger = cf.cloneMeShallow();
                    merger.addColumn(c);
                }
            }
        }
        else // CF type: super
        {
            for (IColumn superColumn : cf.getSortedColumns())
            {
                IColumn mergerSuper = null;
                for (IColumn column : superColumn.getSubColumns())
                {
                    if (!(column instanceof CounterColumn))
                        continue;
                    IColumn c = ((CounterColumn)column).computeOldShardMerger();
                    if (c != null)
                    {
                        if (mergerSuper == null)
                            mergerSuper = ((SuperColumn)superColumn).cloneMeShallow();
                        mergerSuper.addColumn(c);
                    }
                }
                if (mergerSuper != null)
                {
                    if (merger == null)
                        merger = cf.cloneMeShallow();
                    merger.addColumn(mergerSuper);
                }
            }
        }
        return merger;
    }

    public Message makeMutationMessage(int version) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        serializer().serialize(this, dos, version);
        return new Message(GossipUtilities.getLocalAddress(), Verb.COUNTER_MUTATION, bos.toByteArray(), version);
    }

    public boolean shouldReplicateOnWrite()
    {
        for (ColumnFamily cf : rowMutation.getColumnFamilies())
            if (cf.metadata().getReplicateOnWrite())
                return true;
        return false;
    }

    public void apply() throws IOException
    {
        // We need to transform all CounterUpdateColumn to CounterColumn and we need to deepCopy. Both are done 
        // below since CUC.asCounterColumn() does a deep copy.
        RowMutation rm = new RowMutation(rowMutation.getTable(), ByteBufferUtil.clone(rowMutation.key()));
        Table table = Table.open(rm.getTable());

        for (ColumnFamily cf_ : rowMutation.getColumnFamilies())
        {
            ColumnFamily cf = cf_.cloneMeShallow();
            ColumnFamilyStore cfs = table.getColumnFamilyStore(cf.id());
            for (IColumn column : cf_.getColumnsMap().values())
            {
                cf.addColumn(column.localCopy(cfs));
            }
            rm.add(cf);
        }
        rm.apply();
    }

    @Override
    public String toString()
    {
        return toString(false);
    }

    public String toString(boolean shallow)
    {
        StringBuilder buff = new StringBuilder("CounterMutation(");
        buff.append(rowMutation.toString(shallow));
        buff.append(", ").append(consistency.toString());
        return buff.append(")").toString();
    }
}
