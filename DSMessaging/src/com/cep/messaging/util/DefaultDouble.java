package com.cep.messaging.util;


public class DefaultDouble
{
    private final double originalValue;
    private double currentValue;
    
    public DefaultDouble(double value)
    {
        originalValue = value;
        currentValue = value;
    }
    
    public double value() 
    {
        return currentValue;
    }
    
    public void set(double d)
    {
        currentValue = d;
    }
    
    public boolean isModified()
    {
        return originalValue != currentValue;
    }
}
