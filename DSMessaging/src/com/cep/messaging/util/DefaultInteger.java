package com.cep.messaging.util;


public class DefaultInteger 
{
    private final int originalValue;
    private int currentValue;
    
    public DefaultInteger(int value)
    {
        originalValue = value;
        currentValue = value;
    }
    
    public int value() 
    {
        return currentValue;
    }
    
    public void set(int i)
    {
        currentValue = i;
    }
    
    public boolean isModified()
    {
        return originalValue != currentValue;
    }
}
