package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.sstable.Descriptor;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTable;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableReader;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.util.BufferedRandomAccessFile;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.google.common.collect.AbstractIterator;

@SuppressWarnings("rawtypes")
public class KeyIterator extends AbstractIterator<DecoratedKey> implements CloseableIterator<DecoratedKey>
{
    private final BufferedRandomAccessFile in;
    private final Descriptor desc;

    public KeyIterator(Descriptor desc)
    {
        this.desc = desc;
        try
        {
            in = new BufferedRandomAccessFile(new File(desc.filenameFor(SSTable.COMPONENT_INDEX)),
                                              "r",
                                              BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE,
                                              true);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    protected DecoratedKey computeNext()
    {
        try
        {
            if (in.isEOF())
                return endOfData();
            DecoratedKey key = SSTableReader.decodeKey(StorageService.getPartitioner(), desc, ByteBufferUtil.readWithShortLength(in));
            in.readLong(); // skip data position
            return key;
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }

    public void close() throws IOException
    {
        in.close();
    }

    public long getBytesRead()
    {
        return in.getFilePointer();
    }

    public long getTotalBytes()
    {
        try
        {
            return in.length();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
}
