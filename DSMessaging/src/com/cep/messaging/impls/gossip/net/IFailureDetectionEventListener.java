package com.cep.messaging.impls.gossip.net;

import java.net.InetAddress;

/**
 * Implemented by the Gossiper to convict an endpoint based on the PHI
 * calculated by the Failure Detector on the inter-arrival times of the heart
 * beats.
 */

public interface IFailureDetectionEventListener {
	/**
	 * Convict the specified endpoint.
	 * 
	 * @param ep
	 *            endpoint to be convicted
	 */
	public void convict(InetAddress ep);

}
