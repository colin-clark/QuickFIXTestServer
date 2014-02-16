package com.cep.messaging.impls.gossip.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.node.Gossiper;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;

public class IncomingTcpConnection extends Thread {
	private static Logger logger = LoggerFactory.getLogger(IncomingTcpConnection.class);

	private Socket socket;

	public IncomingTcpConnection(Socket socket) {
		assert socket != null;
		this.socket = socket;
	}

	/**
	 * A new connection will either stream or message for its entire lifetime:
	 * because streaming bypasses the InputStream implementations to use
	 * sendFile, we cannot begin buffering until we've determined the type of
	 * the connection.
	 */
	@Override
	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting new IncomingTcpConnection thread from " + socket.getInetAddress().getHostAddress()
					+ " thread id " + Thread.currentThread().getId());
		}
		DataInputStream input;
		int version;
		try {
			// determine the connection type to decide whether to buffer
			input = new DataInputStream(socket.getInputStream());
			MessagingService.validateMagic(input.readInt());
			int header = input.readInt();
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 4096));
			version = MessagingService.getBits(header, 15, 8);
			Gossiper.instance.setVersion(socket.getInetAddress(), version);
		} catch (IOException e) {
			close();
			throw new IOError(e);
		}
		while (true) {
			try {
				int size = input.readInt();
				byte[] contentBytes = new byte[size];
				input.readFully(contentBytes);

				if (version > MessagingService.version_) {
					logger.info("Received connection from newer protocol version. Ignorning message.");
				} else {
					// todo: need to be aware of message version.
					DataInputStream dis = new DataInputStream(new ByteArrayInputStream(contentBytes));
					String id = dis.readUTF();
					Message message = Message.serializer().deserialize(dis, version);
					MessagingService.instance().receive(message, id);
				}
				// prepare to read the next message
				MessagingService.validateMagic(input.readInt());
				int header = input.readInt();
				version = MessagingService.getBits(header, 15, 8);
			} catch (EOFException e) {
				if (logger.isTraceEnabled()) {
					logger.trace("eof reading from socket; closing", e);
				}
				break;
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("error reading from socket; closing", e);
				}
				break;
			}
		}

		close();
	}

	private void close() {
		try {
			socket.close();
		} catch (IOException e) {
			logger.error("IOException occurred while closing socket", e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Socket closed");
		}
	}

}
