package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class BooleanType extends AbstractType<Boolean>
{
  public static final BooleanType instance = new BooleanType();

  BooleanType() {} // singleton

  public Boolean compose(ByteBuffer bytes)
  {
      byte value = bytes.get(bytes.position());
      return Boolean.valueOf(value ==0 ? false:true);
  }

  public ByteBuffer decompose(Boolean value)
  {
    return (value==null) ? ByteBufferUtil.EMPTY_BYTE_BUFFER
                         : value ? ByteBuffer.wrap(new byte[]{1})  // true
                                 : ByteBuffer.wrap(new byte[]{0}); // false
  }
  
  public int compare(ByteBuffer o1, ByteBuffer o2)
  {
      if ((o1 == null) || (o1.remaining() != 1))
        return ((o2 == null) || (o2.remaining() != 1)) ? 0 : -1;
      if ((o2 == null) || (o2.remaining() != 1))
        return 1;

      return o1.compareTo(o2);
  }

  public String getString(ByteBuffer bytes)
  {
      if (bytes.remaining() == 0)
      {
          return Boolean.FALSE.toString();
      }
      if (bytes.remaining() != 1)
      {
          throw new MarshalException("A boolean is stored in exactly 1 byte: "+bytes.remaining());
      }
      byte value = bytes.get(bytes.position());
      
      return value ==0 ? Boolean.FALSE.toString(): Boolean.TRUE.toString();
  }

  public String toString(Boolean b)
  {
      return b.toString();
  }

  public ByteBuffer fromString(String source) throws MarshalException
  {
    
      if (source.isEmpty()|| source.equalsIgnoreCase(Boolean.FALSE.toString()))
          return decompose(false);
      
      if (source.equalsIgnoreCase(Boolean.TRUE.toString()))
          return decompose(true);
      
      throw new MarshalException(String.format("unable to make boolean from '%s'", source));
      
 }

  public void validate(ByteBuffer bytes) throws MarshalException
  {
      if (bytes.remaining() != 1 && bytes.remaining() != 0)
          throw new MarshalException(String.format("Expected 1 or 0 byte value (%d)", bytes.remaining()));
  }

  public Class<Boolean> getType()
  {
      return Boolean.class;
  }

  public boolean isSigned()
  {
    return false;
  }

  public boolean isCaseSensitive()
  {
    return false;
  }

  public boolean isCurrency()
  {
    return false;
  }

  public int getPrecision(Boolean obj)
  {
    return -1;
  }

  public int getScale(Boolean obj)
  {
    return -1;
  }

  public int getJdbcType()
  {
    return Types.BOOLEAN;
  }

  public boolean needsQuotes()
  {
    return false;
  }

}
