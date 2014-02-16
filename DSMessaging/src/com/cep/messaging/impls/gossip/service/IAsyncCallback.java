package com.cep.messaging.impls.gossip.service;

import com.cep.messaging.impls.gossip.transport.IMessageCallback;
import com.cep.messaging.impls.gossip.transport.messages.Message;

/**
 * implementors of IAsyncCallback need to make sure that any public methods
 * are threadsafe with respect to response() being called from the message
 * service.  In particular, if any shared state is referenced, making
 * response alone synchronized will not suffice.
 */
public interface IAsyncCallback extends IMessageCallback
{
	/**
	 * @param msg response received.
	 */
	public void response(Message msg);
}
