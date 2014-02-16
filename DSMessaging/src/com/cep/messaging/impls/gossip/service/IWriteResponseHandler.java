package com.cep.messaging.impls.gossip.service;

import java.util.concurrent.TimeoutException;

import com.cep.messaging.impls.gossip.thrift.UnavailableException;

public interface IWriteResponseHandler extends IAsyncCallback
{
    public void get() throws TimeoutException;
    public void assureSufficientLiveNodes() throws UnavailableException;
}
