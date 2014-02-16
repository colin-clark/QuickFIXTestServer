package com.cep.messaging.impls.gossip.transport;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.cep.messaging.impls.gossip.transport.messages.Message;

public interface IAsyncResult extends IMessageCallback {    
    /**
     * Same operation as the above get() but allows the calling
     * thread to specify a timeout.
     * @param timeout the maximum time to wait
     * @param tu the time unit of the timeout argument
     * @return the result wrapped in an Object[]
    */
    public byte[] get(long timeout, TimeUnit tu) throws TimeoutException;
        
    /**
     * Store the result obtained for the submitted task.
     * @param result the response message
     */
    public void result(Message result);

    public InetAddress getFrom();
}
