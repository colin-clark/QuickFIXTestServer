package com.cep.messaging.impls.gossip.keyspace;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Collection;

import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.marshal.MarshalException;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

public interface IColumn
{
    public static final int MAX_NAME_LENGTH = GossipUtilities.MAX_UNSIGNED_SHORT;

    public boolean isMarkedForDelete();
    public long getMarkedForDeleteAt();
    public long mostRecentLiveChangeAt();
    public ByteBuffer name();
    public int size();
    public int serializedSize();
    public int serializationFlags();
    public long timestamp();
    public ByteBuffer value();
    public Collection<IColumn> getSubColumns();
    public IColumn getSubColumn(ByteBuffer columnName);
    public void addColumn(IColumn column);
    public IColumn diff(IColumn column);
    public IColumn reconcile(IColumn column);
    public void updateDigest(MessageDigest digest);
    public int getLocalDeletionTime(); // for tombstone GC, so int is sufficient granularity
    @SuppressWarnings("rawtypes")
	public String getString(AbstractType comparator);
    public void validateFields(CFMetaData metadata) throws MarshalException;

    /** clones the column, interning column names and making copies of other underlying byte buffers
     * @param cfs*/
    IColumn localCopy(ColumnFamilyStore cfs);

    /**
     * For a simple column, live == !isMarkedForDelete.
     * For a supercolumn, live means it has at least one subcolumn whose timestamp is greater than the
     * supercolumn deleted-at time.
     */
    boolean isLive();
}
