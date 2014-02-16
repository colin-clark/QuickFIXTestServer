package com.cep.messaging.impls.gossip.util;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;

import com.cep.messaging.impls.gossip.cache.FreeableMemory;


public class MemoryInputStream extends AbstractDataInput implements DataInput, Closeable
{
    private final FreeableMemory mem;
    private int position = 0;
    
    public MemoryInputStream(FreeableMemory mem)
    {
        this.mem = mem;
    }
    
    public int read() throws IOException
    {       
        return mem.getValidByte(position++) & 0xFF;
    }
    
    protected void seekInternal(int pos)
    {
        position = pos;
    }
    
    protected int getPosition()
    {
        return position;
    }
    
    public int skipBytes(int n) throws IOException
    {
        seekInternal(getPosition() + n);
        return position;
    }
    
    public void close()
    {
        // do nothing.
    }
}
