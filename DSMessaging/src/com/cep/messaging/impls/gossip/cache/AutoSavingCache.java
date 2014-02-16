package com.cep.messaging.impls.gossip.cache;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.compaction.CompactionInfo;
import com.cep.messaging.impls.gossip.keyspace.compaction.CompactionManager;
import com.cep.messaging.impls.gossip.keyspace.compaction.CompactionType;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.util.BufferedRandomAccessFile;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.FileUtils;
import com.cep.messaging.util.WrappedRunnable;

public abstract class AutoSavingCache<K, V> extends InstrumentingCache<K, V>
{
    private static final Logger logger = LoggerFactory.getLogger(AutoSavingCache.class);

    /** True if a cache flush is currently executing: only one may execute at a time. */
    public static final AtomicBoolean flushInProgress = new AtomicBoolean(false);

    protected final String cfName;
    protected final String tableName;
    protected volatile ScheduledFuture<?> saveTask;
    protected final ColumnFamilyStore.CacheType cacheType;
    
    public AutoSavingCache(ICache<K, V> cache, String tableName, String cfName, ColumnFamilyStore.CacheType cacheType)
    {
        super(cache, tableName, cfName + cacheType);
        this.tableName = tableName;
        this.cfName = cfName;
        this.cacheType = cacheType;
    }

    public abstract ByteBuffer translateKey(K key);
    public abstract double getConfiguredCacheSize(CFMetaData cfm);

    public int getAdjustedCacheSize(long expectedKeys)
    {
        CFMetaData cfm = NodeDescriptor.getCFMetaData(tableName, cfName);
        return (int)Math.min(GossipUtilities.absoluteFromFraction(getConfiguredCacheSize(cfm), expectedKeys), Integer.MAX_VALUE);
    }

    public File getCachePath()
    {
        return NodeDescriptor.getSerializedCachePath(tableName, cfName, cacheType);
    }

    public Writer getWriter()
    {
        return new Writer(tableName, cfName);
    }

    public void scheduleSaving(int savePeriodInSeconds)
    {
        if (saveTask != null)
        {
            saveTask.cancel(false); // Do not interrupt an in-progress save
            saveTask = null;
        }
        if (savePeriodInSeconds > 0)
        {
            WrappedRunnable runnable = new WrappedRunnable()
            {
                public void runMayThrow()
                {
                    submitWrite();
                }
            };
            saveTask = StorageService.tasks.scheduleWithFixedDelay(runnable,
                                                                   savePeriodInSeconds,
                                                                   savePeriodInSeconds,
                                                                   TimeUnit.SECONDS);
        }
    }

    public Future<?> submitWrite()
    {
        return CompactionManager.instance.submitCacheWrite(getWriter());
    }

    @SuppressWarnings("rawtypes")
	public Set<DecoratedKey> readSaved()
    {
        File path = getCachePath();
        Set<DecoratedKey> keys = new TreeSet<DecoratedKey>();
        if (path.exists())
        {
            DataInputStream in = null;
            try
            {
                long start = System.currentTimeMillis();

                logger.info(String.format("reading saved cache %s", path));
                in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
                while (in.available() > 0)
                {
                    int size = in.readInt();
                    byte[] bytes = new byte[size];
                    in.readFully(bytes);
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    DecoratedKey key;
                    try
                    {
                        key = StorageService.getPartitioner().decorateKey(buffer);
                    }
                    catch (Exception e)
                    {
                        logger.info(String.format("unable to read entry #%s from saved cache %s; skipping remaining entries",
                                                  keys.size(), path.getAbsolutePath()), e);
                        break;
                    }
                    keys.add(key);
                }
                if (logger.isDebugEnabled())
                    logger.debug(String.format("completed reading (%d ms; %d keys) saved cache %s",
                                               System.currentTimeMillis() - start, keys.size(), path));
            }
            catch (IOException ioe)
            {
                logger.warn(String.format("error reading saved cache %s", path.getAbsolutePath()), ioe);
            }
            finally
            {
                FileUtils.closeQuietly(in);
            }
        }
        return keys;
    }

    /**
     * Resizes the cache based on a key estimate.
     * Caller is in charge of synchronizing this correctly if needed
     */
    public void updateCacheSize(long keys)
    {
        if (!isCapacitySetManually())
        {
            int cacheSize = getAdjustedCacheSize(keys);
            if (cacheSize != getCapacity())
            {
                // update cache size for the new volume
                if (logger.isDebugEnabled())
                    logger.debug(cacheType + " capacity for " + cfName + " is " + cacheSize);
                updateCapacity(cacheSize);
            }
        }
    }

    public void reduceCacheSize()
    {
        if (getCapacity() > 0)
        {
            int newCapacity = (int) (NodeDescriptor.getReduceCacheCapacityTo() * size());
            logger.warn(String.format("Reducing %s %s capacity from %d to %s to reduce memory pressure",
                                      cfName, cacheType, getCapacity(), newCapacity));
            setCapacity(newCapacity);
        }
    }

    public class Writer implements CompactionInfo.Holder
    {
        private final Set<K> keys;
        private final CompactionInfo info;
        private final long estimatedTotalBytes;
        private long bytesWritten;

        private Writer(String ksname, String cfname)
        {
            keys = getKeySet();
            long bytes = 0;
            for (K key : keys)
                bytes += translateKey(key).remaining();
            // an approximation -- the keyset can change while saving
            estimatedTotalBytes = bytes;
            CompactionType type;

            if (cacheType.equals(ColumnFamilyStore.CacheType.KEY_CACHE_TYPE)) 
                type = CompactionType.KEY_CACHE_SAVE;
            else if (cacheType.equals(ColumnFamilyStore.CacheType.ROW_CACHE_TYPE))
                type = CompactionType.ROW_CACHE_SAVE;
            else
                type = CompactionType.UNKNOWN;

            info = new CompactionInfo(ksname,
                                      cfname,
                                      type,
                                      0,
                                      estimatedTotalBytes);
        }

        public CompactionInfo getCompactionInfo()
        {
            long bytesWritten = this.bytesWritten;
            // keyset can change in size, thus totalBytes can too
            return info.forProgress(bytesWritten,
                                    Math.max(bytesWritten, estimatedTotalBytes));
        }

        public void saveCache() throws IOException
        {
            long start = System.currentTimeMillis();
            File path = getCachePath();

            if (keys.size() == 0 || estimatedTotalBytes == 0)
            {
                logger.debug("Deleting {} (cache is empty)");
                path.delete();
                return;
            }

            logger.debug("Saving {}", path);
            File tmpFile = File.createTempFile(path.getName(), null, path.getParentFile());

            BufferedRandomAccessFile out = new BufferedRandomAccessFile(tmpFile, "rw", BufferedRandomAccessFile.DEFAULT_BUFFER_SIZE, true);
            try
            {
                for (K key : keys)
                {
                    ByteBuffer bytes = translateKey(key);
                    ByteBufferUtil.writeWithLength(bytes, out);
                    bytesWritten += bytes.remaining();
                }
            }
            finally
            {
                out.close();
            }

            path.delete(); // ignore error if it didn't exist
            if (!tmpFile.renameTo(path))
                throw new IOException("Unable to rename " + tmpFile + " to " + path);
            logger.info(String.format("Saved %s (%d items) in %d ms",
                        path.getName(), keys.size(), (System.currentTimeMillis() - start)));
        }
    }
}
