package com.cep.messaging.impls.gossip.service;

import java.io.IOException;

import com.cep.messaging.impls.gossip.transport.messages.Message;

public interface IResponseResolver<T> {

	/**
	 * This Method resolves the responses that are passed in . for example : if
	 * its write response then all we get is true or false return values which
	 * implies if the writes were successful but for reads its more complicated
	 * you need to look at the responses and then based on differences schedule
	 * repairs . Hence you need to derive a response resolver based on your
	 * needs from this interface.
	 */
	public T resolve() throws DigestMismatchException, IOException;

	public boolean isDataPresent();

    /**
     * returns the data response without comparing with any digests
     */
    public T getData() throws IOException;

    public void preprocess(Message message);
    public Iterable<Message> getMessages();
}
