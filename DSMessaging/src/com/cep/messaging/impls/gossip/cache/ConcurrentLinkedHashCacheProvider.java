package com.cep.messaging.impls.gossip.cache;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;

public class ConcurrentLinkedHashCacheProvider implements IRowCacheProvider
{
    @SuppressWarnings("rawtypes")
	public ICache<DecoratedKey, ColumnFamily> create(int capacity, String tableName, String cfName)
    {
        return ConcurrentLinkedHashCache.create(capacity, tableName, cfName);
    }
}
