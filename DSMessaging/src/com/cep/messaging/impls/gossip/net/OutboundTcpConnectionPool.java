package com.cep.messaging.impls.gossip.net;

import java.net.InetAddress;

import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.Stage;

public class OutboundTcpConnectionPool {
	public final OutboundTcpConnection cmdCon;
	public final OutboundTcpConnection ackCon;

	public OutboundTcpConnectionPool(InetAddress remoteEp) {
		cmdCon = new OutboundTcpConnection(remoteEp);
		cmdCon.start();
		ackCon = new OutboundTcpConnection(remoteEp);
		ackCon.start();
	}

	/**
	 * returns the appropriate connection based on message type. returns null if
	 * a connection could not be established.
	 */
	public OutboundTcpConnection getConnection(Message msg) {
		Stage stage = msg.getMessageType();
		return stage == Stage.REQUEST_RESPONSE
				|| stage == Stage.INTERNAL_RESPONSE || stage == Stage.GOSSIP ? ackCon
				: cmdCon;
	}

	public synchronized void reset() {
		for (OutboundTcpConnection con : new OutboundTcpConnection[] { cmdCon, ackCon }) {
			con.closeSocket();
		}
	}
}
