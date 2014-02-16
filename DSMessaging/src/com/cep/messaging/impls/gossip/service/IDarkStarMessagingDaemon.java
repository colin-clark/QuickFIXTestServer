package com.cep.messaging.impls.gossip.service;

import java.io.IOException;

/**
 * The <code>DarkStarDaemon</code> interface captures the lifecycle of a
 * DarkStar daemon that runs on a single node.
 * 
 */
public interface IDarkStarMessagingDaemon
{
    /**
     * Initialize the DarkStar Daemon based on the given <a
     * href="http://commons.apache.org/daemon/jsvc.html">Commons
     * Daemon</a>-specific arguments. To clarify, this is a hook for JSVC.
     * 
     * @param arguments
     *            the arguments passed in from JSVC
     * @throws IOException
     */
    public void init(String[] arguments) throws IOException;
    
    /**
     * Start the DarkStar Daemon, assuming that it has already been
     * initialized, via either {@link #init(String[])} or
     * {@link #load(String[])}.
     * 
     * @throws IOException
     */
    public void start() throws IOException;
    
    /**
     * Stop the daemon, ideally in an idempotent manner.
     */
    public void stop();
    
    /**
     * Clean up all resources obtained during the lifetime of the daemon. Just
     * to clarify, this is a hook for JSVC.
     */
    public void destroy();

    public void startRPCServer();
    public void stopRPCServer();
    public boolean isRPCServerRunning();
    
    /**
     * A convenience method to initialize and start the daemon in one shot.
     */
    public void activate();
    
    /**
     * A convenience method to stop and destroy the daemon in one shot.
     */
    public void deactivate();
    
}
