package com.cep.messaging.impls.gossip.transport;

public interface IMessageCallback {
    /**
     * @return true if this callback is on the read path and its latency should be
     * given as input to the dynamic snitch.
     */
    public boolean isLatencyForSnitch();
}
