package com.cep.messaging.impls.gossip.service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.commands.ReadCommand;
import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;
import com.cep.messaging.impls.gossip.thrift.UnavailableException;
import com.cep.messaging.impls.gossip.transport.MessagingService;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.ReadResponse;
import com.cep.messaging.impls.gossip.util.Stage;
import com.cep.messaging.impls.gossip.util.StageManager;
import com.cep.messaging.util.SimpleCondition;
import com.cep.messaging.util.WrappedRunnable;

public class ReadCallback<T> implements IAsyncCallback
{
    protected static final Logger logger = LoggerFactory.getLogger( ReadCallback.class );

    private static final ThreadLocal<Random> random = new ThreadLocal<Random>()
    {
        @Override
        protected Random initialValue()
        {
            return new Random();
        }
    };

    public final IResponseResolver<T> resolver;
    protected final SimpleCondition condition = new SimpleCondition();
    private final long startTime;
    protected final int blockfor;
    public final List<InetAddress> endpoints;
    private final IReadCommand command;
    protected final AtomicInteger received = new AtomicInteger(0);

    /**
     * Constructor when response count has to be calculated and blocked for.
     */
    public ReadCallback(IResponseResolver<T> resolver, ConsistencyLevel consistencyLevel, IReadCommand command, List<InetAddress> endpoints)
    {
        this.command = command;
        this.blockfor = determineBlockFor(consistencyLevel, command.getKeyspace());
        this.resolver = resolver;
        this.startTime = System.currentTimeMillis();
        boolean repair = randomlyReadRepair();
        this.endpoints = repair || resolver instanceof RowRepairResolver
                       ? endpoints
                       : endpoints.subList(0, Math.min(endpoints.size(), blockfor)); // min so as to not throw exception until assureSufficient is called

        if (logger.isDebugEnabled())
            logger.debug(String.format("Blockfor/repair is %s/%s; setting up requests to %s",
                                       blockfor, repair, StringUtils.join(this.endpoints, ",")));
    }
    
    private boolean randomlyReadRepair()
    {
        if (resolver instanceof RowDigestResolver)
        {
            assert command instanceof ReadCommand : command;
            String table = ((RowDigestResolver) resolver).table;
            String columnFamily = ((ReadCommand) command).getColumnFamilyName();
            CFMetaData cfmd = NodeDescriptor.getTableMetaData(table).get(columnFamily);
            return cfmd.getReadRepairChance() > random.get().nextDouble();
        }
        // we don't read repair on range scans
        return false;
    }

    public T get() throws TimeoutException, DigestMismatchException, IOException
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
            StringBuilder sb = new StringBuilder("");
            for (Message message : resolver.getMessages())
                sb.append(message.getFrom()).append(", ");
            throw new TimeoutException("Operation timed out - received only " + received.get() + " responses from " + sb.toString() + " .");
        }

        return blockfor == 1 ? resolver.getData() : resolver.resolve();
    }

    public void response(Message message)
    {
        resolver.preprocess(message);
        int n = waitingFor(message)
              ? received.incrementAndGet()
              : received.get();
        if (n >= blockfor && resolver.isDataPresent())
        {
            condition.signal();
            maybeResolveForRepair();
        }
    }

    /**
     * @return true if the message counts towards the blockfor threshold
     * TODO turn the Message into a response so we don't need two versions of this method
     */
    protected boolean waitingFor(Message message)
    {
        return true;
    }

    /**
     * @return true if the response counts towards the blockfor threshold
     */
    protected boolean waitingFor(ReadResponse response)
    {
        return true;
    }

    public void response(ReadResponse result)
    {
        ((RowDigestResolver) resolver).injectPreProcessed(result);
        int n = waitingFor(result)
              ? received.incrementAndGet()
              : received.get();
        if (n >= blockfor && resolver.isDataPresent())
        {
            condition.signal();
            maybeResolveForRepair();
        }
    }

    /**
     * Check digests in the background on the Repair stage if we've received replies
     * too all the requests we sent.
     */
    protected void maybeResolveForRepair()
    {
        if (blockfor < endpoints.size() && received.get() == endpoints.size())
        {
            assert resolver.isDataPresent();
            StageManager.getStage(Stage.READ_REPAIR).execute(new AsyncRepairRunner());
        }
    }

    public int determineBlockFor(ConsistencyLevel consistencyLevel, String table)
    {
        switch (consistencyLevel)
        {
            case ONE:
            case ANY:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            case QUORUM:
                return (Table.open(table).getReplicationStrategy().getReplicationFactor() / 2) + 1;
            case ALL:
                return Table.open(table).getReplicationStrategy().getReplicationFactor();
            default:
                throw new UnsupportedOperationException("invalid consistency level: " + consistencyLevel);
        }
    }

    public void assureSufficientLiveNodes() throws UnavailableException
    {
        if (endpoints.size() < blockfor)
            throw new UnavailableException();
    }

    public boolean isLatencyForSnitch()
    {
        return true;
    }

    private class AsyncRepairRunner extends WrappedRunnable
    {
        protected void runMayThrow() throws IOException
        {
            try
            {
                resolver.resolve();
            }
            catch (DigestMismatchException e)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Digest mismatch:", e);

                ReadCommand readCommand = (ReadCommand) command;
                final RowRepairResolver repairResolver = new RowRepairResolver(readCommand.table, readCommand.key);
                IAsyncCallback repairHandler = new AsyncRepairCallback(repairResolver, endpoints.size());

                for (InetAddress endpoint : endpoints)
                    MessagingService.instance().sendRR(readCommand, endpoint, repairHandler);
            }
        }
    }
}
