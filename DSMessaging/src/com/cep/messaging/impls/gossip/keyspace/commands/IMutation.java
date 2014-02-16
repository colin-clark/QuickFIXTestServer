package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

public interface IMutation
{
    public String getTable();
    public Collection<Integer> getColumnFamilyIds();
    public ByteBuffer key();
    public void apply() throws IOException;
    public String toString(boolean shallow);
}
