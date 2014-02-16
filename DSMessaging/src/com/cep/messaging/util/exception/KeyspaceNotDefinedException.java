package com.cep.messaging.util.exception;

import com.cep.messaging.impls.gossip.thrift.InvalidRequestException;

@SuppressWarnings("serial")
public class KeyspaceNotDefinedException extends InvalidRequestException
{
    public KeyspaceNotDefinedException(String why)
    {
        super(why);
    }
}
