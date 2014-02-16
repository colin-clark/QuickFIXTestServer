package com.cep.messaging.impls.gossip.util;

import java.io.DataInput;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.serialization.ColumnSerializer;

/**
 * Facade over a DataInput that contains IColumns in sorted order.
 * We use this because passing a SortedMap to the ConcurrentSkipListMap constructor is the only way
 * to invoke its private buildFromSorted method and avoid worst-case behavior of CSLM.put.
 */
public class ColumnSortedMap implements SortedMap<ByteBuffer, IColumn>
{
    private final ColumnSerializer serializer;
    private final DataInput dis;
    private final Comparator<ByteBuffer> comparator;
    private final int length;
    private final ColumnFamilyStore interner;
    private final boolean fromRemote;
    private final int expireBefore;

    public ColumnSortedMap(Comparator<ByteBuffer> comparator, ColumnSerializer serializer, DataInput dis, ColumnFamilyStore interner, int length, boolean fromRemote, int expireBefore)
    {
        this.comparator = comparator;
        this.serializer = serializer;
        this.interner = interner;
        this.dis = dis;
        this.length = length;
        this.fromRemote = fromRemote;
        this.expireBefore = expireBefore;
    }

    public int size()
    {
        return length;
    }

    public boolean isEmpty()
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsValue(Object value)
    {
        throw new UnsupportedOperationException();
    }

    public IColumn get(Object key)
    {
        throw new UnsupportedOperationException();
    }

    public IColumn put(ByteBuffer key, IColumn value)
    {
        throw new UnsupportedOperationException();
    }

    public IColumn remove(Object key)
    {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends ByteBuffer, ? extends IColumn> m)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {

    }

    public Comparator<? super ByteBuffer> comparator()
    {
        return comparator;
    }

    public SortedMap<ByteBuffer, IColumn> subMap(ByteBuffer fromKey, ByteBuffer toKey)
    {
        throw new UnsupportedOperationException();
    }

    public SortedMap<ByteBuffer, IColumn> headMap(ByteBuffer toKey)
    {
        throw new UnsupportedOperationException();
    }

    public SortedMap<ByteBuffer, IColumn> tailMap(ByteBuffer fromKey)
    {
        throw new UnsupportedOperationException();
    }

    public ByteBuffer firstKey()
    {
        throw new UnsupportedOperationException();
    }

    public ByteBuffer lastKey()
    {
        throw new UnsupportedOperationException();
    }

    public Set<ByteBuffer> keySet()
    {
        throw new UnsupportedOperationException();
    }

    public Collection<IColumn> values()
    {
        throw new UnsupportedOperationException();
    }

    public Set<Map.Entry<ByteBuffer, IColumn>> entrySet()
    {
        return new ColumnSet(serializer, dis, interner, length, fromRemote, expireBefore);
    }
}

class ColumnSet implements Set<Map.Entry<ByteBuffer, IColumn>>
{
    private final ColumnSerializer serializer;
    private final DataInput dis;
    private final int length;
    private final ColumnFamilyStore interner;
    private boolean fromRemote;
    private final int expireBefore;

    public ColumnSet(ColumnSerializer serializer, DataInput dis, ColumnFamilyStore interner, int length, boolean fromRemote, int expireBefore)
    {
        this.serializer = serializer;
        this.dis = dis;
        this.interner = interner;
        this.length = length;
        this.fromRemote = fromRemote;
        this.expireBefore = expireBefore;
    }

    public int size()
    {
        return length;
    }

    public boolean isEmpty()
    {
        throw new UnsupportedOperationException();
    }

    public boolean contains(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<Entry<ByteBuffer, IColumn>> iterator()
    {
        return new ColumnIterator(serializer, dis, interner, length, fromRemote, expireBefore);
    }

    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(T[] a)
    {
        throw new UnsupportedOperationException();
    }

    public boolean add(Entry<ByteBuffer, IColumn> e)
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends Entry<ByteBuffer, IColumn>> c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
    }
}

class ColumnIterator implements Iterator<Map.Entry<ByteBuffer, IColumn>>
{
    private final ColumnSerializer serializer;
    private final DataInput dis;
    private final int length;
    private final boolean fromRemote;
    private int count = 0;
    private ColumnFamilyStore interner;
    private final int expireBefore;

    public ColumnIterator(ColumnSerializer serializer, DataInput dis, ColumnFamilyStore interner, int length, boolean fromRemote, int expireBefore)
    {
        this.dis = dis;
        this.serializer = serializer;
        this.interner = interner;
        this.length = length;
        this.fromRemote = fromRemote;
        this.expireBefore = expireBefore;
    }

    private IColumn deserializeNext()
    {
        try
        {
            count++;
            return serializer.deserialize(dis, interner, fromRemote, expireBefore);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    public boolean hasNext()
    {
        return count < length;
    }

    public Entry<ByteBuffer, IColumn> next()
    {
        if (!hasNext())
        {
            throw new IllegalStateException("end of column iterator");
        }

        final IColumn column = deserializeNext();
        return new Entry<ByteBuffer, IColumn>()
        {
            public IColumn setValue(IColumn value)
            {
                throw new UnsupportedOperationException();
            }

            public IColumn getValue()
            {
                return column;
            }

            public ByteBuffer getKey()
            {
                return column.name();
            }
        };
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
