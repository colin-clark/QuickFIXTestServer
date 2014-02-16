package com.cep.messaging.impls.gossip.cache;

import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;

@SuppressWarnings("rawtypes")
public class AutoSavingRowCache<K extends DecoratedKey, V> extends AutoSavingCache<K, V>
{
    public AutoSavingRowCache(ICache<K, V> cache, String tableName, String cfName)
    {
        super(cache, tableName, cfName, ColumnFamilyStore.CacheType.ROW_CACHE_TYPE);
    }

    @Override
    public double getConfiguredCacheSize(CFMetaData cfm)
    {
        return cfm == null ? CFMetaData.DEFAULT_ROW_CACHE_SIZE : cfm.getRowCacheSize();
    }

    @Override
    public ByteBuffer translateKey(K key)
    {
        return key.key;
    }
}
