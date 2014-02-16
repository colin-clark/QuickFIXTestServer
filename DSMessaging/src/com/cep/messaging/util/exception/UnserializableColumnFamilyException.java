package com.cep.messaging.util.exception;

import java.io.IOException;

@SuppressWarnings("serial")
public class UnserializableColumnFamilyException extends IOException
{
    public final int cfId;
    
    public UnserializableColumnFamilyException(String msg, int cfId)
    {
        super(msg);
        this.cfId = cfId;
    }
}
