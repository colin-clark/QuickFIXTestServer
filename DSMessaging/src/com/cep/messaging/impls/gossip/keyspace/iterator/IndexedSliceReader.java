package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IndexHelper;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.FileDataInput;
import com.cep.messaging.util.FileMark;
import com.cep.messaging.util.FileUtils;
import com.google.common.collect.AbstractIterator;

/**
 *  This is a reader that finds the block for a starting column and returns
 *  blocks before/after it for each next call. This function assumes that
 *  the CF is sorted by name and exploits the name index.
 */
class IndexedSliceReader extends AbstractIterator<IColumn> implements IColumnIterator
{
    private final ColumnFamily emptyColumnFamily;

    private final List<IndexHelper.IndexInfo> indexes;
    private final FileDataInput file;
    private final ByteBuffer startColumn;
    private final ByteBuffer finishColumn;
    private final boolean reversed;

    private BlockFetcher fetcher;
    private Deque<IColumn> blockColumns = new ArrayDeque<IColumn>();
    @SuppressWarnings("rawtypes")
	private AbstractType comparator;

    public IndexedSliceReader(SSTableReader sstable, FileDataInput input, ByteBuffer startColumn, ByteBuffer finishColumn, boolean reversed)
    {
        this.file = input;
        this.startColumn = startColumn;
        this.finishColumn = finishColumn;
        this.reversed = reversed;
        comparator = sstable.metadata.comparator;
        try
        {
            IndexHelper.skipBloomFilter(file);
            indexes = IndexHelper.deserializeIndex(file);

            emptyColumnFamily = ColumnFamily.serializer().deserializeFromSSTableNoColumns(ColumnFamily.create(sstable.metadata), file);
            fetcher = indexes == null ? new SimpleBlockFetcher() : new IndexedBlockFetcher();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    public ColumnFamily getColumnFamily()
    {
        return emptyColumnFamily;
    }

    @SuppressWarnings("rawtypes")
	public DecoratedKey getKey()
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
	private boolean isColumnNeeded(IColumn column)
    {
        if (startColumn.remaining() == 0 && finishColumn.remaining() == 0)
            return true;
        else if (startColumn.remaining() == 0 && !reversed)
            return comparator.compare(column.name(), finishColumn) <= 0;
        else if (startColumn.remaining() == 0 && reversed)
            return comparator.compare(column.name(), finishColumn) >= 0;
        else if (finishColumn.remaining() == 0 && !reversed)
            return comparator.compare(column.name(), startColumn) >= 0;
        else if (finishColumn.remaining() == 0 && reversed)
            return comparator.compare(column.name(), startColumn) <= 0;
        else if (!reversed)
            return comparator.compare(column.name(), startColumn) >= 0 && comparator.compare(column.name(), finishColumn) <= 0;
        else // if reversed
            return comparator.compare(column.name(), startColumn) <= 0 && comparator.compare(column.name(), finishColumn) >= 0;
    }

    protected IColumn computeNext()
    {
        while (true)
        {
            IColumn column = blockColumns.poll();
            if (column != null && isColumnNeeded(column))
                return column;
            try
            {
                if (column == null && !fetcher.getNextBlock())
                    return endOfData();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public void close()
    {
    }

    interface BlockFetcher
    {
        public boolean getNextBlock() throws IOException;
    }

    private class IndexedBlockFetcher implements BlockFetcher
    {
        private final FileMark mark;
        private int curRangeIndex;

        IndexedBlockFetcher() throws IOException
        {
            file.readInt(); // column count
            this.mark = file.mark();
            curRangeIndex = IndexHelper.indexFor(startColumn, indexes, comparator, reversed);
        }

        @SuppressWarnings("unchecked")
		public boolean getNextBlock() throws IOException
        {
            if (curRangeIndex < 0 || curRangeIndex >= indexes.size())
                return false;

            /* seek to the correct offset to the data, and calculate the data size */
            IndexHelper.IndexInfo curColPosition = indexes.get(curRangeIndex);

            /* see if this read is really necessary. */
            if (reversed)
            {
                if ((finishColumn.remaining() > 0 && comparator.compare(finishColumn, curColPosition.lastName) > 0) ||
                    (startColumn.remaining() > 0 && comparator.compare(startColumn, curColPosition.firstName) < 0))
                    return false;
            }
            else
            {
                if ((startColumn.remaining() > 0 && comparator.compare(startColumn, curColPosition.lastName) > 0) ||
                    (finishColumn.remaining() > 0 && comparator.compare(finishColumn, curColPosition.firstName) < 0))
                    return false;
            }

            boolean outOfBounds = false;
            file.reset(mark);
            FileUtils.skipBytesFully(file, curColPosition.offset);
            while (file.bytesPastMark(mark) < curColPosition.offset + curColPosition.width && !outOfBounds)
            {
                IColumn column = emptyColumnFamily.getColumnSerializer().deserialize(file);
                if (reversed)
                    blockColumns.addFirst(column);
                else
                    blockColumns.addLast(column);

                /* see if we can stop seeking. */
                if (!reversed && finishColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), finishColumn) >= 0;
                else if (reversed && startColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), startColumn) >= 0;
            }

            if (reversed)
                curRangeIndex--;
            else
                curRangeIndex++;
            return true;
        }
    }

    private class SimpleBlockFetcher implements BlockFetcher
    {
        @SuppressWarnings("unchecked")
		private SimpleBlockFetcher() throws IOException
        {
            int columns = file.readInt();
            for (int i = 0; i < columns; i++)
            {
                IColumn column = emptyColumnFamily.getColumnSerializer().deserialize(file);
                if (reversed)
                    blockColumns.addFirst(column);
                else
                    blockColumns.addLast(column);

                /* see if we can stop seeking. */
                boolean outOfBounds = false;
                if (!reversed && finishColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), finishColumn) >= 0;
                else if (reversed && startColumn.remaining() > 0)
                    outOfBounds = comparator.compare(column.name(), startColumn) >= 0;
                if (outOfBounds)
                    break;
            }
        }

        public boolean getNextBlock() throws IOException
        {
            return false;
        }
    }
}
