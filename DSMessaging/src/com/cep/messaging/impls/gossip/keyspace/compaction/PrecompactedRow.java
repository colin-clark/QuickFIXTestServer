package com.cep.messaging.impls.gossip.keyspace.compaction;

import java.io.DataOutput;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.CounterColumn;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTableIdentityIterator;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.DataOutputBuffer;

/**
 * PrecompactedRow merges its rows in its constructor in memory.
 */
public class PrecompactedRow extends AbstractCompactedRow
{
    private static Logger logger = LoggerFactory.getLogger(PrecompactedRow.class);

    private final ColumnFamily compactedCf;
    private final int gcBefore;

    // For testing purposes
    @SuppressWarnings("rawtypes")
	public PrecompactedRow(DecoratedKey key, ColumnFamily compacted)
    {
        super(key);
        this.compactedCf = compacted;
        this.gcBefore = Integer.MAX_VALUE;
    }

    public PrecompactedRow(CompactionController controller, List<SSTableIdentityIterator> rows)
    {
        super(rows.get(0).getKey());
        this.gcBefore = controller.gcBefore;

        ColumnFamily cf = null;
        for (SSTableIdentityIterator row : rows)
        {
            ColumnFamily thisCF;
            try
            {
                thisCF = row.getColumnFamilyWithColumns();
            }
            catch (IOException e)
            {
                logger.error("Skipping row " + key + " in " + row.getPath(), e);
                continue;
            }
            if (cf == null)
            {
                cf = thisCF;
            }
            else
            {
                cf.addAll(thisCF);
            }
        }
        compactedCf = controller.shouldPurge(key) ? ColumnFamilyStore.removeDeleted(cf, controller.gcBefore) : cf;
        if (compactedCf != null && compactedCf.metadata().getDefaultValidator().isCommutative())
        {
            CounterColumn.removeOldShards(compactedCf, controller.gcBefore);
        }
    }

    public void write(DataOutput out) throws IOException
    {
        if (compactedCf != null)
        {
            DataOutputBuffer buffer = new DataOutputBuffer();
            DataOutputBuffer headerBuffer = new DataOutputBuffer();
            //ColumnIndexer.serialize(compactedCf, headerBuffer);
            ColumnFamily.serializer().serializeForSSTable(compactedCf, buffer);
            out.writeLong(headerBuffer.getLength() + buffer.getLength());
            out.write(headerBuffer.getData(), 0, headerBuffer.getLength());
            out.write(buffer.getData(), 0, buffer.getLength());
        }
    }

    public void update(MessageDigest digest)
    {
        if (compactedCf != null)
        {
            DataOutputBuffer buffer = new DataOutputBuffer();
            try
            {
                ColumnFamily.serializer().serializeCFInfo(compactedCf, buffer);
                buffer.writeInt(compactedCf.getColumnCount());
                digest.update(buffer.getData(), 0, buffer.getLength());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            compactedCf.updateDigest(digest);
        }
    }

    public boolean isEmpty()
    {
        return compactedCf == null || ColumnFamilyStore.removeDeletedCF(compactedCf, gcBefore) == null;
    }

    public int columnCount()
    {
        return compactedCf == null ? 0 : compactedCf.getColumnCount();
    }

    /**
     * @return the full column family represented by this compacted row.
     *
     * We do not provide this method for other AbstractCompactedRow, because this fits the whole row into
     * memory and don't make sense for those other implementations.
     */
    public ColumnFamily getFullColumnFamily()  throws IOException
    {
        return compactedCf;
    }
}
