package com.cep.messaging.impls.gossip.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.util.Pair;

/**
 * This class manages the streaming of multiple files one after the other.
*/
public class StreamOutSession
{
    private static final Logger logger = LoggerFactory.getLogger( StreamOutSession.class );

    // one host may have multiple stream sessions.
    private static final ConcurrentMap<Pair<InetAddress, Long>, StreamOutSession> streams = new NonBlockingHashMap<Pair<InetAddress, Long>, StreamOutSession>();

    public static StreamOutSession create(String table, InetAddress host, Runnable callback)
    {
        return create(table, host, System.nanoTime(), callback);
    }

    public static StreamOutSession create(String table, InetAddress host, long sessionId)
    {
        return create(table, host, sessionId, null);
    }

    public static StreamOutSession create(String table, InetAddress host, long sessionId, Runnable callback)
    {
        Pair<InetAddress, Long> context = new Pair<InetAddress, Long>(host, sessionId);
        StreamOutSession session = new StreamOutSession(table, context, callback);
        streams.put(context, session);
        return session;
    }

    public static StreamOutSession get(InetAddress host, long sessionId)
    {
        return streams.get(new Pair<InetAddress, Long>(host, sessionId));
    }

    private final Map<String, PendingFile> files = new NonBlockingHashMap<String, PendingFile>();

    public final String table;
    private final Pair<InetAddress, Long> context;
    private final Runnable callback;
    private volatile String currentFile;

    private StreamOutSession(String table, Pair<InetAddress, Long> context, Runnable callback)
    {
        this.table = table;
        this.context = context;
        this.callback = callback;
    }

    public InetAddress getHost()
    {
        return context.left;
    }

    public long getSessionId()
    {
        return context.right;
    }
    
    public void addFilesToStream(List<PendingFile> pendingFiles)
    {
        for (PendingFile pendingFile : pendingFiles)
        {
            if (logger.isDebugEnabled())
                logger.debug("Adding file {} to be streamed.", pendingFile.getFilename());
            files.put(pendingFile.getFilename(), pendingFile);
        }
    }
    
    public void retry()
    {
        streamFile(files.get(currentFile));
    }

    private void streamFile(PendingFile pf)
    {
        if (logger.isDebugEnabled())
            logger.debug("Streaming {} ...", pf);
        currentFile = pf.getFilename();
        MessagingService.instance().stream(new StreamHeader(table, getSessionId(), pf), getHost());
    }

    public void startNext() throws IOException
    {
        assert files.containsKey(currentFile);
        files.remove(currentFile);
        Iterator<PendingFile> iter = files.values().iterator();
        if (iter.hasNext())
            streamFile(iter.next());
    }

    public void close()
    {
        streams.remove(context);
        if (callback != null)
            callback.run();
    }

    /** convenience method for use when testing */
    void await() throws InterruptedException
    {
        while (streams.containsKey(context))
            Thread.sleep(10);
    }

    public Collection<PendingFile> getFiles()
    {
        return files.values();
    }

    public static Set<InetAddress> getDestinations()
    {
        Set<InetAddress> hosts = new HashSet<InetAddress>();
        for (StreamOutSession session : streams.values())
        {
            hosts.add(session.getHost());
        }
        return hosts;
    }

    public static List<PendingFile> getOutgoingFiles(InetAddress host)
    {
        List<PendingFile> list = new ArrayList<PendingFile>();
        for (Map.Entry<Pair<InetAddress, Long>, StreamOutSession> entry : streams.entrySet())
        {
            if (entry.getKey().left.equals(host))
                list.addAll(entry.getValue().getFiles());
        }
        return list;
    }

    public void validateCurrentFile(String file)
    {
        if (!file.equals(currentFile))
            throw new IllegalStateException(String.format("target reports current file is %s but is %s", file, currentFile));
    }

    public void begin()
    {
        PendingFile first = files.isEmpty() ? null : files.values().iterator().next();
        currentFile = first == null ? null : first.getFilename();
        StreamHeader header = new StreamHeader(table, getSessionId(), first, files.values());
        logger.info("Streaming to {}", getHost());
        logger.debug("Files are {}", StringUtils.join(files.values(), ","));
        MessagingService.instance().stream(header, getHost());
    }
}
