package com.cep.messaging.impls.gossip.service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.Stage;
import com.cep.messaging.impls.gossip.util.StageManager;
import com.cep.messaging.util.WrappedRunnable;

public class AsyncRepairCallback implements IAsyncCallback
{
    private final RowRepairResolver repairResolver;
    private final int blockfor;
    protected final AtomicInteger received = new AtomicInteger(0);

    public AsyncRepairCallback(RowRepairResolver repairResolver, int blockfor)
    {
        this.repairResolver = repairResolver;
        this.blockfor = blockfor;
    }

    public void response(Message message)
    {
        repairResolver.preprocess(message);
        if (received.incrementAndGet() == blockfor)
        {
            StageManager.getStage(Stage.READ_REPAIR).execute(new WrappedRunnable()
            {
                protected void runMayThrow() throws DigestMismatchException, IOException
                {
                    repairResolver.resolve();
                }
            });
        }
    }

    public boolean isLatencyForSnitch()
    {
        return true;
    }
}
