package com.cep.messaging.impls.gossip.keyspace.iterator;

import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.google.common.collect.AbstractIterator;

public abstract class SimpleAbstractColumnIterator extends AbstractIterator<IColumn> implements IColumnIterator
{
    public void close() throws IOException {}
}
