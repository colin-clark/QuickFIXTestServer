package com.cep.messaging.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class JMXConfigurableThreadPoolExecutor extends JMXEnabledThreadPoolExecutor implements JMXConfigurableThreadPoolExecutorMBean 
{

    public JMXConfigurableThreadPoolExecutor(int corePoolSize,
        	                                 long keepAliveTime,
        	                                 TimeUnit unit,
                                             BlockingQueue<Runnable> workQueue, 
                                             NamedThreadFactory threadFactory,
                                             String jmxPath)
    {
        super(corePoolSize, keepAliveTime, unit, workQueue, threadFactory, jmxPath);
    }
    
}