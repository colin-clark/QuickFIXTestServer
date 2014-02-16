package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IndexHelper;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.serialization.ColumnFamilySerializer;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.util.FileDataInput;
import com.cep.messaging.util.FileMark;
import com.cep.messaging.util.FileUtils;
import com.cep.messaging.util.Filter;

public class SSTableNamesIterator extends SimpleAbstractColumnIterator implements IColumnIterator
{
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(SSTableNamesIterator.class);

    private ColumnFamily cf;
    private Iterator<IColumn> iter;
    public final SortedSet<ByteBuffer> columns;
    @SuppressWarnings("rawtypes")
	public final DecoratedKey key;

    @SuppressWarnings("rawtypes")
	public SSTableNamesIterator(SSTableReader sstable, DecoratedKey key, SortedSet<ByteBuffer> columns)
    {
        assert columns != null;
        this.columns = columns;
        this.key = key;

        FileDataInput file = sstable.getFileDataInput(key, NodeDescriptor.getIndexedReadBufferSizeInKB() * 1024);
        if (file == null)
            return;

        try
        {
            DecoratedKey keyInDisk = SSTableReader.decodeKey(sstable.partitioner,
                                                             sstable.descriptor,
                                                             ByteBufferUtil.readWithShortLength(file));
            assert keyInDisk.equals(key) : String.format("%s != %s in %s", keyInDisk, key, file.getPath());
            SSTableReader.readRowSize(file, sstable.descriptor);
            read(sstable, file);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        finally
        {
            FileUtils.closeQuietly(file);
        }
    }

    @SuppressWarnings("rawtypes")
	public SSTableNamesIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key, SortedSet<ByteBuffer> columns)
    {
        assert columns != null;
        this.columns = columns;
        this.key = key;

        try
        {
            read(sstable, file);
        }
        catch (IOException ioe)
        {
            throw new IOError(ioe);
        }
    }

    private void read(SSTableReader sstable, FileDataInput file)
    throws IOException
    {
        Filter bf = IndexHelper.defreezeBloomFilter(file, sstable.descriptor.usesOldBloomFilter);
        List<IndexHelper.IndexInfo> indexList = IndexHelper.deserializeIndex(file);

        // we can stop early if bloom filter says none of the columns actually exist -- but,
        // we can't stop before initializing the cf above, in case there's a relevant tombstone
        ColumnFamilySerializer serializer = ColumnFamily.serializer();
        try {
            cf = serializer.deserializeFromSSTableNoColumns(ColumnFamily.create(sstable.metadata), file);
        } catch (Exception e) {
            throw new IOException
                (serializer + " failed to deserialize " + sstable.getColumnFamilyName() + " with " + sstable.metadata + " from " + file, e);
        }

        List<ByteBuffer> filteredColumnNames = new ArrayList<ByteBuffer>(columns.size());
        for (ByteBuffer name : columns)
        {
            if (bf.isPresent(name))
            {
                filteredColumnNames.add(name);
            }
        }
        if (filteredColumnNames.isEmpty())
            return;

        if (indexList == null)
            readSimpleColumns(file, columns, filteredColumnNames);
        else
            readIndexedColumns(sstable.metadata, file, columns, filteredColumnNames, indexList);

        // create an iterator view of the columns we read
        iter = cf.getSortedColumns().iterator();
    }

    private void readSimpleColumns(FileDataInput file, SortedSet<ByteBuffer> columnNames, List<ByteBuffer> filteredColumnNames) throws IOException
    {
        int columns = file.readInt();
        int n = 0;
        for (int i = 0; i < columns; i++)
        {
            IColumn column = cf.getColumnSerializer().deserialize(file);
            if (columnNames.contains(column.name()))
            {
                cf.addColumn(column);
                if (n++ > filteredColumnNames.size())
                    break;
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void readIndexedColumns(CFMetaData metadata, FileDataInput file, SortedSet<ByteBuffer> columnNames, List<ByteBuffer> filteredColumnNames, List<IndexHelper.IndexInfo> indexList)
    throws IOException
    {
        file.readInt(); // column count

        /* get the various column ranges we have to read */
        AbstractType comparator = metadata.comparator;
        SortedSet<IndexHelper.IndexInfo> ranges = new TreeSet<IndexHelper.IndexInfo>(IndexHelper.getComparator(comparator, false));
        for (ByteBuffer name : filteredColumnNames)
        {
            int index = IndexHelper.indexFor(name, indexList, comparator, false);
            if (index == indexList.size())
                continue;
            IndexHelper.IndexInfo indexInfo = indexList.get(index);
            if (comparator.compare(name, indexInfo.firstName) < 0)
                continue;
            ranges.add(indexInfo);
        }

        FileMark mark = file.mark();
        for (IndexHelper.IndexInfo indexInfo : ranges)
        {
            file.reset(mark);
            FileUtils.skipBytesFully(file, indexInfo.offset);
            // TODO only completely deserialize columns we are interested in
            while (file.bytesPastMark(mark) < indexInfo.offset + indexInfo.width)
            {
                IColumn column = cf.getColumnSerializer().deserialize(file);
                // we check vs the original Set, not the filtered List, for efficiency
                if (columnNames.contains(column.name()))
                {
                    cf.addColumn(column);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
	public DecoratedKey getKey()
    {
        return key;
    }

    public ColumnFamily getColumnFamily()
    {
        return cf;
    }

    protected IColumn computeNext()
    {
        if (iter == null || !iter.hasNext())
            return endOfData();
        return iter.next();
    }
}
