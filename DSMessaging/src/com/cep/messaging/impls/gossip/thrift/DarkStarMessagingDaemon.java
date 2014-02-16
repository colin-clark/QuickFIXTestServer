package com.cep.messaging.impls.gossip.thrift;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.service.AbstractDarkStarMessagingDaemon;

/**
 * This class supports two methods for creating a node daemon, 
 * invoking the class's main method, and using the jsvc wrapper from 
 * commons-daemon, (for more information on using this class with the 
 * jsvc wrapper, see the 
 * <a href="http://commons.apache.org/daemon/jsvc.html">Commons Daemon</a>
 * documentation).
 */

public class DarkStarMessagingDaemon extends AbstractDarkStarMessagingDaemon
{
    private static Logger logger = LoggerFactory.getLogger(DarkStarMessagingDaemon.class);
    private ThriftServer server;

    protected void startServer()
    {
        if (server == null)
        {
            server = new ThriftServer(listenAddr, listenPort);
            server.start();
        }
    }

    protected void stopServer()
    {
        if (server != null)
        {
            server.stopServer();
            try
            {
                server.join();
            }
            catch (InterruptedException e)
            {
                logger.error("Interrupted while waiting thrift server to stop", e);
            }
            server = null;
        }
    }

    public static void main(String[] args)
    {
        new DarkStarMessagingDaemon().activate();
    }

    /**
     * Simple class to run the thrift connection accepting code in separate
     * thread of control.
     */
    private static class ThriftServer extends Thread
    {
        private TServer serverEngine;

        public ThriftServer(InetAddress listenAddr, int listenPort)
        {
            // now we start listening for clients
            final DarkStarMessagingServer darkstarMessagingServer = new DarkStarMessagingServer();
            DarkStarMessagingService.Processor processor = new DarkStarMessagingService.Processor(darkstarMessagingServer);

            // Transport
            TServerSocket tServerSocket = null;

            try
            {
                tServerSocket = new TCustomServerSocket(new InetSocketAddress(listenAddr, listenPort),
                        NodeDescriptor.getRpcKeepAlive(),
                        NodeDescriptor.getRpcSendBufferSize(),
                        NodeDescriptor.getRpcRecvBufferSize());
            }
            catch (TTransportException e)
            {
                throw new RuntimeException(String.format("Unable to create thrift socket to %s:%s",
                            listenAddr, listenPort), e);
            }

            logger.info(String.format("Binding thrift service to %s:%s", listenAddr, listenPort));

            // Protocol factory
            TProtocolFactory tProtocolFactory = new TBinaryProtocol.Factory(
            		false,
                    true,
                    NodeDescriptor.getThriftMaxMessageLength()
            );

            // Transport factory
            TTransportFactory inTransportFactory, outTransportFactory;
            int tFramedTransportSize = NodeDescriptor.getThriftFramedTransportSize();
            inTransportFactory  = new TFramedTransport.Factory(tFramedTransportSize);
            outTransportFactory = new TFramedTransport.Factory(tFramedTransportSize);
            logger.info("Using TFastFramedTransport with a max frame size of {} bytes.", tFramedTransportSize);

            // ThreadPool Server
            TThreadPoolServer.Args args = new TThreadPoolServer.Args(tServerSocket)
                                          .minWorkerThreads(NodeDescriptor.getRpcMinThreads())
                                          .maxWorkerThreads(NodeDescriptor.getRpcMaxThreads())
                                          .inputTransportFactory(inTransportFactory)
                                          .outputTransportFactory(outTransportFactory)
                                          .inputProtocolFactory(tProtocolFactory)
                                          .outputProtocolFactory(tProtocolFactory)
                                          .processor(processor);

            ExecutorService executorService = new CleaningThreadPool(darkstarMessagingServer.clientState,
                    args.minWorkerThreads,
                    args.maxWorkerThreads);
            serverEngine = new CustomTThreadPoolServer(args, executorService);
        }

        public void run()
        {
            logger.info("Listening for thrift clients...");
            serverEngine.serve();
        }

        public void stopServer()
        {
            logger.info("Stop listening to thrift clients");
            serverEngine.stop();
        }
    }
}
