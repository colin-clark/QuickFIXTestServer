package com.cep.darkstar.darkstarping;

import java.nio.ByteBuffer;
import java.util.Iterator;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;

import com.cep.commons.EventObject;

public class DarkStarPing {
	static String hostName;
	static String clusterName = "DarkStarCluster";
	static String keyspace = "system";
	protected static Mutator<ByteBuffer> mutator;

	protected static StringSerializer se = StringSerializer.get();
	protected static LongSerializer ls = LongSerializer.get();
	protected static ByteBufferSerializer bfs = ByteBufferSerializer.get();
	protected static BytesArraySerializer bas = BytesArraySerializer.get();

	protected static long commit = 0;
	protected EventObject event;
	protected ByteBuffer rowKey;
	protected MutationResult mr;
	protected String key;
	protected Iterator<?> iterator;
	protected static Keyspace keySpace;
	static CassandraHost cassandraHost;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try {
			setProperties(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (true) {
			long start = System.currentTimeMillis();
			System.out.println("Pinging cluster " + clusterName + " at "+hostName);		
			try {
				cassandraHost = new CassandraHost(hostName);
			    boolean found = false;
			    HThriftClient client = new HThriftClient(cassandraHost);
		        client.open();
		        found = client.getCassandra().describe_cluster_name() != null;
		        client.close();              
				long end = System.currentTimeMillis();
				long latency = end - start;
				System.out.println("Reply from cluster " + clusterName + " at " + hostName + ": time=" + latency + " ms");
				Thread.sleep(1000);
			} catch (HectorException he) {
				System.out.println("No connection to cluster " + clusterName + " at " + hostName + " available");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void setProperties(String[] args) throws Exception {
		switch(args.length){
		case 2:
			clusterName = args[1];
		case 1:
			hostName = args[0];
			break;
		default:
			System.out.println("Usage: darkstarping <i.p. address>:<port> [cluster name]");
		}
	}
}
