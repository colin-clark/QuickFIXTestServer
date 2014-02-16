package com.cep.messaging.impls.gossip.transport;

import java.io.IOError;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.net.IncomingTcpConnection;
import com.cep.messaging.impls.gossip.net.OutboundTcpConnection;
import com.cep.messaging.impls.gossip.net.OutboundTcpConnectionPool;
import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.serialization.SerializerType;
import com.cep.messaging.impls.gossip.service.IAsyncCallback;
import com.cep.messaging.impls.gossip.transport.handlers.IVerbHandler;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;
import com.cep.messaging.util.DataOutputBuffer;
import com.cep.messaging.util.Pair;
import com.cep.messaging.util.SimpleCondition;
import com.cep.messaging.util.exception.ConfigurationException;
import com.google.common.base.Function;

public final class MessagingService {
	public static final int VERSION_07 = 1;
	public static final int version_ = 2;
	private SerializerType serializerType_ = SerializerType.BINARY;
	private static final long DEFAULT_CALLBACK_TIMEOUT = (long) (1.1 * NodeDescriptor.getRpcTimeout());
	/* This records all the results mapped by message Id */
    private final com.cep.messaging.util.collection.ExpiringMap<String, Pair<InetAddress, IMessageCallback>> callbacks;
	/**
	 * we preface every message with this number so the recipient can validate the sender is sane
	 */
	private static final int PROTOCOL_MAGIC = 0xCA552DFA;

	/* Lookup table for registering message handlers based on the verb. */
	private final Map<Verb, IVerbHandler> verbHandlers_;

	/* Thread pool to handle messaging write activities */
	private final ExecutorService streamExecutor_;

	private final NonBlockingHashMap<InetAddress, OutboundTcpConnectionPool> connectionManagers_ = 
		new NonBlockingHashMap<InetAddress, OutboundTcpConnectionPool>();

	private static final Logger logger_ = LoggerFactory.getLogger(MessagingService.class);
	private static final int LOG_DROPPED_INTERVAL_IN_MS = 5000;

	private SocketThread socketThread;
	private final SimpleCondition listenGate;
	private final Map<Verb, AtomicInteger> droppedMessages = new EnumMap<Verb, AtomicInteger>(Verb.class);

	{
		for (Verb verb : Verb.values()) 
		{
			droppedMessages.put(verb, new AtomicInteger());
		}
	}

	private static class MSHandle {
		public static final MessagingService instance = new MessagingService();
	}

	public static MessagingService instance() {
		return MSHandle.instance;
	}

