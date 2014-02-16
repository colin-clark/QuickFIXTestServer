package com.cep.messaging.util;

public interface JMXConfigurableThreadPoolExecutorMBean extends JMXEnabledThreadPoolExecutorMBean
{
    void setCorePoolSize(int n);

    int getCorePoolSize();
}