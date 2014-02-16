package com.cep.messaging.impls.gossip.transport.handlers;

import com.cep.messaging.impls.gossip.transport.messages.Message;

/**
 * IVerbHandler provides the method that all verb handlers need to implement.
 * The concrete implementation of this interface would provide the functionality
 * for a given verb.
 */

public interface IVerbHandler {
	/**
	 * This method delivers a message to the implementing class (if the
	 * implementing class was registered by a call to
	 * MessagingService.registerVerbHandlers). Note that the caller should not
	 * be holding any locks when calling this method because the implementation
	 * may be synchronized.
	 * 
	 * @param message
	 *            - incoming message that needs handling.
	 * @param id
	 */
	public void doVerb(Message message, String id);
}
