package com.cep.messaging.impls.gossip.keyspace;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.util.BloomFilter;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.LegacyBloomFilter;
import com.cep.messaging.util.FileDataInput;
import com.cep.messaging.util.FileMark;
import com.cep.messaging.util.FileUtils;
import com.cep.messaging.util.Filter;

/**
 * Provides helper to serialize, deserialize and use column indexes.
 */
public class IndexHelper
{

    /**
     * Skip the bloom filter
     * @param in the data input from which the bloom filter should be skipped
     * @throws IOException
     */
    public static void skipBloomFilter(FileDataInput in) throws IOException
    {
        /* size of the bloom filter */
        int size = in.readInt();
        /* skip the serialized bloom filter */
        FileUtils.skipBytesFully(in, size);
    }

	/**
	 * Skip the index
	 * @param file the data input from which the index should be skipped
	 * @throws IOException if an I/O error occurs.
	 */
	public static void skipIndex(FileDataInput file) throws IOException
	{
        /* read only the column index list */
        int columnIndexSize = file.readInt();
        /* skip the column index data */
        FileUtils.skipBytesFully(file, columnIndexSize);
	}
    
    /**
     * Deserialize the index into a structure and return it
     *
     * @param in - input source
     *
     * @return ArrayList<IndexInfo> - list of de-serialized indexes
     * @throws IOException if an I/O error occurs.
     */
	public static ArrayList<IndexInfo> deserializeIndex(FileDataInput in) throws IOException
	{
		int columnIndexSize = in.readInt();
        if (columnIndexSize == 0)
            return null;
        ArrayList<IndexInfo> indexList = new ArrayList<IndexInfo>();
        FileMark mark = in.mark();
        while (in.bytesPastMark(mark) < columnIndexSize)
        {
            indexList.add(IndexInfo.deserialize(in));
        }
        assert in.bytesPastMark(mark) == columnIndexSize;

        return indexList;
	}

    public static Filter defreezeBloomFilter(FileDataInput file, boolean usesOldBloomFilter) throws IOException
    {
        return defreezeBloomFilter(file, Integer.MAX_VALUE, usesOldBloomFilter);
    }

    /**
     * De-freeze the bloom filter.
     *
     * @param file - source file
     * @param maxSize - sanity check: if filter claimes to be larger than this it is bogus
     * @param useOldBuffer - do we need to reuse old buffer?
     *
     * @return bloom filter summarizing the column information
     * @throws java.io.IOException if an I/O error occurs.
     * Guarantees that file's current position will be just after the bloom filter, even if
     * the filter cannot be deserialized, UNLESS EOFException is thrown.
     */
    public static Filter defreezeBloomFilter(FileDataInput file, long maxSize, boolean useOldBuffer) throws IOException
    {
        int size = file.readInt();
        if (size > maxSize || size <= 0)
            throw new EOFException("bloom filter claims to be " + size + " bytes, longer than entire row size " + maxSize);
        ByteBuffer bytes = file.readBytes(size);

        DataInputStream stream = new DataInputStream(ByteBufferUtil.inputStream(bytes));
        return useOldBuffer
                ? LegacyBloomFilter.serializer().deserialize(stream, 0) // version means nothing there.
                : BloomFilter.serializer().deserialize(stream);
    }

    /**
     * The index of the IndexInfo in which a scan starting with @name should begin.
     *
     * @param name
     *         name of the index
     *
     * @param indexList
     *          list of the indexInfo objects
     *
     * @param comparator
     *          comparator type
     *
     * @param reversed
     *          is name reversed
     *
     * @return int index
     */
    @SuppressWarnings("rawtypes")
	public static int indexFor(ByteBuffer name, List<IndexInfo> indexList, AbstractType comparator, boolean reversed)
    {
        if (name.remaining() == 0 && reversed)
            return indexList.size() - 1;
        IndexInfo target = new IndexInfo(name, name, 0, 0);
        /*
        Take the example from the unit test, and say your index looks like this:
        [0..5][10..15][20..25]
        and you look for the slice [13..17].

        When doing forward slice, we we doing a binary search comparing 13 (the start of the query)
        to the lastName part of the index slot. You'll end up with the "first" slot, going from left to right,
        that may contain the start.

        When doing a reverse slice, we do the same thing, only using as a start column the end of the query,
        i.e. 17 in this example, compared to the firstName part of the index slots.  bsearch will give us the
        first slot where firstName > start ([20..25] here), so we subtract an extra one to get the slot just before.
        */
        int index = Collections.binarySearch(indexList, target, getComparator(comparator, reversed));
        return index < 0 ? -index - (reversed ? 2 : 1) : index;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Comparator<IndexInfo> getComparator(final AbstractType nameComparator, boolean reversed)
    {
        return reversed ? nameComparator.indexReverseComparator : nameComparator.indexComparator;
    }

    public static class IndexInfo
    {
        public final long width;
        public final ByteBuffer lastName;
        public final ByteBuffer firstName;
        public final long offset;

        public IndexInfo(ByteBuffer firstName, ByteBuffer lastName, long offset, long width)
        {
            this.firstName = firstName;
            this.lastName = lastName;
            this.offset = offset;
            this.width = width;
        }

        public void serialize(DataOutput dos) throws IOException
        {
            ByteBufferUtil.writeWithShortLength(firstName, dos);
            ByteBufferUtil.writeWithShortLength(lastName, dos);
            dos.writeLong(offset);
            dos.writeLong(width);
        }

        public int serializedSize()
        {
            return 2 + firstName.remaining() + 2 + lastName.remaining() + 8 + 8;
        }

        public static IndexInfo deserialize(FileDataInput dis) throws IOException
        {
            return new IndexInfo(ByteBufferUtil.readWithShortLength(dis), ByteBufferUtil.readWithShortLength(dis), dis.readLong(), dis.readLong());
        }
    }
}
