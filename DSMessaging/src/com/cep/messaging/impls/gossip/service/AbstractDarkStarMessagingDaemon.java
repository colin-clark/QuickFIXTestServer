package com.cep.messaging.impls.gossip.service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.CLibrary;
import com.cep.messaging.util.DebuggableThreadPoolExecutor;
import com.cep.messaging.util.Mx4jTool;
import com.cep.messaging.util.exception.ConfigurationException;

public abstract class AbstractDarkStarMessagingDaemon implements IDarkStarMessagingDaemon
{
	private static Logger logger = Logger.getLogger(AbstractDarkStarMessagingDaemon.class);
    
    protected InetAddress listenAddr;
    protected int listenPort;
    protected volatile boolean isRunning = false;
    
    /**
     * This is a hook for concrete daemons to initialize themselves suitably.
     *
     * Subclasses should override this to finish the job (listening on ports, etc.)
     *
     * @throws IOException
     */
    protected void setup() throws IOException
    {
        CLibrary.tryMlockall();

        listenPort = NodeDescriptor.getRpcPort();
        listenAddr = NodeDescriptor.getRpcAddress();
        
        /* 
         * If ThriftAddress was left completely unconfigured, then assume
         * the same default as ListenAddress
         */
        if (listenAddr == null)
            listenAddr = GossipUtilities.getLocalAddress();
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                logger.error("Fatal exception in thread " + t, e);
                if (e instanceof OutOfMemoryError)
                {
                    System.exit(100);
                }
            }
        });
        
        // initialize keyspaces
        //for (String table : NodeDescriptor.getTables())
        //{
        //    if (logger.isDebugEnabled())
        //        logger.debug("opening keyspace " + table);
        //    Table.open(table);
        //}

        // check to see if CL.recovery modified the lastMigrationId. if it did, we need to re apply migrations. this isn't
        // the same as merely reloading the schema (which wouldn't perform file deletion after a DROP). The solution
        // is to read those migrations from disk and apply them.
        //UUID currentMigration = NodeDescriptor.getDefsVersion();
        //UUID lastMigration = Migration.getLastMigrationId();
        //if ((lastMigration != null) && (lastMigration.timestamp() > currentMigration.timestamp()))
        //{
        //    Gossiper.instance.maybeInitializeLocalState(SystemTable.incrementAndGetGeneration());
        //    MigrationManager.applyMigrations(currentMigration, lastMigration);
        //}
        
        //SystemTable.purgeIncompatibleHints();

        // start server internals
        StorageService.instance.registerDaemon(this);
        try
        {
            StorageService.instance.initServer();
        }
        catch (ConfigurationException e)
        {
            logger.error("Fatal configuration error", e);
            System.err.println(e.getMessage() + "\nFatal configuration error; unable to start server.  See log for stacktrace.");
            System.exit(1);
        }

        Mx4jTool.maybeLoad();
    }

    public void init(String[] arguments) throws IOException
    {
        setup();
    }
    
    public void start()
    {
    	startRPCServer();
    }
    
    /**
     * Stop the daemon, ideally in an idempotent manner.
     *
     * Hook for JSVC
     */
    public void stop()
    {
        // this doesn't entirely shut down DarkStar, just the RPC server.
        // jsvc takes care of taking the rest down
        logger.info("DarkStar RPC Server shutting down...");
        stopRPCServer();
    }

    /**
     * Start the underlying RPC server in idempotent manner.
     */
    public void startRPCServer()
    {
        if (!isRunning)
        {
            startServer();
            isRunning = true;
        }
    }

    /**
     * Stop the underlying RPC server in idempotent manner.
     */
    public void stopRPCServer()
    {
        if (isRunning)
        {
            stopServer();
            isRunning = false;
        }
    }

    /**
     * Returns whether the underlying RPC server is running or not.
     */
    public boolean isRPCServerRunning()
    {
        return isRunning;
    }

    /**
     * Start the underlying RPC server.
     * This method shoud be able to restart a server stopped through stopServer().
     * Should throw a RuntimeException if the server cannot be started
     */
    protected abstract void startServer();

    /**
     * Stop the underlying RPC server.
     * This method should be able to stop server started through startServer().
     * Should throw a RuntimeException if the server cannot be stopped
     */
    protected abstract void stopServer();

    
    /**
     * Clean up all resources obtained during the lifetime of the daemon. This
     * is a hook for JSVC.
     */
    public void destroy()
    {}
    
    /**
     * A convenience method to initialize and start the daemon in one shot.
     */
    public void activate()
    {
        try
        {
            setup();
            start();
        } catch (Throwable e)
        {
            String msg = "Exception encountered during startup.";
            logger.error(msg, e);
            
            // try to warn user on stdout too, if we haven't already detached
            System.out.println(msg);
            e.printStackTrace();
            
            System.exit(3);
        }
    }
    
    /**
     * A convenience method to stop and destroy the daemon in one shot.
     */
    public void deactivate()
    {
        stop();
        destroy();
    }
    
    /**
     * A subclass of Java's ThreadPoolExecutor which implements Jetty's ThreadPool
     * interface (for integration with Avro), and performs ClientState cleanup.
     */
    public static class CleaningThreadPool extends ThreadPoolExecutor 
    {
        private ThreadLocal<ClientState> state;
        public CleaningThreadPool(ThreadLocal<ClientState> state, int minWorkerThread, int maxWorkerThreads)
        {
            super(minWorkerThread, maxWorkerThreads, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
            this.state = state;
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            super.afterExecute(r, t);
            DebuggableThreadPoolExecutor.logExceptionsAfterExecute(r, t);
            state.get().logout();
        }


    }
}
