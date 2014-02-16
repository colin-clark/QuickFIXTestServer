package com.cep.messaging.impls.gossip.configuration;

import java.util.Map;

public class Config {
	public String supervisor_host;
	public String log4j_file = "log4j.configuration";
	
	public int listen_port = 5672;
	public String listen_topic = "QUERY";
	public String listen_exchange = "amq.topic";
	
	public boolean thread_pool_inbound = true;
	public int thread_pool_inbound_threads = 2;
	public int thread_pool_inbound_capacity = 10000;

	public boolean thread_pool_outbound = true;
	public int thread_pool_outbound_threads = 2;
	public int thread_pool_outbound_capacity = 10000;

	public boolean thread_pool_route_exec = true;
	public int thread_pool_route_exec_threads = 2;
	public int thread_pool_route_exec_capacity = 1000;

	public boolean thread_pool_timer_exec = true;
	public int thread_pool_timer_exec_threads = 2;
	public boolean external_timing = false;
	
	public String persistence_ip = "localhost";
	public String persistence_cluster;
	public String persistence_cf = "system";
	public int persistence_port = 9160;
	public int persistence_batch_size = 500;
	public int persistence_queue_size = 5000;
	
	public String cluster_name = "DarkStar Cluster";
	public String partitioner = "com.cep.messaging.impls.gossip.partitioning.OrderPreservingPartitioner";
	public SeedProviderDef seed_provider;
	public String initial_token;
	public Integer phi_convict_threshold = 8;
	public Integer storage_port = 7001;
	public String listen_address;
	public Integer max_client_threads = 16;
	public long rpc_timeout = 10;
	
	public int concurrent_reads = 32;
	public int concurrent_writes = 32;
	public int concurrent_replicates = 1;
	public EncryptionOptions encryption_options = new EncryptionOptions();
	
	public DiskAccessMode disk_access_mode = DiskAccessMode.auto;
	public String[] data_file_directories;
	public int memtable_flush_queue_size = 4;
	public int memtable_total_space_in_mb = 2048;
	
    public String request_scheduler = "com.cep.messaging.impls.gossip.scheduler.NoScheduler";
    public RequestSchedulerId request_scheduler_id = RequestSchedulerId.keyspace;
    public RequestSchedulerOptions request_scheduler_options = new RequestSchedulerOptions();
    
    public Boolean rpc_keepalive = true;
    public Integer rpc_send_buff_size_in_bytes = 8192;
    public Integer rpc_recv_buff_size_in_bytes = 8192;
    public Integer thrift_max_message_length_in_mb = 16;
    public Integer thrift_framed_transport_size_in_mb = 15;
    public Boolean snapshot_before_compaction = false;
    public Integer compaction_thread_priority = Thread.MIN_PRIORITY;
    public Integer rpc_min_threads = 16;
    public Integer rpc_max_threads = Integer.MAX_VALUE;
    public String authenticator = "com.cep.messaging.impls.gossip.auth.AllowAllAuthenticator";
    public String authority = "com.cep.messaging.impls.gossip.auth.AllowAllAuthority";
    public Integer index_interval = 128;
    public Integer memtable_flush_writers = 1;
    public String saved_caches_directory;
    public double reduce_cache_capacity_to = 0.6;
    public Integer column_index_size_in_kb = 64;
    public Integer sliced_buffer_size_in_kb = 64;
    public Integer rpc_port = 9159;
    public Integer max_hint_window_in_ms = Integer.MAX_VALUE;
    public Integer in_memory_compaction_limit_in_mb = 256;
    public Integer concurrent_compactors = Runtime.getRuntime().availableProcessors();
    public Integer compaction_throughput_mb_per_sec = 16;
    public String rpc_address = "localhost";
    public String endpoint_snitch;
    public String gatePath;
    public String ADVHandler = null;
	public String adv_oracle_url;
	public String adv_oracle_user;
	public String adv_oracle_pwd;
	public boolean cluster_surveillance = false;
	public String HASupervisor_host;
	public String socket_address;
	public int socket_port;
	public String connection_handler = "com.cep.metadata.CassandraConnectionHelper";
	public Map<String,String> connection_handler_parameters;
	
    public static enum CommitLogSync {
        periodic,
        batch
    }
    
    public static enum DiskAccessMode {
        auto,
        mmap,
        mmap_index_only,
        standard,
    }
    
    public static enum RequestSchedulerId
    {
        keyspace
    }

}
