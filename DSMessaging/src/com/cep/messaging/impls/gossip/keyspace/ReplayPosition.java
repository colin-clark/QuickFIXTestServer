package com.cep.messaging.impls.gossip.keyspace;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Comparator;

import com.cep.messaging.impls.gossip.keyspace.sstable.SSTable;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer2;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

public class ReplayPosition implements Comparable<ReplayPosition>
{
    public static final ReplayPositionSerializer serializer = new ReplayPositionSerializer();

    // NONE is used for SSTables that are streamed from other nodes and thus have no relationship
    // with our local commitlog. The values satisfy the critera that
    //  - no real commitlog segment will have the given id
    //  - it will sort before any real replayposition, so it will be effectively ignored by getReplayPosition
    public static final ReplayPosition NONE = new ReplayPosition(-1, 0);

    /**
     * Convenience method to compute the replay position for a group of SSTables.
     * @param sstables
     * @return the most recent (highest) replay position
     */
    public static ReplayPosition getReplayPosition(Iterable<? extends SSTable> sstables)
    {
        if (Iterables.isEmpty(sstables))
            return NONE;

        Function<SSTable, ReplayPosition> f = new Function<SSTable, ReplayPosition>()
        {
            public ReplayPosition apply(SSTable sstable)
            {
                return sstable.replayPosition;
            }
        };
        Ordering<ReplayPosition> ordering = Ordering.from(ReplayPosition.comparator);
        return ordering.max(Iterables.transform(sstables, f));
    }


    public final long segment;
    public final int position;

    public static final Comparator<ReplayPosition> comparator = new Comparator<ReplayPosition>()
    {
        public int compare(ReplayPosition o1, ReplayPosition o2)
        {
            if (o1.segment != o2.segment)
                return Long.valueOf(o1.segment).compareTo(o2.segment);

            return Integer.valueOf(o1.position).compareTo(o2.position);
        }
    };

    public ReplayPosition(long segment, int position)
    {
        this.segment = segment;
        assert position >= 0;
        this.position = position;
    }

    public int compareTo(ReplayPosition other)
    {
        return comparator.compare(this, other);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplayPosition that = (ReplayPosition) o;

        if (position != that.position) return false;
        return segment == that.segment;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (segment ^ (segment >>> 32));
        result = 31 * result + position;
        return result;
    }

    @Override
    public String toString()
    {
        return "ReplayPosition(" +
               "segmentId=" + segment +
               ", position=" + position +
               ')';
    }

    public static class ReplayPositionSerializer implements ICompactSerializer2<ReplayPosition>
    {
        public void serialize(ReplayPosition rp, DataOutput dos) throws IOException
        {
            dos.writeLong(rp.segment);
            dos.writeInt(rp.position);
        }

        public ReplayPosition deserialize(DataInput dis) throws IOException
        {
            return new ReplayPosition(dis.readLong(), dis.readInt());
        }
    }
}
