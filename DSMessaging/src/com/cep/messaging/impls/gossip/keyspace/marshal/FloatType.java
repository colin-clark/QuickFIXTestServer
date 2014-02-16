package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;


public class FloatType extends AbstractType<Float>
{
    public static final FloatType instance = new FloatType();

    FloatType() {} // singleton    

    public Float compose(ByteBuffer bytes)
    {
        return ByteBufferUtil.toFloat(bytes);
    }
    
    public ByteBuffer decompose(Float value)
    {
        return (value==null) ? ByteBufferUtil.EMPTY_BYTE_BUFFER : ByteBufferUtil.bytes(value);
    }
    

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        if (o1.remaining() == 0)
        {
            return o2.remaining() == 0 ? 0 : -1;
        }
        if (o2.remaining() == 0)
        {
            return 1;
        }
        
        return compose(o1).compareTo(compose(o2));
    }

    public String getString(ByteBuffer bytes)
    {
        if (bytes.remaining() == 0)
        {
            return "";
        }
        if (bytes.remaining() != 4)
        {
            throw new MarshalException("A float is exactly 4 bytes : "+bytes.remaining());
        }
        
        return compose(bytes).toString();
    }

    public String toString(Float d)
    {
      return d.toString();
    }

    public ByteBuffer fromString(String source) throws MarshalException
    {
      // Return an empty ByteBuffer for an empty string.
      if (source.isEmpty())
          return ByteBufferUtil.EMPTY_BYTE_BUFFER;
      
      Float f;
      try
      {
          f = Float.parseFloat(source);
      }
      catch (NumberFormatException e1)
      {
          throw new MarshalException(String.format("unable to coerce '%s' to a float", source), e1);
      }
          
      return ByteBufferUtil.bytes(f);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 4 && bytes.remaining() != 0)
            throw new MarshalException(String.format("Expected 4 or 0 byte value for a float (%d)", bytes.remaining()));
    }

    public Class<Float> getType()
    {
        return Float.class;
    }

    public boolean isSigned()
    {
      return true;
    }

    public boolean isCaseSensitive()
    {
      return false;
    }

    public boolean isCurrency()
    {
      return false;
    }

    public int getPrecision(Float obj) // see: http://teaching.idallen.org/dat2343/09f/notes/10FloatingPoint.htm
    {
      return 7;
    }

    public int getScale(Float obj) // see: http://teaching.idallen.org/dat2343/09f/notes/10FloatingPoint.htm
    {
      return 40;
    }

    public int getJdbcType()
    {
      return Types.FLOAT;
    }

    public boolean needsQuotes()
    {
      return false;
    }
    
}
