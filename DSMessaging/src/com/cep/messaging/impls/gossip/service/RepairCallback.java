package com.cep.messaging.impls.gossip.service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.util.SimpleCondition;

public class RepairCallback<T> implements IAsyncCallback
{
    private final IResponseResolver<T> resolver;
    private final List<InetAddress> endpoints;
    private final SimpleCondition condition = new SimpleCondition();
    private final long startTime;
    protected final AtomicInteger received = new AtomicInteger(0);

    /**
     * The main difference between this and ReadCallback is, ReadCallback has a ConsistencyLevel
     * it needs to achieve.  Repair on the other hand is happy to repair whoever replies within the timeout.
     *
     * (The other main difference of course is, this is only created once we know we have a digest
     * mismatch, and we're going to do full-data reads from everyone -- that is, this is the final
     * stage in the read process.)
     */
    public RepairCallback(IResponseResolver<T> resolver, List<InetAddress> endpoints)
    {
        this.resolver = resolver;
        this.endpoints = endpoints;
        this.startTime = System.currentTimeMillis();
    }

    public T get() throws TimeoutException, DigestMismatchException, IOException
    {
        long timeout = NodeDescriptor.getRpcTimeout() - (System.currentTimeMillis() - startTime);
        try
        {
            condition.await(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex)
        {
            throw new AssertionError(ex);
        }

        return received.get() > 1 ? resolver.resolve() : null;
    }

    public void response(Message message)
    {
        resolver.preprocess(message);
        if (received.incrementAndGet() == endpoints.size())
            condition.signal();
    }

    public boolean isLatencyForSnitch()
    {
        return true;
    }
}
