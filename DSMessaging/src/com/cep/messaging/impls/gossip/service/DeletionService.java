package com.cep.messaging.impls.gossip.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.cep.messaging.util.FileUtils;
import com.cep.messaging.util.JMXEnabledThreadPoolExecutor;
import com.cep.messaging.util.WrappedRunnable;

public class DeletionService
{
    public static final int MAX_RETRIES = 10;

    public static final ExecutorService executor = new JMXEnabledThreadPoolExecutor("FILEUTILS-DELETE-POOL");

    public static void executeDelete(final String file)
    {
        Runnable deleter = new WrappedRunnable()
        {
            protected void runMayThrow() throws IOException
            {
                FileUtils.deleteWithConfirm(new File(file));
            }
        };
        executor.execute(deleter);
    }
    
    public static void waitFor() throws InterruptedException, ExecutionException
    {
        executor.submit(new Runnable() { public void run() { }}).get();
    }
}
