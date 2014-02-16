package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyType;
import com.cep.messaging.impls.gossip.keyspace.HintedHandOffManager;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.SuperColumn;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;
import com.cep.messaging.impls.gossip.thrift.ColumnOrSuperColumn;
import com.cep.messaging.impls.gossip.thrift.Deletion;
import com.cep.messaging.impls.gossip.transport.MessageProducer;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;

public class RowMutation implements IMutation, MessageProducer
{
    private static RowMutationSerializer serializer_ = new RowMutationSerializer();
    public static final String HINT = "HINT";
    public static final String FORWARD_HEADER = "FORWARD";

    public static RowMutationSerializer serializer()
    {
        return serializer_;
    }

    private String table_;
    private ByteBuffer key_;
    // map of column family id to mutations for that column family.
    protected Map<Integer, ColumnFamily> modifications_ = new HashMap<Integer, ColumnFamily>();

    private Map<Integer, byte[]> preserializedBuffers = new HashMap<Integer, byte[]>();

    public RowMutation(String table, ByteBuffer key)
    {
        table_ = table;
        key_ = key;
    }

    public RowMutation(String table, Row row)
    {
        table_ = table;
        key_ = row.key.key;
        add(row.cf);
    }

    protected RowMutation(String table, ByteBuffer key, Map<Integer, ColumnFamily> modifications)
    {
        table_ = table;
        key_ = key;
        modifications_ = modifications;
    }

    public String getTable()
    {
        return table_;
    }

    public Collection<Integer> getColumnFamilyIds()
    {
        return modifications_.keySet();
    }

    public ByteBuffer key()
    {
        return key_;
    }

    public Collection<ColumnFamily> getColumnFamilies()
    {
        return modifications_.values();
    }

    public void addHints(RowMutation rm) throws IOException
    {
        for (ColumnFamily cf : rm.getColumnFamilies())
        {
            ByteBuffer combined = HintedHandOffManager.makeCombinedName(rm.getTable(), cf.metadata().cfName);
            QueryPath path = new QueryPath(HintedHandOffManager.HINTS_CF, rm.key(), combined);
            add(path, ByteBufferUtil.EMPTY_BYTE_BUFFER, System.currentTimeMillis(), cf.metadata().getGcGraceSeconds());
        }
    }

    /*
     * Specify a column family name and the corresponding column
     * family object.
     * param @ cf - column family name
     * param @ columnFamily - the column family.
     */
    public void add(ColumnFamily columnFamily)
    {
        assert columnFamily != null;
        ColumnFamily prev = modifications_.put(columnFamily.id(), columnFamily);
        if (prev != null)
            // developer error
            throw new IllegalArgumentException("ColumnFamily " + columnFamily + " already has modifications in this mutation: " + prev);
    }

    public boolean isEmpty()
    {
        return modifications_.isEmpty();
    }

    /*
     * Specify a column name and a corresponding value for
     * the column. Column name is specified as <column family>:column.
     * This will result in a ColumnFamily associated with
     * <column family> as name and a Column with <column>
     * as name. The column can be further broken up
     * as super column name : columnname  in case of super columns
     *
     * param @ cf - column name as <column family>:<column>
     * param @ value - value associated with the column
     * param @ timestamp - timestamp associated with this data.
     * param @ timeToLive - ttl for the column, 0 for standard (non expiring) columns
     */
    public void add(QueryPath path, ByteBuffer value, long timestamp, int timeToLive)
    {
        Integer id = CFMetaData.getId(table_, path.columnFamilyName);
        ColumnFamily columnFamily = modifications_.get(id);
        if (columnFamily == null)
        {
            columnFamily = ColumnFamily.create(table_, path.columnFamilyName);
            modifications_.put(id, columnFamily);
        }
        columnFamily.addColumn(path, value, timestamp, timeToLive);
    }

    public void addCounter(QueryPath path, long value)
    {
        Integer id = CFMetaData.getId(table_, path.columnFamilyName);
        ColumnFamily columnFamily = modifications_.get(id);
        if (columnFamily == null)
        {
            columnFamily = ColumnFamily.create(table_, path.columnFamilyName);
            modifications_.put(id, columnFamily);
        }
        columnFamily.addCounter(path, value);
    }

    public void add(QueryPath path, ByteBuffer value, long timestamp)
    {
        add(path, value, timestamp, 0);
    }

    @SuppressWarnings("deprecation")
	public void delete(QueryPath path, long timestamp)
    {
        Integer id = CFMetaData.getId(table_, path.columnFamilyName);

        int localDeleteTime = (int) (System.currentTimeMillis() / 1000);

        ColumnFamily columnFamily = modifications_.get(id);
        if (columnFamily == null)
        {
            columnFamily = ColumnFamily.create(table_, path.columnFamilyName);
            modifications_.put(id, columnFamily);
        }

        if (path.superColumnName == null && path.columnName == null)
        {
            columnFamily.delete(localDeleteTime, timestamp);
        }
        else if (path.columnName == null)
        {
            SuperColumn sc = new SuperColumn(path.superColumnName, columnFamily.getSubComparator());
            sc.markForDeleteAt(localDeleteTime, timestamp);
            columnFamily.addColumn(sc);
        }
        else
        {
            columnFamily.addTombstone(path, localDeleteTime, timestamp);
        }
    }

    /*
     * This is equivalent to calling commit. Applies the changes to
     * to the table that is obtained by calling Table.open().
     */
    public void apply() throws IOException
    {
        KSMetaData ksm = NodeDescriptor.getTableDefinition(getTable());
        
        Table.open(table_).apply(this, ksm.isDurableWrites());
    }

    public void applyUnsafe() throws IOException
    {
        Table.open(table_).apply(this, false);
    }

