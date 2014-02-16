package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInput;
import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.IColumn;

public interface IColumnSerializer extends ICompactSerializer2<IColumn>
{
    public IColumn deserialize(DataInput in, ColumnFamilyStore interner, boolean fromRemote, int expireBefore) throws IOException;
}
