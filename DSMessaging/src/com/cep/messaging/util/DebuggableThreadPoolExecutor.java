package com.cep.messaging.util;

import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encorporates some Executor best practices for DarkStar.  Most of the executors in the system
 * should use or extend this.  There are two main improvements over a vanilla TPE:
 *
 * - If a task throws an exception, the default uncaught exception handler will be invoked; if there is
 *   no such handler, the exception will be logged.
 * - MaximumPoolSize is not supported.  Here is what that means (quoting TPE javadoc):
 *
 *     If fewer than corePoolSize threads are running, the Executor always prefers adding a new thread rather than queuing.
 *     If corePoolSize or more threads are running, the Executor always prefers queuing a request rather than adding a new thread.
 *     If a request cannot be queued, a new thread is created unless this would exceed maximumPoolSize, in which case, the task will be rejected.
 *
 *   We don't want this last stage of creating new threads if the queue is full; it makes it needlessly difficult to
 *   reason about the system's behavior.  In other words, if DebuggableTPE has allocated our maximum number of (core)
 *   threads and the queue is full, we want the enqueuer to block.  But to allow the number of threads to drop if a
 *   stage is less busy, core thread timeout is enabled.
 */
public class DebuggableThreadPoolExecutor extends ThreadPoolExecutor
{
    protected static Logger logger = LoggerFactory.getLogger(DebuggableThreadPoolExecutor.class);

    public DebuggableThreadPoolExecutor(String threadPoolName, int priority)
    {
        this(1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(threadPoolName, priority));
    }

    public DebuggableThreadPoolExecutor(int corePoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
    {
        super(corePoolSize, corePoolSize, keepAliveTime, unit, workQueue, threadFactory);
        allowCoreThreadTimeOut(true);

        // preserve task serialization.  this is more complicated than it needs to be,
        // since TPE rejects if queue.offer reports a full queue.  we'll just
        // override this with a handler that retries until it gets in.  ugly, but effective.
        // (there is an extensive analysis of the options here at
        //  http://today.java.net/pub/a/today/2008/10/23/creating-a-notifying-blocking-thread-pool-executor.html)
        this.setRejectedExecutionHandler(new RejectedExecutionHandler()
        {
            public void rejectedExecution(Runnable task, ThreadPoolExecutor executor)
            {
                BlockingQueue<Runnable> queue = executor.getQueue();
                while (true)
                {
                    if (executor.isShutdown())
                        throw new RejectedExecutionException("ThreadPoolExecutor has shut down");
                    try
                    {
                        if (queue.offer(task, 1000, TimeUnit.MILLISECONDS))
                            break;
                    }
                    catch (InterruptedException e)
                    {
                        throw new AssertionError(e);
                    }
                }
            }
        });
    }

    @Override
    public void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r,t);
        logExceptionsAfterExecute(r, t);
    }

    public static void logExceptionsAfterExecute(Runnable r, Throwable t)
    {
        // exceptions wrapped by FutureTask
        if (r instanceof FutureTask<?>)
        {
            try
            {
                ((FutureTask<?>) r).get();
            }
            catch (InterruptedException e)
            {
                throw new AssertionError(e);
            }
            catch (ExecutionException e)
            {
                if (Thread.getDefaultUncaughtExceptionHandler() == null)
                    logger.error("Error in ThreadPoolExecutor", e.getCause());
                else
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e.getCause());
            }
        }

        // exceptions for non-FutureTask runnables [i.e., added via execute() instead of submit()]
        if (t != null)
        {
            if (Thread.getDefaultUncaughtExceptionHandler() == null)
                logger.error("Error in ThreadPoolExecutor", t);
            else
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
        }
    }
}
