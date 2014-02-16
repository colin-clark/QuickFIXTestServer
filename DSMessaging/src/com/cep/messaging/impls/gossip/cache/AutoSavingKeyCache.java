package com.cep.messaging.impls.gossip.cache;

import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.sstable.Descriptor;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.Pair;

@SuppressWarnings("rawtypes")
public class AutoSavingKeyCache<K extends Pair<Descriptor, DecoratedKey>, V> extends AutoSavingCache<K, V>
{
    public AutoSavingKeyCache(ICache<K, V> cache, String tableName, String cfName)
    {
        super(cache, tableName, cfName, ColumnFamilyStore.CacheType.KEY_CACHE_TYPE);
    }

    @Override
    public double getConfiguredCacheSize(CFMetaData cfm)
    {
        return cfm == null ? CFMetaData.DEFAULT_KEY_CACHE_SIZE : cfm.getKeyCacheSize();
    }

    @Override
    public ByteBuffer translateKey(K key)
    {
        return key.right.key;
    }
}
