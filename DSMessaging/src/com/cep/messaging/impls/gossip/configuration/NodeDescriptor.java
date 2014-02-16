package com.cep.messaging.impls.gossip.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.cep.messaging.impls.gossip.auth.AllowAllAuthenticator;
import com.cep.messaging.impls.gossip.auth.AllowAllAuthority;
import com.cep.messaging.impls.gossip.auth.IAuthenticator;
import com.cep.messaging.impls.gossip.auth.IAuthority;
import com.cep.messaging.impls.gossip.configuration.Config.RequestSchedulerId;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyType;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.sstable.Descriptor;
import com.cep.messaging.impls.gossip.net.IEndpointSnitch;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.replication.LocalStrategy;
import com.cep.messaging.impls.gossip.scheduler.IRequestScheduler;
import com.cep.messaging.impls.gossip.scheduler.NoScheduler;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.Pair;
import com.cep.messaging.util.exception.ConfigurationException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class NodeDescriptor {
	/*
	 * leave listenAddress null so we can fall through to getLocalHost
	 */
	private static InetAddress listenAddress; 
	private static SeedProvider seedProvider;
	private static int consistencyThreads = 4; 
	private static IPartitioner<?> partitioner;
	private static Config conf;
	static Map<String, KSMetaData> tables = new HashMap<String, KSMetaData>();
    private static IRequestScheduler requestScheduler;
    private static RequestSchedulerId requestSchedulerId;
    private static RequestSchedulerOptions requestSchedulerOptions;
    private static IAuthenticator authenticator = new AllowAllAuthenticator();
    private static IAuthority authority = new AllowAllAuthority();
    private static Config.DiskAccessMode indexAccessMode;
    private static InetAddress rpcAddress;
    private static IEndpointSnitch snitch;


	private final static String DEFAULT_CONFIGURATION = "configuration/darkstarnode.yaml";
	
	public static final UUID INITIAL_VERSION = new UUID(4096, 0); // has type nibble set to 1, 
																	// everything else to zero.
	private static volatile UUID defsVersion = INITIAL_VERSION;

	/**
	 * Inspect the classpath to find storage configuration file
	 */
	public static URL getStorageConfigURL() throws ConfigurationException {
		String configUrl = System.getenv("DARKSTAR_CONFIG");
		if (configUrl == null) {
			System.out.println("Environment Variable DARKSTAR_CONFIG is not set, using default url");
			configUrl = DEFAULT_CONFIGURATION;
			if (!new File(configUrl).exists()) {
				throw new ConfigurationException("Cannot locate " + configUrl);
			}
		}
		URL url;
		try {
			url = new File(configUrl).toURI().toURL();
			url.openStream(); // catches well-formed but bogus URLs
		} catch (Exception e) {
			ClassLoader loader = NodeDescriptor.class.getClassLoader();
			url = loader.getResource(configUrl);
			if (url == null) {
				throw new ConfigurationException("Cannot locate " + configUrl);
			}
		}
		return url;
	}

	static {
		try {
			URL url = getStorageConfigURL();
			InputStream input = null;
			try {
				input = url.openStream();
			} catch (IOException e) {
				// getStorageConfigURL should have ruled this out
				throw new AssertionError(e);
			}
			org.yaml.snakeyaml.constructor.Constructor constructor = new org.yaml.snakeyaml.constructor.Constructor(Config.class);
			TypeDescription seedDesc = new TypeDescription(SeedProviderDef.class);
			seedDesc.putMapPropertyType("parameters", String.class, String.class);
			constructor.addTypeDescription(seedDesc);
			Yaml yaml = new Yaml(new Loader(constructor));
			conf = (Config) yaml.load(input);

			/* Hashing strategy */
			if (conf.partitioner == null) {
				throw new ConfigurationException("Missing directive: partitioner");
			}
			try {
				partitioner = GossipUtilities.newPartitioner(conf.partitioner);
			} catch (Exception e) {
				throw new ConfigurationException("Invalid partitioner class " + conf.partitioner);
			}

			/* phi convict threshold for FailureDetector */
			if (conf.phi_convict_threshold < 5 || conf.phi_convict_threshold > 16) {
				throw new ConfigurationException("phi_convict_threshold must be between 5 and 16");
			}

			/* Local IP or hostname to bind services to */
			if (conf.listen_address != null) {
				if (conf.listen_address.equals("0.0.0.0")) {
					throw new ConfigurationException("listen_address must be a single interface.");
				}

				try {
					listenAddress = InetAddress.getByName(conf.listen_address);
				} catch (UnknownHostException e) {
					throw new ConfigurationException("Unknown listen_address '" + conf.listen_address + "'");
				}
			}
			
            /* Request Scheduler setup */
            requestSchedulerOptions = conf.request_scheduler_options;
            if (conf.request_scheduler != null)
            {
                try
                {
                    if (requestSchedulerOptions == null)
                    {
                        requestSchedulerOptions = new RequestSchedulerOptions();
                    }
                    Class cls = Class.forName(conf.request_scheduler);
                    requestScheduler = (IRequestScheduler) cls.getConstructor(RequestSchedulerOptions.class).newInstance(requestSchedulerOptions);
                }
                catch (ClassNotFoundException e)
                {
                    throw new ConfigurationException("Invalid Request Scheduler class " + conf.request_scheduler);
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Unable to instantiate request scheduler", e);
                }
            }
            else
            {
                requestScheduler = new NoScheduler();
            }

            if (conf.request_scheduler_id == RequestSchedulerId.keyspace)
            {
                requestSchedulerId = conf.request_scheduler_id;
            }
            else
            {
                // Default to Keyspace
                requestSchedulerId = RequestSchedulerId.keyspace;
            }

            /* Authentication and authorization backend, implementing IAuthenticator and IAuthority */
            if (conf.authenticator != null)
                authenticator = GossipUtilities.<IAuthenticator>construct(conf.authenticator, "authenticator");
            if (conf.authority != null)
                authority = GossipUtilities.<IAuthority>construct(conf.authority, "authority");
            authenticator.validateConfiguration();
            authority.validateConfiguration();
            
            // Hardcoded system tables
            KSMetaData systemMeta = new KSMetaData(Table.SYSTEM_TABLE,
                                                   LocalStrategy.class,
                                                   KSMetaData.optsWithRF(1),
                                                   CFMetaData.StatusCf,
                                                   CFMetaData.HintsCf,
                                                   CFMetaData.MigrationsCf,
                                                   CFMetaData.SchemaCf,
                                                   CFMetaData.IndexCf,
                                                   CFMetaData.NodeIdCf);
            CFMetaData.map(CFMetaData.StatusCf);
            CFMetaData.map(CFMetaData.HintsCf);
            CFMetaData.map(CFMetaData.MigrationsCf);
            CFMetaData.map(CFMetaData.SchemaCf);
            CFMetaData.map(CFMetaData.IndexCf);
            CFMetaData.map(CFMetaData.NodeIdCf);
            tables.put(Table.SYSTEM_TABLE, systemMeta);

			/* Load the seeds for node contact points */
			if (conf.seed_provider == null) {
				throw new ConfigurationException("seeds configuration is missing; a minimum of one seed is required.");
			}
			try {
				Class<?> seedProviderClass = Class.forName(conf.seed_provider.class_name);
				seedProvider = (SeedProvider) seedProviderClass.getConstructor(Map.class).newInstance(conf.seed_provider.parameters);
			}
			// there are about 5 checked exceptions that could be thrown here.
			catch (Exception e) {
				System.err.println("Fatal configuration error" + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
			if (seedProvider.getSeeds().size() == 0) {
				throw new ConfigurationException("The seed provider lists no seeds.");
			}
			
            /* evaluate the DiskAccessMode Config directive, which also affects indexAccessMode selection */           
            if (conf.disk_access_mode == Config.DiskAccessMode.auto)
            {
                conf.disk_access_mode = System.getProperty("os.arch").contains("64") ? Config.DiskAccessMode.mmap : Config.DiskAccessMode.standard;
                indexAccessMode = conf.disk_access_mode;
            }
            else if (conf.disk_access_mode == Config.DiskAccessMode.mmap_index_only)
            {
                conf.disk_access_mode = Config.DiskAccessMode.standard;
                indexAccessMode = Config.DiskAccessMode.mmap;
            }
            else
            {
                indexAccessMode = conf.disk_access_mode;
            }
            /* Local IP or hostname to bind RPC server to */
            if (conf.rpc_address != null)
            {
                try
                {
                    rpcAddress = InetAddress.getByName(conf.rpc_address);
                }
                catch (UnknownHostException e)
                {
                    throw new ConfigurationException("Unknown host in rpc_address " + conf.rpc_address);
                }
            }
            snitch = null;
		} catch (ConfigurationException e) {
			System.err.println("Fatal configuration error" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (YAMLException e) {
			System.err.println("Fatal configuration error" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static IPartitioner<?> getPartitioner() {
		return partitioner;
	}

	public static String getInitialToken() {
		return conf.initial_token;
	}

	public static String getClusterName() {
		return conf.cluster_name;
	}

	public static int getStoragePort() {
		return conf.storage_port;
	}

	public static int getPhiConvictThreshold() {
		return conf.phi_convict_threshold;
	}

	public static int getConsistencyThreads() {
		return consistencyThreads;
	}

	public static Set<InetAddress> getSeeds() {
		return Collections.unmodifiableSet(new HashSet(seedProvider.getSeeds()));
	}

	public static UUID getDefsVersion() {
		return defsVersion;
	}

	public static InetAddress getListenAddress() {
		return listenAddress;
	}
	
	public static Integer getMaxClientThreads() {
		return conf.max_client_threads;
	}

	public static long getRpcTimeout() {
		return conf.rpc_timeout;
	}
	
    public static int getConcurrentReaders() {
        return conf.concurrent_reads;
    }

    public static int getConcurrentWriters() {
        return conf.concurrent_writes;
    }
    
    public static int getConcurrentReplicators() {
        return conf.concurrent_replicates;
    }

    public static IRequestScheduler getRequestScheduler()
    {
        return requestScheduler;
    }

    public static boolean getRpcKeepAlive()
    {
        return conf.rpc_keepalive;
    }

    public static Integer getRpcSendBufferSize()
    {
        return conf.rpc_send_buff_size_in_bytes;
    }

    public static Integer getRpcRecvBufferSize()
    {
        return conf.rpc_recv_buff_size_in_bytes;
    }

    public static int getThriftMaxMessageLength()
    {
        return conf.thrift_max_message_length_in_mb * 1024 * 1024;
    }

    public static int getThriftFramedTransportSize() 
    {
        return conf.thrift_framed_transport_size_in_mb * 1024 * 1024;
    }

    public static Integer getRpcMinThreads()
    {
        return conf.rpc_min_threads;
    }
    
    public static Integer getRpcMaxThreads()
    {
        return conf.rpc_max_threads;
    }

    public static RequestSchedulerOptions getRequestSchedulerOptions()
    {
        return requestSchedulerOptions;
    }

    public static RequestSchedulerId getRequestSchedulerId()
    {
        return requestSchedulerId;
    }

    public static IAuthenticator getAuthenticator()
    {
        return authenticator;
    }

    public static KSMetaData getTableDefinition(String table)
    {
        return tables.get(table);
    }

    public static Set<String> getTables()
    {
        return tables.keySet();
    }

    public static List<String> getNonSystemTables()
    {
        List<String> tableslist = new ArrayList<String>(tables.keySet());
        tableslist.remove(Table.SYSTEM_TABLE);
        return Collections.unmodifiableList(tableslist);
    }

    public static Integer getIndexInterval()
    {
        return conf.index_interval;
    }

    public static CFMetaData getCFMetaData(Integer cfId)
    {
        Pair<String,String> cf = CFMetaData.getCF(cfId);
        if (cf == null)
            return null;
        return getCFMetaData(cf.left, cf.right);
    }

    /*
     * Given a table name & column family name, get the column family
     * meta data. If the table name or column family name is not valid
     * this function returns null.
     */
    public static CFMetaData getCFMetaData(String tableName, String cfName)
    {
        assert tableName != null;
        KSMetaData ksm = tables.get(tableName);
        if (ksm == null)
            return null;
        return ksm.cfMetaData().get(cfName);
    }

    public static AbstractType getComparator(String ksName, String cfName)
    {
        assert ksName != null;
        CFMetaData cfmd = getCFMetaData(ksName, cfName);
        if (cfmd == null)
            throw new IllegalArgumentException("Unknown ColumnFamily " + cfName + " in keyspace " + ksName);
        return cfmd.comparator;
    }

    public static AbstractType getSubComparator(String tableName, String cfName)
    {
        assert tableName != null;
        return getCFMetaData(tableName, cfName).subcolumnComparator;
    }

    public static int getFlushWriters()
    {
    	return conf.memtable_flush_writers;
    }

    public static int getFlushQueueSize()
    {
        return conf.memtable_flush_queue_size;
    }

    public static boolean estimatesRealMemtableSize()
    {
        return conf.memtable_total_space_in_mb > 0;
    }

    public static int getTotalMemtableSpaceInMB()
    {
        // should only be called if estimatesRealMemtableSize() is true
        assert conf.memtable_total_space_in_mb > 0;
        return conf.memtable_total_space_in_mb;
    }

    public static File getSerializedCachePath(String ksName, String cfName, ColumnFamilyStore.CacheType cacheType)
    {
        return new File(conf.saved_caches_directory + File.separator + ksName + "-" + cfName + "-" + cacheType);
    }

    public static double getReduceCacheCapacityTo()
    {
        return conf.reduce_cache_capacity_to;
    }

    public static int getIndexedReadBufferSizeInKB()
    {
        return conf.column_index_size_in_kb;
    }

    public static int getSlicedReadBufferSizeInKB()
    {
        return conf.sliced_buffer_size_in_kb;
    }

    public static IEndpointSnitch getEndpointSnitch()
    {
        return snitch;
    }

    public static IAuthority getAuthority()
    {
        return authority;
    }

    public static AbstractType getValueValidator(String keyspace, String cf, ByteBuffer column)
    {
        return getCFMetaData(keyspace, cf).getValueValidator(column);
    }

    public static void validateMemtableThroughput(int sizeInMB) throws ConfigurationException
    {
        if (sizeInMB <= 0)
            throw new ConfigurationException("memtable_throughput_in_mb must be greater than 0.");
    }

    public static void validateMemtableOperations(double operationsInMillions) throws ConfigurationException
    {
        if (operationsInMillions <= 0)
            throw new ConfigurationException("memtable_operations_in_millions must be greater than 0.0.");
        if (operationsInMillions > Long.MAX_VALUE / 1024 * 1024)
            throw new ConfigurationException("memtable_operations_in_millions must be less than " + Long.MAX_VALUE / 1024 * 1024);
    }

    public static int getRpcPort()
    {
        return Integer.parseInt(conf.rpc_port.toString());
    }

    public static InetAddress getRpcAddress()
    {
        return rpcAddress;
    }

    public static Map<String, CFMetaData> getTableMetaData(String tableName)
    {
        assert tableName != null;
        KSMetaData ksm = tables.get(tableName);
        assert ksm != null;
        return ksm.cfMetaData();
    }

	public static void loadSchemas() throws IOException {
		// Don't need this right now, maybe at a later date		
	}

	public static boolean hintedHandoffEnabled() {
		return false;
	}

    public static int getMaxHintWindow()
    {
        return conf.max_hint_window_in_ms;
    }

    public static EncryptionOptions getEncryptionOptions()
    {
        return conf.encryption_options;
    }

    public static Config.DiskAccessMode getIndexAccessMode()
    {
        return indexAccessMode;
    }

    public static Config.DiskAccessMode getDiskAccessMode()
    {
        return conf.disk_access_mode;
    }

	public static boolean incrementalBackupsEnabled() {
		return false;
	}

    public static CFMetaData getCFMetaData(Descriptor desc)
    {
        return getCFMetaData(desc.ksname, desc.cfname);
    }

    public static int getInMemoryCompactionLimit()
    {
        return conf.in_memory_compaction_limit_in_mb * 1024 * 1024;
    }

    public static int getCompactionThroughputMbPerSec()
    {
        return conf.compaction_throughput_mb_per_sec;
    }

    public static int getColumnIndexSize()
    {
    	return conf.column_index_size_in_kb * 1024;
    }

	public static boolean isSnapshotBeforeCompaction() {
		return false;
	}

	public static boolean getPreheatKeyCache() {
		return false;
	}

    public static int getConcurrentCompactors()
    {
        return conf.concurrent_compactors;
    }

    public static int getCompactionThreadPriority()
    {
        return conf.compaction_thread_priority;
    }

    public static void setTableDefinition(KSMetaData ksm, UUID newVersion)
    {
        if (ksm != null)
            tables.put(ksm.name, ksm);
        NodeDescriptor.defsVersion = newVersion;
    }

    public static KSMetaData getKSMetaData(String table)
    {
        assert table != null;
        return tables.get(table);
    }

    public static void clearTableDefinition(KSMetaData ksm, UUID newVersion)
    {
        tables.remove(ksm.name);
        NodeDescriptor.defsVersion = newVersion;
    }

	public static void createAllDirectories() {
		// Not using disk storage, don't need this now
	}

    public static String[] getAllDataFileLocations()
    {
        return conf.data_file_directories;
    }

	public static String getDataFileLocationForTable(String name, long expectedSize) {
		// Not using disk storage, don't need this now
		return null;
	}

    public static ColumnFamilyType getColumnFamilyType(String tableName, String cfName)
    {
        assert tableName != null && cfName != null;
        CFMetaData cfMetaData = getCFMetaData(tableName, cfName);
        
        if (cfMetaData == null)
            return null;
        return cfMetaData.cfType;
    }

    /**
     * Get a list of data directories for a given table
     * 
     * @param table name of the table.
     * 
     * @return an array of path to the data directories. 
     */
    public static String[] getAllDataFileLocationsForTable(String table)
    {
        String[] tableLocations = new String[conf.data_file_directories.length];

        for (int i = 0; i < conf.data_file_directories.length; i++)
        {
            tableLocations[i] = conf.data_file_directories[i] + File.separator + table;
        }

        return tableLocations;
    }

	public static long getHintedHandoffThrottleDelay() {
		return 0;
	}

    public static String getSavedCachesLocation()
    {
        return conf.saved_caches_directory;
    }


}