	private MessagingService() {
		listenGate = new SimpleCondition();
		verbHandlers_ = new EnumMap<Verb, IVerbHandler>(Verb.class);
		streamExecutor_ = new ThreadPoolExecutor(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		Function<Pair<String, Pair<InetAddress, IMessageCallback>>, ?> timeoutReporter = 
			new Function<Pair<String, Pair<InetAddress, IMessageCallback>>, Object>()
        {
            @SuppressWarnings("unused")
			public Object apply(Pair<String, Pair<InetAddress, IMessageCallback>> pair)
            {
                Pair<InetAddress, IMessageCallback> expiredValue = pair.right;
                return null;
            }
        };
        callbacks = new com.cep.messaging.util.collection.ExpiringMap<String, Pair<InetAddress, IMessageCallback>>(DEFAULT_CALLBACK_TIMEOUT, timeoutReporter);
        Runnable logDropped = new Runnable() {
			public void run() {
				logDroppedMessages();
			}
		};
		
		StorageService.scheduledTasks.scheduleWithFixedDelay(logDropped, LOG_DROPPED_INTERVAL_IN_MS, 
				LOG_DROPPED_INTERVAL_IN_MS, TimeUnit.MILLISECONDS);
	}

	/** called from gossiper when it notices a node is not responding. */
	public void convict(InetAddress ep) {
		if (logger_.isDebugEnabled()) {
			logger_.debug("Resetting pool for " + ep);
		}
		getConnectionPool(ep).reset();
	}

	/**
	 * Listen on the specified port.
	 * 
	 * @param localEp
	 *            InetAddress whose port to listen on.
	 */
	public void listen(InetAddress localEp) throws IOException, ConfigurationException {
		socketThread = new SocketThread(getServerSocket(localEp), "ACCEPT-" + localEp);
		socketThread.start();
		listenGate.signalAll();
	}

	private ServerSocket getServerSocket(InetAddress localEp) throws IOException, ConfigurationException {
		final ServerSocket ss;
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		ss = serverChannel.socket();
		ss.setReuseAddress(true);
		InetSocketAddress address = new InetSocketAddress(localEp, NodeDescriptor.getStoragePort());
		try {
			ss.bind(address);
		} catch (BindException e) {
			if (e.getMessage().contains("in use")) {
				throw new ConfigurationException(address + " is in use by another process.  " +
						"Change listen_address:storage_port in to a value that does not conflict with other services");
			}
			else if (e.getMessage().contains("Cannot assign requested address")) {
				throw new ConfigurationException("Unable to bind to address " + address
						+ ". Set listen_address to an interface you can bind to, e.g., your private IP address on EC2");
			}
			else {
				throw e;
			}	
		}
		logger_.info("Starting Messaging Service on port " + NodeDescriptor.getStoragePort());
		return ss;
	}

	public void waitUntilListening() {
		try {
			listenGate.await();
		} catch (InterruptedException ie) {
			logger_.debug("await interrupted");
		}
	}

	public OutboundTcpConnectionPool getConnectionPool(InetAddress to) {
		OutboundTcpConnectionPool cp = connectionManagers_.get(to);
		if (cp == null) {
			connectionManagers_.putIfAbsent(to, new OutboundTcpConnectionPool(to));
			cp = connectionManagers_.get(to);
		}
		return cp;
	}

	public OutboundTcpConnection getConnection(InetAddress to, Message msg) {
		return getConnectionPool(to).getConnection(msg);
	}

	/**
	 * Register a verb and the corresponding verb handler with the Messaging
	 * Service.
	 * 
	 * @param verb
	 * @param verbHandler
	 *            handler for the specified verb
	 */
	public void registerVerbHandlers(Verb verb, IVerbHandler verbHandler) {
		assert !verbHandlers_.containsKey(verb);
		verbHandlers_.put(verb, verbHandler);
	}

	/**
	 * This method returns the verb handler associated with the registered verb.
	 * If no handler has been registered then null is returned.
	 * 
	 * @param type
	 *            for which the verb handler is sought
	 * @return a reference to IVerbHandler which is the handler for the
	 *         specified verb
	 */
	public IVerbHandler getVerbHandler(Verb type) {
		return verbHandlers_.get(type);
	}

	private static AtomicInteger idGen = new AtomicInteger(0);

	private static String nextId() {
		return Integer.toString(idGen.incrementAndGet());
	}

	public void sendReply(Message message, String id, InetAddress to) {
		sendOneWay(message, id, to);
	}


	/** blocks until the processing pools are empty and done. */
	public void waitFor() throws InterruptedException {
		while (!streamExecutor_.isTerminated()) {
			streamExecutor_.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	public void shutdown() {
		logger_.info("Shutting down MessageService...");

		try {
			socketThread.close();
		} catch (IOException e) {
			throw new IOError(e);
		}

		streamExecutor_.shutdownNow();

		logger_.info("Shutdown complete (no further commands will be processed)");
	}

	private static int numThreads = Runtime.getRuntime().availableProcessors();
	private static ExecutorService stage = new ThreadPoolExecutor(numThreads, numThreads,
			60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	public void receive(Message message, String id) {
		if (logger_.isDebugEnabled()) {
			logger_.debug("MessagingService received message: " + message.toString());
		}
		message = SinkManager.processServerMessage(message, id);
		if (message == null) {
			return;
		}
		Runnable runnable = new MessageDeliveryTask(message, id);
		assert stage != null : "No stage for message type " + message.getMessageType();
		stage.execute(runnable);
	}

	public static void validateMagic(int magic) throws IOException {
		if (magic != PROTOCOL_MAGIC) {
			throw new IOException("invalid protocol header");
		}
	}

	public static int getBits(int x, int p, int n) {
		return x >>> (p + 1) - n & ~(-1 << n);
	}

	public ByteBuffer packIt(byte[] bytes, boolean compress, int version) {
		/*
		 * Setting up the protocol header. This is 4 bytes long represented as
		 * an integer. The first 2 bits indicate the serializer type. The 3rd
		 * bit indicates if compression is turned on or off. It is turned off by
		 * default. The 4th bit indicates if we are in streaming mode. It is
		 * turned off by default. The 5th-8th bits are reserved for future use.
		 * The next 8 bits indicate a version number. Remaining 15 bits are not
		 * used currently.
		 */
		int header = 0;
		// Setting up the serializer bit
		header |= serializerType_.ordinal();
		// set compression bit.
		if (compress) {
			header |= 4;
		}
		// Setting up the version bit
		header |= (version << 8);

		ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 4 + bytes.length);
		buffer.putInt(PROTOCOL_MAGIC);
		buffer.putInt(header);
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}

	public int incrementDroppedMessages(Verb verb) {
		return droppedMessages.get(verb).incrementAndGet();
	}

	@SuppressWarnings("unused")
	private void logDroppedMessages() {
		boolean logTpstats = false;
		for (Map.Entry<Verb, AtomicInteger> entry : droppedMessages.entrySet()) {
			AtomicInteger dropped = entry.getValue();
			if (dropped.get() > 0) {
				logTpstats = true;
				logger_.warn("Dropped {} {} messages in the last {} ms",
						new Object[] { dropped, entry.getKey(), LOG_DROPPED_INTERVAL_IN_MS });
			}
			dropped.set(0);
		}
	}

	private static class SocketThread extends Thread {
		private final ServerSocket server;

		SocketThread(ServerSocket server, String name) {
			super(name);
			this.server = server;
		}

		public void run() {
			while (true) {
				try {
					Socket socket = server.accept();
					new IncomingTcpConnection(socket).start();
				} catch (AsynchronousCloseException e) {
					// this happens when another thread calls close().
					logger_.info("MessagingService shutting down server thread.");
					break;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		void close() throws IOException {
			server.close();
		}
	}

	public Map<String, Integer> getCommandPendingTasks() {
		Map<String, Integer> pendingTasks = new HashMap<String, Integer>();
		for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet()) {
			pendingTasks.put(entry.getKey().getHostAddress(), entry.getValue().cmdCon.getPendingMessages());
		}
		return pendingTasks;
	}

	public Map<String, Long> getCommandCompletedTasks() {
		Map<String, Long> completedTasks = new HashMap<String, Long>();
		for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet()) {
			completedTasks.put(entry.getKey().getHostAddress(), entry.getValue().cmdCon.getCompletedMesssages());
		}
		return completedTasks;
	}

	public Map<String, Integer> getResponsePendingTasks() {
		Map<String, Integer> pendingTasks = new HashMap<String, Integer>();
		for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet()) {
			pendingTasks.put(entry.getKey().getHostAddress(),entry.getValue().ackCon.getPendingMessages());
		}
		return pendingTasks;
	}

	public Map<String, Long> getResponseCompletedTasks() {
		Map<String, Long> completedTasks = new HashMap<String, Long>();
		for (Map.Entry<InetAddress, OutboundTcpConnectionPool> entry : connectionManagers_.entrySet()) {
			completedTasks.put(entry.getKey().getHostAddress(), entry.getValue().ackCon.getCompletedMesssages());
		}
		return completedTasks;
	}

    public IAsyncResult sendRR(Message message, InetAddress to) {
        IAsyncResult iar = new AsyncResult();
        sendRR(message, to, iar);
        return iar;
    }
    
    /**
     * Send a message to a given endpoint. similar to sendRR(Message, InetAddress, IAsyncCallback)
     * @param producer pro
     * @param to endpoing to which the message needs to be sent
     * @param cb callback that processes responses.
     * @return a reference to the message id use to match with the result.
     */
    public String sendRR(MessageProducer producer, InetAddress to, IAsyncCallback cb)
    {
        try
        {
            return sendRR(producer.getMessage(Gossiper.instance.getVersion(to)), to, cb);
        }
        catch (IOException ex)
        {
            // happened during message creation.
            throw new IOError(ex);
        }
    }


    
    public String sendRR(Message message, InetAddress to, IMessageCallback cb) {
        return sendRR(message, to, cb, DEFAULT_CALLBACK_TIMEOUT);
    }

    /**
     * Send a message to a given endpoint. This method specifies a callback
     * which is invoked with the actual response.
     * @param message message to be sent.
     * @param to endpoint to which the message needs to be sent
     * @param cb callback interface which is used to pass the responses or
     *           suggest that a timeout occurred to the invoker of the send().
     *           suggest that a timeout occurred to the invoker of the send().
     * @param timeout the timeout used for expiration
     * @return an reference to message id used to match with the result
     */
    public String sendRR(Message message, InetAddress to, IMessageCallback cb, long timeout) {
        String id = nextId();
        addCallback(cb, id, to, timeout);
        sendOneWay(message, id, to);
        return id;
    }

    public void sendOneWay(Message message, InetAddress to) {
        sendOneWay(message, nextId(), to);
    }
    
    private void addCallback(IMessageCallback cb, String messageId, InetAddress to, long timeout) {
        Pair<InetAddress, IMessageCallback> previous = callbacks.put(messageId, 
        		new Pair<InetAddress, IMessageCallback>(to, cb), timeout);
        assert previous == null;
    }
    
    /**
     * Send a message to a given endpoint. This method adheres to the fire and forget
     * style messaging.
     * @param message messages to be sent.
     * @param to endpoint to which the message needs to be sent
     */
    private void sendOneWay(Message message, String id, InetAddress to) {
        if (logger_.isTraceEnabled()) {
            logger_.trace(GossipUtilities.getLocalAddress() + " sending " + message.getVerb() + " to " + id + "@" + to);
        }
        // do local deliveries
        if ( message.getFrom().equals(to) ) {
            receive(message, id);
            return;
        }

        // message sinks are a testing hook
        Message processedMessage = SinkManager.processClientMessage(message, id, to);
        if (processedMessage == null) {
            return;
        }

        // get pooled connection (really, connection queue)
        OutboundTcpConnection connection = getConnection(to, message);

        // pack message with header in a bytebuffer
        byte[] data;
        try {
            DataOutputBuffer buffer = new DataOutputBuffer();
            buffer.writeUTF(id);
            Message.serializer().serialize(message, buffer, message.getVersion());
            data = buffer.getData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert data.length > 0;
        ByteBuffer buffer = packIt(data , false, message.getVersion());

        // write it
        connection.write(buffer);
    }

	public void addLatency(InetAddress localAddress, long l) {
		// TODO Auto-generated method stub
	}
	
    /**
     * Stream a file from source to destination. This is highly optimized
     * to not hold any of the contents of the file in memory.
     * @param header Header contains file to stream and other metadata.
     * @param to endpoint to which we need to stream the file.
    */

    public void stream(StreamHeader header, InetAddress to)
    {
        /* Streaming asynchronously on streamExector_ threads. 
        if (NodeDescriptor.getEncryptionOptions().internode_encryption == EncryptionOptions.InternodeEncryption.all)
            streamExecutor_.execute(new SSLFileStreamTask(header, to));
        else
            streamExecutor_.execute(new FileStreamTask(header, to)); */
    }


}
