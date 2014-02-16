package com.cep.messaging.impls.gossip.transport;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.transport.messages.Message;

class AsyncResult implements IAsyncResult {
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(AsyncResult.class);
    private byte[] result;
    private AtomicBoolean done = new AtomicBoolean(false);
    private Lock lock = new ReentrantLock();
    private Condition condition;
    private long startTime;
    private InetAddress from;

    public AsyncResult() {        
        condition = lock.newCondition();
        startTime = System.currentTimeMillis();
    }    
            
    public byte[] get(long timeout, TimeUnit tu) throws TimeoutException {
        lock.lock();
        try {            
            boolean bVal = true;
            try {
                if (!done.get()) {
                    timeout = TimeUnit.MILLISECONDS.convert(timeout, tu);
                    long overall_timeout = timeout - (System.currentTimeMillis() - startTime);
                    bVal = overall_timeout > 0 && condition.await(overall_timeout, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException ex) {
                throw new AssertionError(ex);
            }
            
            if (!bVal && !done.get()) {                                           
                throw new TimeoutException("Operation timed out.");
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    public void result(Message response) {        
        try {
            lock.lock();
            if (!done.get()) {
                from = response.getFrom();
                result = response.getMessageBody();
                done.set(true);
                condition.signal();
            }
        } finally {
            lock.unlock();
        }        
    }

    public boolean isLatencyForSnitch() {
        return false;
    }

    public InetAddress getFrom() {
        return from;
    }
}
