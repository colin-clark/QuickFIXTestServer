package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public final class IntegerType extends AbstractType<BigInteger>
{
    public static final IntegerType instance = new IntegerType();

    private static int findMostSignificantByte(ByteBuffer bytes)
    {
        int len = bytes.remaining() - 1;
        int i = 0;
        for (; i < len; i++)
        {
            byte b0 = bytes.get(bytes.position() + i);
            if (b0 != 0 && b0 != -1)
                break;
            byte b1 = bytes.get(bytes.position() + i + 1);
            if (b0 == 0 && b1 != 0)
            {
                if (b1 > 0)
                    i++;
                break;
            }
            if (b0 == -1 && b1 != -1)
            {
                if (b1 < 0)
                    i++;
                break;
            }
        }
        return i;
    }

    IntegerType() {/* singleton */}

    public BigInteger compose(ByteBuffer bytes)
    {
        return new BigInteger(ByteBufferUtil.getArray(bytes));
    }

    public ByteBuffer decompose(BigInteger value)
    {
        return ByteBuffer.wrap(value.toByteArray());
    }

    public int compare(ByteBuffer lhs, ByteBuffer rhs)
    {
        int lhsLen = lhs.remaining();
        int rhsLen = rhs.remaining();

        if (lhsLen == 0)
            return rhsLen == 0 ? 0 : -1;
        if (rhsLen == 0)
            return 1;

        int lhsMsbIdx = findMostSignificantByte(lhs);
        int rhsMsbIdx = findMostSignificantByte(rhs);

        //diffs contain number of "meaningful" bytes (i.e. ignore padding)
        int lhsLenDiff = lhsLen - lhsMsbIdx;
        int rhsLenDiff = rhsLen - rhsMsbIdx;

        byte lhsMsb = lhs.get(lhs.position() + lhsMsbIdx);
        byte rhsMsb = rhs.get(rhs.position() + rhsMsbIdx);

        /*         +    -
         *      -----------
         *    + | -d |  1 |
         * LHS  -----------
         *    - | -1 |  d |
         *      -----------
         *          RHS
         *
         * d = difference of length in significant bytes
         */
        if (lhsLenDiff != rhsLenDiff)
        {
            if (lhsMsb < 0)
                return rhsMsb < 0 ? rhsLenDiff - lhsLenDiff : -1;
            if (rhsMsb < 0)
                return 1;
            return lhsLenDiff - rhsLenDiff;
        }

        // msb uses signed comparison
        if (lhsMsb != rhsMsb)
            return lhsMsb - rhsMsb;
        lhsMsbIdx++;
        rhsMsbIdx++;

        // remaining bytes are compared unsigned
        while (lhsMsbIdx < lhsLen)
        {
            lhsMsb = lhs.get(lhs.position() + lhsMsbIdx++);
            rhsMsb = rhs.get(rhs.position() + rhsMsbIdx++);

            if (lhsMsb != rhsMsb)
                return (lhsMsb & 0xFF) - (rhsMsb & 0xFF);
        }

        return 0;
    }

    public String getString(ByteBuffer bytes)
    {
        if (bytes == null)
            return "null";
        if (bytes.remaining() == 0)
            return "empty";

        return new java.math.BigInteger(ByteBufferUtil.getArray(bytes)).toString(10);
    }
    
    public String toString(BigInteger bi)
    {
        return bi.toString();
    }

    public ByteBuffer fromString(String source) throws MarshalException
    {
        // Return an empty ByteBuffer for an empty string.
        if (source.isEmpty())
            return ByteBufferUtil.EMPTY_BYTE_BUFFER;
        
        BigInteger integerType;

        try
        {
            integerType = new BigInteger(source);
        }
        catch (Exception e)
        {
            throw new MarshalException(String.format("unable to make int from '%s'", source), e);
        }

        return decompose(integerType);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        // no invalid integers.
    }

    public Class<BigInteger> getType()
    {
        return BigInteger.class;
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

    public int getPrecision(BigInteger obj)
    {
        return obj.toString().length();
    }

    public int getScale(BigInteger obj)
    {
        return 0;
    }

    public int getJdbcType()
    {
        return Types.BIGINT;
    }

    public boolean needsQuotes()
    {
        return false;
    }
}
