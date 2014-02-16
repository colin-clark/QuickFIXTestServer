package com.cep.messaging.impls.gossip.service;

import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;
import com.cep.messaging.impls.gossip.thrift.UnavailableException;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.util.SimpleCondition;
import com.google.common.collect.Multimap;

public abstract class AbstractWriteResponseHandler implements IWriteResponseHandler
{
    protected final SimpleCondition condition = new SimpleCondition();
    protected final long startTime;
    protected final Collection<InetAddress> writeEndpoints;
    protected final Multimap<InetAddress, InetAddress> hintedEndpoints;
    protected final ConsistencyLevel consistencyLevel;

    protected AbstractWriteResponseHandler(Collection<InetAddress> writeEndpoints, Multimap<InetAddress, InetAddress> hintedEndpoints, ConsistencyLevel consistencyLevel)
    {
        startTime = System.currentTimeMillis();
        this.consistencyLevel = consistencyLevel;
        this.hintedEndpoints = hintedEndpoints;
        this.writeEndpoints = writeEndpoints;
    }

    public void get() throws TimeoutException
    {
        long timeout = NodeDescriptor.getRpcTimeout() - (System.currentTimeMillis() - startTime);
        boolean success;
        try
        {
            success = condition.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex)
        {
            throw new AssertionError(ex);
        }

        if (!success)
        {
            throw new TimeoutException();
        }
    }

    /** null message means "response from local write" */
    public abstract void response(Message msg);

    public abstract void assureSufficientLiveNodes() throws UnavailableException;
}
