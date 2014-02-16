package com.cep.messaging.interfaces;

import java.net.InetAddress;
import java.util.Map.Entry;
import java.util.Set;

import com.cep.darkstar.query.ICEPEngine;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.util.exception.InvalidTokenException;

public interface IMessagingService {

	/**
	 * Get the destination for a message based on custom partitioning
	 * 
	 * @param token the contents of the field messages are being partitioned on
	 * @return the address the message should be delivered to
	 */
	public InetAddress getDestinationFor(String token) throws InvalidTokenException;

	/**
	 * @param token the token we are partitioning on
	 * @return true if this message should be handled locally, otherwise false
	 */
	public boolean handleLocally(String token);

	/**
	 * Start the service up
	 * @param isServer true if running as server, false if running as client
	 */
	public void start(boolean isServer);

	/**
	 * @return information about the nodes in the token ring
	 */
	@SuppressWarnings("rawtypes")
	Set<Entry<Token, InetAddress>> getNodeInfo();

	/**
	 * Start the service up as server
	 */
	void start();
	
	public void registerCEPEngine(ICEPEngine engine);
	
	public void setLog4jFile(String filename);

}
