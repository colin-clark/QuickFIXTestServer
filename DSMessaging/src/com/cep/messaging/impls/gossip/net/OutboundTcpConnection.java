package com.cep.messaging.impls.gossip.net;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;

public class OutboundTcpConnection extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(OutboundTcpConnection.class);

	private static final ByteBuffer CLOSE_SENTINEL = ByteBuffer.allocate(0);
	private static final int OPEN_RETRY_DELAY = 100; // ms between retries

	private final InetAddress endpoint;
	private final BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<ByteBuffer>();
	private DataOutputStream output;
	private Socket socket;
	private long completedCount;

	public OutboundTcpConnection(InetAddress remoteEp) {
		super("WRITE-" + remoteEp);
		this.endpoint = remoteEp;
	}

	public void write(ByteBuffer buffer) {
		try {
			queue.put(buffer);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
	}

	void closeSocket() {
		queue.clear();
		write(CLOSE_SENTINEL);
	}

	public void run() {
		//TODO: This is purely debugging and needs to be removed
		if (logger.isDebugEnabled()) {
			logger.debug("Starting new OutboundTcpConnection to " + endpoint.getHostAddress()
					+ " thread id " + Thread.currentThread().getId());
		}
		while (true) {
			ByteBuffer bb = take();
			if (bb == CLOSE_SENTINEL) {
				disconnect();
				continue;
			}
			if (socket != null || connect()) {
				writeConnected(bb);
			} else {
				// clear out the queue, else gossip messages back up.
				queue.clear();
			}
		}
	}

	public int getPendingMessages() {
		return queue.size();
	}

	public long getCompletedMesssages() {
		return completedCount;
	}

	private void writeConnected(ByteBuffer bb) {
		try {
			ByteBufferUtil.write(bb, output);
			if (queue.peek() == null) {
				output.flush();
			}
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("error writing to " + endpoint, e);
			}
			disconnect();
		}
	}

	private void disconnect() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.error("IOException occurred while closing socket: ", e);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Socket closed");
			}
			output = null;
			socket = null;
		}
	}

	private ByteBuffer take() {
		ByteBuffer bb;
		try {
			bb = queue.take();
			completedCount++;
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		return bb;
	}

	private boolean connect() {
		if (logger.isDebugEnabled()) {
			logger.debug("attempting to connect to " + endpoint + ":"+NodeDescriptor.getStoragePort() + ", " + GossipUtilities.getLocalAddress());
		}
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() < start + 10000) {
			try {
				// zero means 'bind on any available port.'
				socket = new Socket(endpoint, NodeDescriptor.getStoragePort(), GossipUtilities.getLocalAddress(), 0);
				logger.info("Successfully connected to " + endpoint);
				socket.setKeepAlive(true);
				socket.setTcpNoDelay(true);
				output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 4096));
				return true;
			} catch (IOException e) {
				logger.warn("Unable to connect to " + endpoint);
				if (logger.isDebugEnabled()) {
					logger.debug("Stack Trace: ", e);
				}
				socket = null;
				try {
					Thread.sleep(OPEN_RETRY_DELAY);
				} catch (InterruptedException e1) {
					throw new AssertionError(e1);
				}
			}
		}
		return false;
	}
}
