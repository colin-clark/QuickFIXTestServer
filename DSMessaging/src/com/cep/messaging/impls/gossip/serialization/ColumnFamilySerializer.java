package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.ColumnIndexer;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.util.exception.UnserializableColumnFamilyException;

public class ColumnFamilySerializer implements ICompactSerializer3<ColumnFamily>
{
    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ColumnFamilySerializer.class);

    /*
     * Serialized ColumnFamily format:
     *
     * [serialized for intra-node writes only, e.g. returning a query result]
     * <cf nullability boolean: false if the cf is null>
     * <cf id>
     *
     * [in sstable only]
     * <column bloom filter>
     * <sparse column index, start/finish columns every ColumnIndexSizeInKB of data>
     *
     * [always present]
     * <local deletion time>
     * <client-provided deletion time>
     * <column count>
     * <columns, serialized individually>
    */
    public void serialize(ColumnFamily columnFamily, DataOutput dos)
    {
        try
        {
            if (columnFamily == null)
            {
                dos.writeBoolean(false);
                return;
            }

            dos.writeBoolean(true);
            dos.writeInt(columnFamily.id());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        serializeForSSTable(columnFamily, dos);
    }

    public int serializeForSSTable(ColumnFamily columnFamily, DataOutput dos)
    {
        try
        {
            serializeCFInfo(columnFamily, dos);

            Collection<IColumn> columns = columnFamily.getSortedColumns();
            int count = columns.size();
            dos.writeInt(count);
            for (IColumn column : columns)
            {
                columnFamily.getColumnSerializer().serialize(column, dos);
            }
            return count;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void serializeCFInfo(ColumnFamily columnFamily, DataOutput dos) throws IOException
    {
        dos.writeInt(columnFamily.localDeletionTime.get());
        dos.writeLong(columnFamily.markedForDeleteAt.get());
    }

    public int serializeWithIndexes(ColumnFamily columnFamily, DataOutput dos)
    {
        ColumnIndexer.serialize(columnFamily, dos);
        return serializeForSSTable(columnFamily, dos);
    }

    public ColumnFamily deserialize(DataInput dis) throws IOException
    {
        return deserialize(dis, false, false);
    }

    public ColumnFamily deserialize(DataInput dis, boolean intern, boolean fromRemote) throws IOException
    {
        if (!dis.readBoolean())
            return null;

        // create a ColumnFamily based on the cf id
        int cfId = dis.readInt();
        if (CFMetaData.getCF(cfId) == null)
            throw new UnserializableColumnFamilyException("Couldn't find cfId=" + cfId, cfId);
        ColumnFamily cf = ColumnFamily.create(cfId);
        deserializeFromSSTableNoColumns(cf, dis);
        deserializeColumns(dis, cf, intern, fromRemote);
        return cf;
    }

    public void deserializeColumns(DataInput dis, ColumnFamily cf, boolean intern, boolean fromRemote) throws IOException
    {
        int size = dis.readInt();
        ColumnFamilyStore interner = intern ? Table.open(CFMetaData.getCF(cf.id()).left).getColumnFamilyStore(cf.id()) : null;
        for (int i = 0; i < size; ++i)
        {
            IColumn column = cf.getColumnSerializer().deserialize(dis, interner, fromRemote, (int) (System.currentTimeMillis() / 1000));
            cf.addColumn(column);
        }
    }

    @SuppressWarnings("deprecation")
	public ColumnFamily deserializeFromSSTableNoColumns(ColumnFamily cf, DataInput input) throws IOException
    {
        cf.delete(input.readInt(), input.readLong());
        return cf;
    }

    public long serializedSize(ColumnFamily cf)
    {
        return cf.serializedSize();
    }
}
