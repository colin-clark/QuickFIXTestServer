package com.cep.darkstar.pubsub.sub;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;

import com.cep.commons.EventObject;
import com.eaio.uuid.UUID;
import com.rabbitmq.client.ShutdownSignalException;

public class TopicToCassandra {
	static Logger logger = Logger.getLogger("com.cep.darkstar.offramp.TopicToCassandra");

	public static class Publish implements Runnable {
		private final BlockingQueue<byte[]> queue;
		protected final Mutator<ByteBuffer> mutator;
		protected final String firm;

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
		
		static class TimedCommit implements Runnable {
			protected Mutator<?> mutator;
			protected MutationResult mr;
			
			TimedCommit(Mutator<?> mutator) {
				this.mutator = mutator;
			}
			
			private void commit() {
				try {
					synchronized (this) {
						mr = mutator.execute();
						mutator.discardPendingMutations();
					}
					logger.info(Thread.currentThread().getName()+" committing on "+mr.getHostUsed());
				} catch (Exception ex){
					logger.error("Problem executing insert:"+ex.getLocalizedMessage(), ex);
				}
			}

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
					commit();
				}
			}
		}
		
		// give me the queue and keyspace please
		Publish(BlockingQueue<byte[]> queue, Keyspace ko, String firm) {
			this.queue = queue;
			this.mutator = HFactory.createMutator(ko, bfs);
			this.firm = firm;
			//TimedCommit timedCommit = new TimedCommit(mutator);
			//timedCommit.run();
		}

		// do the actual insertions
		private void insert(byte[] msg) {
			try {
				// stuff batch mutation with insertions
				rowKey = se.toByteBuffer((new UUID()).toString());
				// insert the timestap of the FIX message in ms
				// this way, we can load real time, and batch
				//mutator.addInsertion(se.toByteBuffer(rowKey.toString()),"events", createColumn("ds_timestamp", ls.toByteBuffer(System.currentTimeMillis()), se, bfs));
				mutator.addInsertion(rowKey,"events", HFactory.createColumn("ds_timestamp", ls.toBytes(System.currentTimeMillis()), se, bas));
				// there's got to be a faster way of doing this
				event = new EventObject(new String(msg));
				iterator = event.keys();
				while(iterator.hasNext()) {
					key = iterator.next().toString();
					mutator.addInsertion(rowKey,"events", createColumn(key, se.toByteBuffer(event.get(key).toString()),se, bfs));
				}
				// add firm name to FIX message
				mutator.addInsertion(rowKey, "events", createColumn("firm", se.toByteBuffer(firm), se, bfs));
				if(++commit%500==0) {
					try {
						mr = mutator.execute();
						mutator.discardPendingMutations();
						logger.info(Thread.currentThread().getName()+" committing on "+mr.getHostUsed());
					} catch (Exception ex){
						logger.error("Problem executing insert:"+ex.getLocalizedMessage(), ex);
					}
				}
			} catch (HectorException e) {
				logger.error(e.getMessage(), e);
			} catch (JSONException e) {
				logger.error(e.getMessage(), e);
			}
		}

		// and handle the insertions
		public void run() {
			try{
				while(true) 
					insert(queue.take());
			} catch (InterruptedException e) {
				// never, ever, ever swallow this exception if possible
				Thread.currentThread().interrupt();
			}
		}
	}

	static class Subscribe implements Runnable {
		// set everything up
		long end = 0;
		long begin = 0;
		int i = 0;
		EventObject event;

		// shared queue
		private final BlockingQueue<byte[]> queue;

		SubscribeTopic listen;

		// give me the queue and keyspace please
		Subscribe(BlockingQueue<byte[]> q, String hostName, int portNumber, String topicPattern) {
			queue = q;
			try {
				listen = new SubscribeTopic.Builder().hostName(hostName).portNumber(portNumber).topic(topicPattern).build();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		public void run() {
			// subscribe to bus for messages
			try {
				begin = System.currentTimeMillis();
				while (true) {
					queue.put(listen.nextDelivery());
					//byte[] temp = listen.nextDelivery();
					if ((++i%10000)==0) {
						end = System.currentTimeMillis();
						logger.info("10,000 messages sent to thread pool in "+Double.toString((end-begin)/1000.0)+" seconds");
						begin = end;
					}
				}
			} catch (ShutdownSignalException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				// don't swallow this exception
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void main(String[] args) {
		// initialize log4j
		PropertyConfigurator.configure("log4j.TopicToCassandra");
		// cassandra goodness
		final ConsistencyLevelPolicy mcl = new MyConsistencyLevel();
		Cluster cluster = null;
		Keyspace ko;

		// parse the command line
		try {
			if (args.length > 5) {
				System.err.print(	"Usage: SimpleTopicConsumer [CassandraHostName]\n" +
									"							[BrokerHostName]\n" +
									"                           [firm]\n" +
									"							[brokerport\n" +
									"                           [topicpattern\n" +
									"                           [exchange\n" +
									"                           [queue]\n" +
									"where\n" +
									" - CassandraHostName defaults to locahost\n" +
									" - BrokerHostName defaults to localhost\n"+
									" - topicpattern defaults to \"#\",\n" +
									" - exchange to \"amq.topic\", and\n" +
									" - queue to a private, autodelete queue\n");
				System.exit(1);
			}


			// subscription stuff
			String cassandraHostName = (args.length>0) ? args[0]: "localhost";
			String hostName = (args.length > 1) ? args[1] : "localhost";
			String firm = (args.length > 2) ? args[2] : "CEP";
			int portNumber = (args.length > 3) ? Integer.parseInt(args[3]) : 5672;
			String topicPattern = (args.length > 4) ? args[4] : "#";

			// shared queue that we'll pump to and distribute events to insertion threads
			BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(1000);

			// initialize cassandra and pass keyspace around
			CassandraHostConfigurator hostConfig = new CassandraHostConfigurator(cassandraHostName+":9160");
			hostConfig.setAutoDiscoverHosts(true);
			cluster = HFactory.createCluster("Test Cluster", hostConfig);
			ko = HFactory.createKeyspace("FIX", cluster, mcl);

			// i'd like 6 publishers please
			// we need to specify this on the command line
			final Executor toCassandra = Executors.newFixedThreadPool(6);
			toCassandra.execute(new Publish(queue, ko, firm));
			toCassandra.execute(new Publish(queue, ko, firm));
			toCassandra.execute(new Publish(queue, ko, firm));
			toCassandra.execute(new Publish(queue, ko, firm));
			
			// and 1 subscriber, 'cuz I roll like that...
			final Executor fromRabbit = Executors.newFixedThreadPool(1);
			fromRabbit.execute(new Subscribe(queue, hostName, portNumber, topicPattern));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			cluster.getConnectionManager().shutdown();
			System.exit(1);
		}
	}

	// consistency level
	private static class MyConsistencyLevel implements ConsistencyLevelPolicy {
		@Override
		public HConsistencyLevel get(OperationType op, String arg1) {
			switch(op) {
				case READ: return HConsistencyLevel.ONE;
				case WRITE: return HConsistencyLevel.ANY;
			}
			return HConsistencyLevel.ONE;
		}

		@Override
		public HConsistencyLevel get(OperationType op) {
			switch(op) {
				case READ: return HConsistencyLevel.ONE;
				case WRITE: return HConsistencyLevel.ANY;
			}
			return HConsistencyLevel.ONE;
		}
	}
}

