package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class DoubleType extends AbstractType<Double>
{
    public static final DoubleType instance = new DoubleType();

    DoubleType() {} // singleton    

    public Double compose(ByteBuffer bytes)
    {
      return ByteBufferUtil.toDouble(bytes);
    }
    
    public ByteBuffer decompose(Double value)
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
        if (bytes.remaining() != 8)
        {
            throw new MarshalException("A double is exactly 8 bytes : "+bytes.remaining());
        }
        
        return compose(bytes).toString();
    }

    public String toString(Double d)
    {
        return d.toString();
    }

    public ByteBuffer fromString(String source) throws MarshalException
    {
      // Return an empty ByteBuffer for an empty string.
      if (source.isEmpty())
          return ByteBufferUtil.EMPTY_BYTE_BUFFER;
      
      Double d;
      try
      {
          d = Double.parseDouble(source);
      }
      catch (NumberFormatException e1)
      {
          throw new MarshalException(String.format("unable to coerce '%s' to a double", source), e1);
      }
          
      return decompose(d);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 8 && bytes.remaining() != 0)
            throw new MarshalException(String.format("Expected 8 or 0 byte value for a double (%d)", bytes.remaining()));
    }

    public Class<Double> getType()
    {
        return Double.class;
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

    public int getPrecision(Double obj) // see: http://teaching.idallen.org/dat2343/09f/notes/10FloatingPoint.htm
    {
      return 15;
    }

    public int getScale(Double obj) // see: http://teaching.idallen.org/dat2343/09f/notes/10FloatingPoint.htm
    {
      return 300;
    }

    public int getJdbcType()
    {
      return Types.DOUBLE;
    }

    public boolean needsQuotes()
    {
      return false;
    }
    
}
