package com.cep.messaging.util;

import java.nio.ByteBuffer;

public abstract class Filter
{
    protected int hashCount;

    public int getHashCount()
    {
        return hashCount;
    }

    public abstract void add(ByteBuffer key);

    public abstract boolean isPresent(ByteBuffer key);
}
