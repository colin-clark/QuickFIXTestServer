package com.cep.messaging.impls.gossip.keyspace;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.keyspace.iterator.IIterableColumns;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.marshal.MarshalException;
import com.cep.messaging.impls.gossip.serialization.ColumnFamilySerializer;
import com.cep.messaging.impls.gossip.serialization.IColumnSerializer;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

public class ColumnFamily implements IColumnContainer, IIterableColumns
{
	public static final int boolSize_ = 1;
	public static final int intSize_ = 4;
	public static final int longSize_ = 8;
	public static final int shortSize_ = 2;
	public static final int tsSize_ = 8;
	
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ColumnFamily.class);

    /* The column serializer for this Column Family. Create based on config. */
    private static ColumnFamilySerializer serializer = new ColumnFamilySerializer();
    private final CFMetaData cfm;

    public static ColumnFamilySerializer serializer()
    {
        return serializer;
    }

    public static ColumnFamily create(Integer cfId)
    {
        return create(NodeDescriptor.getCFMetaData(cfId));
    }

    public static ColumnFamily create(String tableName, String cfName)
    {
        return create(NodeDescriptor.getCFMetaData(tableName, cfName));
    }

    public static ColumnFamily create(CFMetaData cfm)
    {
        return new ColumnFamily(cfm);
    }

    private transient IColumnSerializer columnSerializer;
    public final AtomicLong markedForDeleteAt = new AtomicLong(Long.MIN_VALUE);
    public final AtomicInteger localDeletionTime = new AtomicInteger(Integer.MIN_VALUE);
    private ConcurrentSkipListMap<ByteBuffer, IColumn> columns;
    
    @SuppressWarnings("unchecked")
	public ColumnFamily(CFMetaData cfm)
    {
        assert cfm != null;
        this.cfm = cfm;
        columnSerializer = cfm.cfType == ColumnFamilyType.Standard ? Column.serializer() : SuperColumn.serializer(cfm.subcolumnComparator);
        columns = new ConcurrentSkipListMap<ByteBuffer, IColumn>(cfm.comparator);
     }
    
    public ColumnFamily cloneMeShallow()
    {
        ColumnFamily cf = new ColumnFamily(cfm);
        cf.markedForDeleteAt.set(markedForDeleteAt.get());
        cf.localDeletionTime.set(localDeletionTime.get());
        return cf;
    }

    @SuppressWarnings("rawtypes")
	public AbstractType getSubComparator()
    {
        return (columnSerializer instanceof SuperColumnSerializer) ? ((SuperColumnSerializer)columnSerializer).getComparator() : null;
    }

    public ColumnFamilyType getType()
    {
        return cfm.cfType;
    }

    public ColumnFamily cloneMe()
    {
        ColumnFamily cf = cloneMeShallow();
        cf.columns = columns.clone();
        return cf;
    }

    public Integer id()
    {
        return cfm.cfId;
    }

    /**
     * @return The CFMetaData for this row
     */
    public CFMetaData metadata()
    {
        return cfm;
    }

    /*
     *  We need to go through each column
     *  in the column family and resolve it before adding
    */
    public void addAll(ColumnFamily cf)
    {
        for (IColumn column : cf.getSortedColumns())
            addColumn(column);
        delete(cf);
    }

    public IColumnSerializer getColumnSerializer()
    {
        return columnSerializer;
    }

    public int getColumnCount()
    {
        return columns.size();
    }

    public boolean isEmpty()
    {
        return columns.isEmpty();
    }

    public boolean isSuper()
    {
        return getType() == ColumnFamilyType.Super;
    }

    public void addColumn(QueryPath path, ByteBuffer value, long timestamp)
    {
        addColumn(path, value, timestamp, 0);
    }

    public void addColumn(QueryPath path, ByteBuffer value, long timestamp, int timeToLive)
    {
        assert path.columnName != null : path;
        assert !metadata().getDefaultValidator().isCommutative();
        Column column;
        if (timeToLive > 0)
            column = new ExpiringColumn(path.columnName, value, timestamp, timeToLive);
        else
            column = new Column(path.columnName, value, timestamp);
        addColumn(path.superColumnName, column);
    }

    public void addCounter(QueryPath path, long value)
    {
        assert path.columnName != null : path;
        addColumn(path.superColumnName, new CounterUpdateColumn(path.columnName, value, System.currentTimeMillis()));
    }

    public void addTombstone(QueryPath path, ByteBuffer localDeletionTime, long timestamp)
    {
        assert path.columnName != null : path;
        addColumn(path.superColumnName, new DeletedColumn(path.columnName, localDeletionTime, timestamp));
    }

    public void addTombstone(QueryPath path, int localDeletionTime, long timestamp)
    {
        assert path.columnName != null : path;
        addColumn(path.superColumnName, new DeletedColumn(path.columnName, localDeletionTime, timestamp));
    }

    public void addTombstone(ByteBuffer name, int localDeletionTime, long timestamp)
    {
        addColumn(null, new DeletedColumn(name, localDeletionTime, timestamp));
    }

    public void addColumn(ByteBuffer superColumnName, Column column)
    {
        IColumn c;
        if (superColumnName == null)
        {
            c = column;
        }
        else
        {
            assert isSuper();
            c = new SuperColumn(superColumnName, getSubComparator());
            c.addColumn(column); // checks subcolumn name
        }
        addColumn(c);
    }

    public void clear()
    {
        columns.clear();
    }

    /*
     * If we find an old column that has the same name
     * the ask it to resolve itself else add the new column .
    */
    public void addColumn(IColumn column)
    {
        ByteBuffer name = column.name();
        IColumn oldColumn;
        while ((oldColumn = columns.putIfAbsent(name, column)) != null)
        {
            if (oldColumn instanceof SuperColumn)
            {
                ((SuperColumn) oldColumn).putColumn(column);
                break;  // Delegated to SuperColumn
            }
            else
            {
                // calculate reconciled col from old (existing) col and new col
                IColumn reconciledColumn = column.reconcile(oldColumn);
                if (columns.replace(name, oldColumn, reconciledColumn))
                    break;

                // We failed to replace column due to a concurrent update or a concurrent removal. Keep trying.
                // (Currently, concurrent removal should not happen (only updates), but let us support that anyway.)
            }
        }
    }

    public IColumn getColumn(ByteBuffer name)
    {
        return columns.get(name);
    }

    public SortedSet<ByteBuffer> getColumnNames()
    {
        return columns.keySet();
    }

    public Collection<IColumn> getSortedColumns()
    {
        return columns.values();
    }

    public Collection<IColumn> getReverseSortedColumns()
    {
        return columns.descendingMap().values();
    }

    public Map<ByteBuffer, IColumn> getColumnsMap()
    {
        return columns;
    }

    public void remove(ByteBuffer columnName)
    {
        columns.remove(columnName);
    }

    @Deprecated // TODO this is a hack to set initial value outside constructor
    public void delete(int localtime, long timestamp)
    {
        localDeletionTime.set(localtime);
        markedForDeleteAt.set(timestamp);
    }

    public void delete(ColumnFamily cf2)
    {
        GossipUtilities.atomicSetMax(localDeletionTime, cf2.getLocalDeletionTime()); // do this first so we won't have a column that's "deleted" but has no local deletion time
        GossipUtilities.atomicSetMax(markedForDeleteAt, cf2.getMarkedForDeleteAt());
    }

    public boolean isMarkedForDelete()
    {
        return markedForDeleteAt.get() > Long.MIN_VALUE;
    }

    /*
     * This function will calculate the difference between 2 column families.
     * The external input is assumed to be a superset of internal.
     */
    public ColumnFamily diff(ColumnFamily cfComposite)
    {
        assert cfComposite.id().equals(id());
        ColumnFamily cfDiff = new ColumnFamily(cfm);
        if (cfComposite.getMarkedForDeleteAt() > getMarkedForDeleteAt())
        {
            cfDiff.delete(cfComposite.getLocalDeletionTime(), cfComposite.getMarkedForDeleteAt());
        }

        // (don't need to worry about cfNew containing IColumns that are shadowed by
        // the delete tombstone, since cfNew was generated by CF.resolve, which
        // takes care of those for us.)
        Map<ByteBuffer, IColumn> columns = cfComposite.getColumnsMap();
        for (Map.Entry<ByteBuffer, IColumn> entry : columns.entrySet())
        {
            ByteBuffer cName = entry.getKey();
            IColumn columnInternal = this.columns.get(cName);
            IColumn columnExternal = entry.getValue();
            if (columnInternal == null)
            {
                cfDiff.addColumn(columnExternal);
            }
            else
            {
                IColumn columnDiff = columnInternal.diff(columnExternal);
                if (columnDiff != null)
                {
                    cfDiff.addColumn(columnDiff);
                }
            }
        }

        if (!cfDiff.getColumnsMap().isEmpty() || cfDiff.isMarkedForDelete())
            return cfDiff;
        return null;
    }

    @SuppressWarnings("rawtypes")
	public AbstractType getComparator()
    {
        return (AbstractType)columns.comparator();
    }

    int size()
    {
        int size = 0;
        for (IColumn column : columns.values())
        {
            size += column.size();
        }
        return size;
    }

    public int hashCode()
    {
        throw new RuntimeException("Not implemented.");
    }

    public boolean equals(Object o)
    {
        throw new RuntimeException("Not implemented.");
    }

    @SuppressWarnings("unchecked")
	public String toString()
    {
        StringBuilder sb = new StringBuilder("ColumnFamily(");
        CFMetaData cfm = metadata();
        sb.append(cfm == null ? "<anonymous>" : cfm.cfName);

        if (isMarkedForDelete())
            sb.append(" -deleted at ").append(getMarkedForDeleteAt()).append("-");

        sb.append(" [").append(getComparator().getColumnsString(getSortedColumns())).append("])");
        return sb.toString();
    }

    public static ByteBuffer digest(ColumnFamily cf)
    {
        MessageDigest digest = GossipUtilities.threadLocalMD5Digest();
        if (cf != null)
            cf.updateDigest(digest);
        return ByteBuffer.wrap(digest.digest());
    }

    public void updateDigest(MessageDigest digest)
    {
        for (IColumn column : columns.values())
            column.updateDigest(digest);
    }

    public long getMarkedForDeleteAt()
    {
        return markedForDeleteAt.get();
    }

    public int getLocalDeletionTime()
    {
        return localDeletionTime.get();
    }

    @SuppressWarnings("rawtypes")
	public static AbstractType getComparatorFor(String table, String columnFamilyName, ByteBuffer superColumnName)
    {
        return superColumnName == null
               ? NodeDescriptor.getComparator(table, columnFamilyName)
               : NodeDescriptor.getSubComparator(table, columnFamilyName);
    }

    public static ColumnFamily diff(ColumnFamily cf1, ColumnFamily cf2)
    {
        if (cf1 == null)
            return cf2;
        return cf1.diff(cf2);
    }

    public void resolve(ColumnFamily cf)
    {
        // Row _does_ allow null CF objects :(  seems a necessary evil for efficiency
        if (cf == null)
            return;
        addAll(cf);
    }

    public int getEstimatedColumnCount()
    {
        return getColumnCount();
    }

    public Iterator<IColumn> iterator()
    {
        return columns.values().iterator();
    }

    public long serializedSize()
    {
        int size = boolSize_ // bool
                 + intSize_ // id
                 + intSize_ // local deletion time
                 + longSize_ // client deltion time
                 + intSize_; // column count
        for (IColumn column : columns.values())
            size += column.serializedSize();
        return size;
    }

    /**
     * Goes over all columns and check the fields are valid (as far as we can
     * tell).
     * This is used to detect corruption after deserialization.
     */
    public void validateColumnFields() throws MarshalException
    {
        CFMetaData metadata = metadata();
        for (IColumn column : getSortedColumns())
        {
            column.validateFields(metadata);
        }
    }
}
