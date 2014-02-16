package com.cep.messaging.impls.gossip.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.cep.messaging.impls.gossip.serialization.ICompactSerializer2;
import com.cep.messaging.util.OpenBitSet;

class BloomFilterSerializer implements ICompactSerializer2<BloomFilter>
{
    public void serialize(BloomFilter bf, DataOutput dos) throws IOException
    {
        long[] bits = bf.bitset.getBits();
        int bitLength = bits.length;

        dos.writeInt(bf.getHashCount());
        dos.writeInt(bitLength);

        for (int i = 0; i < bitLength; i++)
            dos.writeLong(bits[i]);
    }

    public BloomFilter deserialize(DataInput dis) throws IOException
    {
        int hashes = dis.readInt();
        int bitLength = dis.readInt();
        long[] bits = new long[bitLength];
        for (int i = 0; i < bitLength; i++)
            bits[i] = dis.readLong();
        OpenBitSet bs = new OpenBitSet(bits, bitLength);
        return new BloomFilter(hashes, bs);
    }
}


