package com.cep.messaging.impls.gossip.keyspace.sstable;

import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IndexHelper;
import com.cep.messaging.impls.gossip.keyspace.iterator.IColumnIterator;
import com.cep.messaging.impls.gossip.keyspace.marshal.MarshalException;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.util.BufferedRandomAccessFile;

@SuppressWarnings("rawtypes")
public class SSTableIdentityIterator implements Comparable<SSTableIdentityIterator>, IColumnIterator
{
    private static final Logger logger = LoggerFactory.getLogger(SSTableIdentityIterator.class);

	private final DecoratedKey key;
    private final long finishedAt;
    private final BufferedRandomAccessFile file;
    private final long dataStart;
    public final long dataSize;
    public final boolean fromRemote;

    private final ColumnFamily columnFamily;
    public final int columnCount;
    private final long columnPosition;

    // Used by lazilyCompactedRow, so that we see the same things when deserializing the first and second time
    private final int expireBefore;

    private final boolean validateColumns;

    /**
     * Used to iterate through the columns of a row.
     * @param sstable SSTable we are reading ffrom.
     * @param file Reading using this file.
     * @param key Key of this row.
     * @param dataStart Data for this row starts at this pos.
     * @param dataSize length of row data
     * @throws IOException
     */
    public SSTableIdentityIterator(SSTableReader sstable, BufferedRandomAccessFile file, DecoratedKey key, long dataStart, long dataSize)
    throws IOException
    {
        this(sstable, file, key, dataStart, dataSize, false);
    }

    /**
     * Used to iterate through the columns of a row.
     * @param sstable SSTable we are reading ffrom.
     * @param file Reading using this file.
     * @param key Key of this row.
     * @param dataStart Data for this row starts at this pos.
     * @param dataSize length of row data
     * @param checkData if true, do its best to deserialize and check the coherence of row data
     * @throws IOException
     */
    public SSTableIdentityIterator(SSTableReader sstable, BufferedRandomAccessFile file, DecoratedKey key, long dataStart, long dataSize, boolean checkData)
    throws IOException
    {
        this(sstable.metadata, file, key, dataStart, dataSize, checkData, sstable, false);
    }

    public SSTableIdentityIterator(CFMetaData metadata, BufferedRandomAccessFile file, DecoratedKey key, long dataStart, long dataSize, boolean fromRemote)
    throws IOException
    {
        this(metadata, file, key, dataStart, dataSize, false, null, fromRemote);
    }

    // sstable may be null *if* deserializeRowHeader is false
    private SSTableIdentityIterator(CFMetaData metadata, BufferedRandomAccessFile file, DecoratedKey key, long dataStart, long dataSize, boolean checkData, SSTableReader sstable, boolean fromRemote)
    throws IOException
    {
        this.file = file;
        this.key = key;
        this.dataStart = dataStart;
        this.dataSize = dataSize;
        this.expireBefore = (int)(System.currentTimeMillis() / 1000);
        this.fromRemote = fromRemote;
        this.validateColumns = checkData;
        finishedAt = dataStart + dataSize;

        try
        {
            file.seek(this.dataStart);
            if (checkData)
            {
                try
                {
                    IndexHelper.defreezeBloomFilter(file, dataSize, sstable.descriptor.usesOldBloomFilter);
                }
                catch (Exception e)
                {
                    if (e instanceof EOFException)
                        throw (EOFException) e;

                    logger.debug("Invalid bloom filter in {}; will rebuild it", sstable);
                    // deFreeze should have left the file position ready to deserialize index
                }
                try
                {
                    IndexHelper.deserializeIndex(file);
                }
                catch (Exception e)
                {
                    logger.debug("Invalid row summary in {}; will rebuild it", sstable);
                }
                file.seek(this.dataStart);
            }

            IndexHelper.skipBloomFilter(file);
            IndexHelper.skipIndex(file);
            columnFamily = ColumnFamily.create(metadata);
            ColumnFamily.serializer().deserializeFromSSTableNoColumns(columnFamily, file);
            columnCount = file.readInt();
            columnPosition = file.getFilePointer();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    public DecoratedKey getKey()
    {
        return key;
    }

    public ColumnFamily getColumnFamily()
    {
        return columnFamily;
    }

    public boolean hasNext()
    {
        return file.getFilePointer() < finishedAt;
    }

    public IColumn next()
    {
        try
        {
            IColumn column = columnFamily.getColumnSerializer().deserialize(file, null, fromRemote, expireBefore);
            if (validateColumns)
                column.validateFields(columnFamily.metadata());
            return column;
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        catch (MarshalException e)
        {
            throw new IOError(new IOException("Error validating row " + key, e));
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException
    {
        // creator is responsible for closing file when finished
    }

    public String getPath()
    {
        return file.getPath();
    }

    public void echoData(DataOutput out) throws IOException
    {
        file.seek(dataStart);
        while (file.getFilePointer() < finishedAt)
        {
            out.write(file.readByte());
        }
    }

    public ColumnFamily getColumnFamilyWithColumns() throws IOException
    {
        file.seek(columnPosition - 4); // seek to before column count int
        ColumnFamily cf = columnFamily.cloneMeShallow();
        ColumnFamily.serializer().deserializeColumns(file, cf, false, fromRemote);
        if (validateColumns)
        {
            try
            {
                cf.validateColumnFields();
            }
            catch (MarshalException e)
            {
                throw new IOException("Error validating row " + key, e);
            }
        }
        return cf;
    }

    public int compareTo(SSTableIdentityIterator o)
    {
        return key.compareTo(o.key);
    }

    public void reset()
    {
        try
        {
            file.seek(columnPosition);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
}
