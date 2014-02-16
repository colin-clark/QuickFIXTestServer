package com.cep.messaging.impls.gossip.cache;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Wraps an ICache in requests + hits tracking.
 */
public class InstrumentingCache<K, V> implements InstrumentingCacheMBean
{
    private final AtomicLong requests = new AtomicLong(0);
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong lastRequests = new AtomicLong(0);
    private final AtomicLong lastHits = new AtomicLong(0);
    private volatile boolean capacitySetManually;
    private final ICache<K, V> map;

    public InstrumentingCache(ICache<K, V> map, String table, String name)
    {
        this.map = map;
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            ObjectName mbeanName = new ObjectName("com.cep.messaging.impls.gossip.cache:type=Caches,keyspace=" + table + ",cache=" + name);
            // unregister any previous, as this may be a replacement.
            if (mbs.isRegistered(mbeanName))
                mbs.unregisterMBean(mbeanName);
            mbs.registerMBean(this, mbeanName);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void put(K key, V value)
    {
        map.put(key, value);
    }

    public V get(K key)
    {
        V v = map.get(key);
        requests.incrementAndGet();
        if (v != null)
            hits.incrementAndGet();
        return v;
    }

    public V getInternal(K key)
    {
        return map.get(key);
    }

    public void remove(K key)
    {
        map.remove(key);
    }

    public int getCapacity()
    {
        return map.capacity();
    }

    public boolean isCapacitySetManually()
    {
        return capacitySetManually;
    }

    public void updateCapacity(int capacity)
    {
        map.setCapacity(capacity);
    }

    public void setCapacity(int capacity)
    {
        updateCapacity(capacity);
        capacitySetManually = true;
    }

    public int size()
    {
        return map.size();
    }

    public int getSize()
    {
        return size();
    }

    public long getHits()
    {
        return hits.get();
    }

    public long getRequests()
    {
        return requests.get();
    }

    public double getRecentHitRate()
    {
        long r = requests.get();
        long h = hits.get();
        try
        {
            return ((double)(h - lastHits.get())) / (r - lastRequests.get());
        }
        finally
        {
            lastRequests.set(r);
            lastHits.set(h);
        }
    }

    public void clear()
    {
        map.clear();
        requests.set(0);
        hits.set(0);
    }

    public Set<K> getKeySet()
    {
        return map.keySet();
    }

    public boolean isPutCopying()
    {
        return map.isPutCopying();
    }
}
