package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.IOException;


public abstract class AbstractColumnIterator implements IColumnIterator
{
    public void close() throws IOException
    {}

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