    public Message getMessage(Integer version) throws IOException
    {
        return makeRowMutationMessage(Verb.MUTATION, version);
    }

    public Message makeRowMutationMessage(Verb verb, int version) throws IOException
    {
        return new Message(GossipUtilities.getLocalAddress(), verb, getSerializedBuffer(version), version);
    }

    public synchronized byte[] getSerializedBuffer(int version) throws IOException
    {
        byte[] preserializedBuffer = preserializedBuffers.get(version);
        if (preserializedBuffer == null)
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            RowMutation.serializer().serialize(this, dout, version);
            dout.close();
            preserializedBuffer = bout.toByteArray();
            preserializedBuffers.put(version, preserializedBuffer);
        }
        return preserializedBuffer;
    }

    public String toString()
    {
        return toString(false);
    }

    public String toString(boolean shallow)
    {
        StringBuilder buff = new StringBuilder("RowMutation(");
        buff.append("keyspace='").append(table_).append('\'');
        buff.append(", key='").append(ByteBufferUtil.bytesToHex(key_)).append('\'');
        buff.append(", modifications=[");
        if (shallow)
        {
            List<String> cfnames = new ArrayList<String>();
            for (Integer cfid : modifications_.keySet())
            {
                CFMetaData cfm = NodeDescriptor.getCFMetaData(cfid);
                cfnames.add(cfm == null ? "-dropped-" : cfm.cfName);
            }
            buff.append(StringUtils.join(cfnames, ", "));
        }
        else
            buff.append(StringUtils.join(modifications_.values(), ", "));
        return buff.append("])").toString();
    }

    public void addColumnOrSuperColumn(String cfName, ColumnOrSuperColumn cosc)
    {
        if (cosc.super_column != null)
        {
            for (com.cep.messaging.impls.gossip.thrift.Column column : cosc.super_column.columns)
            {
                add(new QueryPath(cfName, cosc.super_column.name, column.name), column.value, column.timestamp, column.ttl);
            }
        }
        else if (cosc.column != null)
        {
            add(new QueryPath(cfName, null, cosc.column.name), cosc.column.value, cosc.column.timestamp, cosc.column.ttl);
        }
        else if (cosc.counter_super_column != null)
        {
            for (com.cep.messaging.impls.gossip.thrift.CounterColumn column : cosc.counter_super_column.columns)
            {
                addCounter(new QueryPath(cfName, cosc.counter_super_column.name, column.name), column.value);
            }
        }
        else // cosc.counter_column != null
        {
            addCounter(new QueryPath(cfName, null, cosc.counter_column.name), cosc.counter_column.value);
        }
    }

    public void deleteColumnOrSuperColumn(String cfName, Deletion del)
    {
        if (del.predicate != null && del.predicate.column_names != null)
        {
            for(ByteBuffer c : del.predicate.column_names)
            {
                if (del.super_column == null && NodeDescriptor.getColumnFamilyType(table_, cfName) == ColumnFamilyType.Super)
                    delete(new QueryPath(cfName, c), del.timestamp);
                else
                    delete(new QueryPath(cfName, del.super_column, c), del.timestamp);
            }
        }
        else
        {
            delete(new QueryPath(cfName, del.super_column), del.timestamp);
        }
    }

    public static RowMutation fromBytes(byte[] raw, int version) throws IOException
    {
        RowMutation rm = serializer_.deserialize(new DataInputStream(new ByteArrayInputStream(raw)), version);
        rm.preserializedBuffers.put(version, raw);
        return rm;
    }

    public RowMutation localCopy()
    {
        RowMutation rm = new RowMutation(table_, ByteBufferUtil.clone(key_));

        Table table = Table.open(table_);
        for (Map.Entry<Integer, ColumnFamily> entry : modifications_.entrySet())
        {
            ColumnFamily cf = entry.getValue().cloneMeShallow();
            ColumnFamilyStore cfs = table.getColumnFamilyStore(cf.id());
            for (Map.Entry<ByteBuffer, IColumn> ce : entry.getValue().getColumnsMap().entrySet())
                cf.addColumn(ce.getValue().localCopy(cfs));
            rm.modifications_.put(entry.getKey(), cf);
        }

        return rm;
    }

    public static class RowMutationSerializer implements ICompactSerializer<RowMutation>
    {
        public void serialize(RowMutation rm, DataOutputStream dos, int version) throws IOException
        {
            dos.writeUTF(rm.getTable());
            ByteBufferUtil.writeWithShortLength(rm.key(), dos);

            /* serialize the modifications_ in the mutation */
            int size = rm.modifications_.size();
            dos.writeInt(size);
            if (size > 0)
            {
                for (Map.Entry<Integer,ColumnFamily> entry : rm.modifications_.entrySet())
                {
                    dos.writeInt(entry.getKey());
                    ColumnFamily.serializer().serialize(entry.getValue(), dos);
                }
            }
        }

        public RowMutation deserialize(DataInputStream dis, int version, boolean fromRemote) throws IOException
        {
            String table = dis.readUTF();
            ByteBuffer key = ByteBufferUtil.readWithShortLength(dis);
            Map<Integer, ColumnFamily> modifications = new HashMap<Integer, ColumnFamily>();
            int size = dis.readInt();
            for (int i = 0; i < size; ++i)
            {
                Integer cfid = Integer.valueOf(dis.readInt());
                ColumnFamily cf = ColumnFamily.serializer().deserialize(dis, true, fromRemote);
                modifications.put(cfid, cf);
            }
            return new RowMutation(table, key, modifications);
        }

        public RowMutation deserialize(DataInputStream dis, int version) throws IOException
        {
            return deserialize(dis, version, true);
        }
    }
}
