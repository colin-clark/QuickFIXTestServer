package com.cep.messaging.impls.gossip.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.util.SimpleCondition;

public class TruncateResponseHandler implements IAsyncCallback
{
    protected static final Logger logger = LoggerFactory.getLogger(TruncateResponseHandler.class);
    protected final SimpleCondition condition = new SimpleCondition();
    private final int responseCount;
    protected AtomicInteger responses = new AtomicInteger(0);
    private final long startTime;

    public TruncateResponseHandler(int responseCount)
    {
        // at most one node per range can bootstrap at a time, and these will be added to the write until
        // bootstrap finishes (at which point we no longer need to write to the old ones).
        assert 1 <= responseCount: "invalid response count " + responseCount;

        this.responseCount = responseCount;
        startTime = System.currentTimeMillis();
    }

    public void get() throws TimeoutException
    {
        long timeout = NodeDescriptor.getRpcTimeout() - (System.currentTimeMillis() - startTime);
        boolean success;
        try
        {
            success = condition.await(timeout, TimeUnit.MILLISECONDS); // TODO truncate needs a much longer timeout
        }
        catch (InterruptedException ex)
        {
            throw new AssertionError(ex);
        }

        if (!success)
        {
            throw new TimeoutException("Truncate timed out - received only " + responses.get() + " responses");
        }
    }

    public void response(Message message)
    {
        responses.incrementAndGet();
        if (responses.get() >= responseCount)
            condition.signal();
    }

    public boolean isLatencyForSnitch()
    {
        return false;
    }
}
