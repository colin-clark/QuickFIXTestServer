package com.cep.messaging.impls.gossip.cache;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;

/**
 * Provides cache objects with a requested capacity.
 */
public interface IRowCacheProvider
{
    @SuppressWarnings("rawtypes")
	public ICache<DecoratedKey, ColumnFamily> create(int capacity, String tableName, String cfName);
}
