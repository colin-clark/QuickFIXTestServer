 package com.cep.darkstar.node.configuration;

import java.util.Map;

import com.cep.messaging.impls.gossip.configuration.EncryptionOptions;
import com.cep.messaging.impls.gossip.configuration.RequestSchedulerOptions;
import com.cep.messaging.impls.gossip.configuration.SeedProviderDef;
import com.rabbitmq.client.AMQP;


public class YamlConfigInfo {
	private String supervisor_host;
	private int listen_port = AMQP.PROTOCOL.PORT;
	private String listen_topic = "QUERY";
	private String listen_exchange = "amq.topic";
	private String log4j_file;
	
	private boolean thread_pool_inbound = true;
	private int thread_pool_inbound_threads = 2;
	private int thread_pool_inbound_capacity = 10000;

	private boolean thread_pool_outbound = true;
	private int thread_pool_outbound_threads = 2;
	private int thread_pool_outbound_capacity = 10000;

	private boolean thread_pool_route_exec = true;
	private int thread_pool_route_exec_threads = 2;
	private int thread_pool_route_exec_capacity = 1000;

	private boolean thread_pool_timer_exec = true;
	private int thread_pool_timer_exec_threads = 2;
	private boolean external_timing = false;
	
	private String persistence_ip = "localhost";
	private String persistence_cluster;
	private String persistence_cf = "system";
	private int persistence_port = 9160;
	private int persistence_batch_size = 500;
	private int persistence_queue_size = 5000;
	
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
    public Boolean embed_cassandra = false;
	private String gatePath = ".";
	
	public String ADVHandler = null;
	public String adv_oracle_url;
	public String adv_oracle_user;
	public String adv_oracle_pwd;
	
	public boolean cluster_surveillance = false;
	public String HASupervisor_host;
	
	private String connection_handler = "com.cep.metadata.CassandraConnectionHelper";
	private Map<String,String> connection_handler_parameters;
	
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
	
	public String getSupervisor_host() {
		return supervisor_host;
	}
	public void setSupervisor_host(String supervisor_host) {
		this.supervisor_host = supervisor_host;
	}
	public int getListen_port() {
		return listen_port;
	}
	public void setListen_port(int listen_port) {
		this.listen_port = listen_port;
	}
	public String getListen_topic() {
		return listen_topic;
	}
	public void setListen_topic(String listen_topic) {
		this.listen_topic = listen_topic;
	}
	public String getListen_exchange() {
		return listen_exchange;
	}
	public void setListen_exchange(String listen_exchange) {
		this.listen_exchange = listen_exchange;
	}
	public String getLog4j_file() {
		return log4j_file;
	}
	public void setLog4j_file(String log4j_file) {
		this.log4j_file = log4j_file;
	}
	public boolean isThread_pool_inbound() {
		return thread_pool_inbound;
	}
	public void setThread_pool_inbound(boolean thread_pool_inbound) {
		this.thread_pool_inbound = thread_pool_inbound;
	}
	public int getThread_pool_inbound_threads() {
		return thread_pool_inbound_threads;
	}
	public void setThread_pool_inbound_threads(int thread_pool_inbound_threads) {
		this.thread_pool_inbound_threads = thread_pool_inbound_threads;
	}
	public int getThread_pool_inbound_capacity() {
		return thread_pool_inbound_capacity;
	}
	public void setThread_pool_inbound_capacity(int thread_pool_inbound_capacity) {
		this.thread_pool_inbound_capacity = thread_pool_inbound_capacity;
	}
	public boolean isThread_pool_outbound() {
		return thread_pool_outbound;
	}
	public void setThread_pool_outbound(boolean thread_pool_outbound) {
		this.thread_pool_outbound = thread_pool_outbound;
	}
	public int getThread_pool_outbound_threads() {
		return thread_pool_outbound_threads;
	}
	public void setThread_pool_outbound_threads(int thread_pool_outbound_threads) {
		this.thread_pool_outbound_threads = thread_pool_outbound_threads;
	}
	public int getThread_pool_outbound_capacity() {
		return thread_pool_outbound_capacity;
	}
	public void setThread_pool_outbound_capacity(int thread_pool_outbound_capacity) {
		this.thread_pool_outbound_capacity = thread_pool_outbound_capacity;
	}
	public boolean isThread_pool_route_exec() {
		return thread_pool_route_exec;
	}
	public void setThread_pool_route_exec(boolean thread_pool_route_exec) {
		this.thread_pool_route_exec = thread_pool_route_exec;
	}
	public int getThread_pool_route_exec_threads() {
		return thread_pool_route_exec_threads;
	}
	public void setThread_pool_route_exec_threads(int thread_pool_route_exec_threads) {
		this.thread_pool_route_exec_threads = thread_pool_route_exec_threads;
	}
	public int getThread_pool_route_exec_capacity() {
		return thread_pool_route_exec_capacity;
	}
	public void setThread_pool_route_exec_capacity(
			int thread_pool_route_exec_capacity) {
		this.thread_pool_route_exec_capacity = thread_pool_route_exec_capacity;
	}
	public boolean isThread_pool_timer_exec() {
		return thread_pool_timer_exec;
	}
	public void setThread_pool_timer_exec(boolean thread_pool_timer_exec) {
		this.thread_pool_timer_exec = thread_pool_timer_exec;
	}
	public int getThread_pool_timer_exec_threads() {
		return thread_pool_timer_exec_threads;
	}
	public void setThread_pool_timer_exec_threads(int thread_pool_timer_exec_threads) {
		this.thread_pool_timer_exec_threads = thread_pool_timer_exec_threads;
	}
	public boolean isExternal_timing() {
		return external_timing;
	}
	public void setExternal_timing(boolean external_timing) {
		this.external_timing = external_timing;
	}
	public String getPersistence_ip() {
		return persistence_ip;
	}
	public void setPersistence_ip(String persistence_ip) {
		this.persistence_ip = persistence_ip;
	}
	public String getPersistence_cluster() {
		return persistence_cluster;
	}
	public void setPersistence_cluster(String persistence_cluster) {
		this.persistence_cluster = persistence_cluster;
	}
	public String getPersistence_cf() {
		return persistence_cf;
	}
	public void setPersistence_cf(String persistence_cf) {
		this.persistence_cf = persistence_cf;
	}
	public int getPersistence_port() {
		return persistence_port;
	}
	public void setPersistence_port(int persistence_port) {
		this.persistence_port = persistence_port;
	}
	public int getPersistence_batch_size() {
		return persistence_batch_size;
	}
	public void setPersistence_batch_size(int persistence_batch_size) {
		this.persistence_batch_size = persistence_batch_size;
	}
	public Boolean getEmbed_cassandra() {
		return embed_cassandra;
	}
	public void setEmbed_cassandra(Boolean embed_cassandra) {
		this.embed_cassandra = embed_cassandra;
	}
	public String getGatePath() {
		return gatePath ;
	}
	public void setGatePath(String gatePath) {
		this.gatePath = gatePath;
	}
	public int getPersistence_queue_size() {
		return persistence_queue_size;
	}
	public void setPersistence_queue_size(int persistence_queue_size) {
		this.persistence_queue_size = persistence_queue_size;
	}
	public String getConnection_handler() {
		return connection_handler;
	}
	public void setConnection_handler(String connection_handler) {
		this.connection_handler = connection_handler;
	}
	public Map<String, String> getConnection_handler_parameters() {
		return connection_handler_parameters;
	}
	public void setConnection_handler_parameters(
			Map<String, String> connection_handler_parameters) {
		this.connection_handler_parameters = connection_handler_parameters;
	}

}
