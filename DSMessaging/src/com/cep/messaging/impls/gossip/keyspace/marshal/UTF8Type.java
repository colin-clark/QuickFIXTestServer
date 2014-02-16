package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.sql.Types;

import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.google.common.base.Charsets;

public class UTF8Type extends AbstractType<String>
{
    public static final UTF8Type instance = new UTF8Type();

    UTF8Type() {} // singleton

    public String compose(ByteBuffer bytes)
    {
        return getString(bytes);
    }

    public ByteBuffer decompose(String value)
    {
        return ByteBufferUtil.bytes(value, Charsets.UTF_8);
    }

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        return BytesType.bytesCompare(o1, o2);
    }

    public String getString(ByteBuffer bytes)
    {
        try
        {
            return ByteBufferUtil.string(bytes);
        }
        catch (CharacterCodingException e)
        {
            throw new MarshalException("invalid UTF8 bytes " + ByteBufferUtil.bytesToHex(bytes));
        }
    }

    public String toString(String s)
    {
        return s;
    }

    public ByteBuffer fromString(String source)
    {
        return decompose(source);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (!UTF8Validator.validate(bytes.slice()))
            throw new MarshalException("String didn't validate.");
    }
    
    static class UTF8Validator
    {
        enum State {
            START,
            TWO,
            TWO_80,
            THREE_a0bf,
            THREE_80bf_1,
            THREE_80bf_2,
            FOUR_90bf,
            FOUR_80bf_3,
        };    
        
        // since we're not converting to java strings, we don't need to worry about converting to surrogates.
        // buf has already been sliced/duplicated.
        static boolean validate(ByteBuffer buf) 
        {
            int b = 0;
            State state = State.START;
            while (buf.remaining() > 0)
            {
                b = buf.get();
                switch (state)
                {
                    case START:
                        if (b >= 0)
                        {
                            // ascii, state stays start.
                            if (b > 127)
                                return false;
                        }
                        else if ((b >> 5) == -2)
                        {
                            // validate first byte of 2-byte char, 0xc2-0xdf
                            if (b == (byte) 0xc0)
                                // speical case: modified utf8 null is 0xc080.
                                state = State.TWO_80;
                            else if ((b & 0x1e) == 0)
                                return false;
                            state = State.TWO;
                        }
                        else if ((b >> 4) == -2)
                        {
                            // 3 bytes. first byte will be 0xe0 or 0xe1-0xef. handling of second byte will differ.
                            // so 0xe0,0xa0-0xbf,0x80-0xbf or 0xe1-0xef,0x80-0xbf,0x80-0xbf.
                            if (b == (byte)0xe0)
                                state = State.THREE_a0bf;
                            else
                                state = State.THREE_80bf_2;
                            break;            
                        }
                        else if ((b >> 3) == -2)
                        {
                            // 4 bytes. this is where the fun starts.
                            if (b == (byte)0xf0)
                                // 0xf0, 0x90-0xbf, 0x80-0xbf, 0x80-0xbf
                                state = State.FOUR_90bf;
                            else if (b == (byte)0xf4)
                                // 0xf4, 0x80-0xbf, 0x80-0xbf, 0x80-0xbf
                                state = State.FOUR_80bf_3;
                            else
                                // 0xf1-0xf3, 0x80-0xbf, 0x80-0xbf, 0x80-0xbf
                                state = State.FOUR_80bf_3;
                            break;
                        }
                        else
                            return false; // malformed.
                        break;
                    case TWO:
                        // validate second byte of 2-byte char, 0x80-0xbf
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.START;
                        break;
                    case TWO_80:
                        if (b != (byte)0x80)
                            return false;
                        state = State.START;
                        break;
                    case THREE_a0bf:
                        if ((b & 0xe0) == 0x80)
                            return false;
                        state = State.THREE_80bf_1;
                        break;
                    case THREE_80bf_1:
                        // expecting 0x80-0xbf
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.START;
                        break;
                    case THREE_80bf_2:
                        // expecting 0x80-bf and then another of the same.
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.THREE_80bf_1;
                        break;
                    case FOUR_90bf:
                        // expecting 0x90-bf. 2nd byte of 4byte sequence. after that it should degrade to 80-bf,80-bf (like 3byte seq).
                        if ((b & 0x30) == 0)
                            return false;
                        state = State.THREE_80bf_2;
                        break;
                    case FOUR_80bf_3:
                        // expecting 0x80-bf 3 times. degenerates to THREE_80bf_2.
                        if ((b & 0xc0) != 0x80)
                            return false;
                        state = State.THREE_80bf_2;
                        break;
                    default:
                        return false; // invalid state.
                }
            }
            // if state != start, we've got underflow. that's an error.
            return state == State.START;
        }
    }

    public Class<String> getType()
    {
        return String.class;
    }

    public boolean isSigned()
    {
        return false;
    }

    public boolean isCaseSensitive()
    {
        return true;
    }

    public boolean isCurrency()
    {
        return false;
    }

    public int getPrecision(String obj)
    {
        return -1;
    }

    public int getScale(String obj)
    {
        return -1;
    }

    public int getJdbcType()
    {
        return Types.VARCHAR;
    }

    public boolean needsQuotes()
    {
        return true;
    }
}
