package com.cep.messaging.impls.gossip.cache;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.util.exception.ConfigurationException;
import com.sun.jna.Memory;

public class SerializingCacheProvider implements IRowCacheProvider
{
    public SerializingCacheProvider() throws ConfigurationException
    {
        try
        {
            Memory.class.getName();
        }
        catch (NoClassDefFoundError e)
        {
            throw new ConfigurationException("Cannot intialize SerializationCache without JNA in the class path");
        }
    }

    @SuppressWarnings("rawtypes")
	public ICache<DecoratedKey, ColumnFamily> create(int capacity, String tableName, String cfName)
    {
        return new SerializingCache<DecoratedKey, ColumnFamily>(capacity, ColumnFamily.serializer(), tableName, cfName);
    }
}
