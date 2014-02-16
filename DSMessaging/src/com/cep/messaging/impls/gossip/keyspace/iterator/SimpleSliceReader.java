package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IndexHelper;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.FileDataInput;
import com.cep.messaging.util.FileMark;
import com.google.common.collect.AbstractIterator;

class SimpleSliceReader extends AbstractIterator<IColumn> implements IColumnIterator
{
    private final FileDataInput file;
    private final ByteBuffer finishColumn;
    @SuppressWarnings("rawtypes")
	private final AbstractType comparator;
    private final ColumnFamily emptyColumnFamily;
    private final int columns;
    private int i;
    private FileMark mark;

    public SimpleSliceReader(SSTableReader sstable, FileDataInput input, ByteBuffer finishColumn)
    {
        this.file = input;
        this.finishColumn = finishColumn;
        comparator = sstable.metadata.comparator;
        try
        {
            IndexHelper.skipBloomFilter(file);
            IndexHelper.skipIndex(file);

            emptyColumnFamily = ColumnFamily.serializer().deserializeFromSSTableNoColumns(ColumnFamily.create(sstable.metadata), file);
            columns = file.readInt();
            mark = file.mark();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    @SuppressWarnings("unchecked")
	protected IColumn computeNext()
    {
        if (i++ >= columns)
            return endOfData();

        IColumn column;
        try
        {
            file.reset(mark);
            column = emptyColumnFamily.getColumnSerializer().deserialize(file);
        }
        catch (IOException e)
        {
            throw new RuntimeException("error reading " + i + " of " + columns, e);
        }
        if (finishColumn.remaining() > 0 && comparator.compare(column.name(), finishColumn) > 0)
            return endOfData();

        mark = file.mark();
        return column;
    }

    public ColumnFamily getColumnFamily()
    {
        return emptyColumnFamily;
    }

    public void close() throws IOException
    {
    }

    @SuppressWarnings("rawtypes")
	public DecoratedKey getKey()
    {
        throw new UnsupportedOperationException();
    }
}
