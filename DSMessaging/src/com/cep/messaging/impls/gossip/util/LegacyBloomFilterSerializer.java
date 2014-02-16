package com.cep.messaging.impls.gossip.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.BitSet;

import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;

class LegacyBloomFilterSerializer implements ICompactSerializer<LegacyBloomFilter>
{
    public void serialize(LegacyBloomFilter bf, DataOutputStream dos, int version)
            throws IOException
    {
        throw new UnsupportedOperationException("Shouldn't be serializing legacy bloom filters");
//        dos.writeInt(bf.getHashCount());
//        ObjectOutputStream oos = new ObjectOutputStream(dos);
//        oos.writeObject(bf.getBitSet());
//        oos.flush();
    }

    public LegacyBloomFilter deserialize(DataInputStream dis, int version) throws IOException
    {
        int hashes = dis.readInt();
        ObjectInputStream ois = new ObjectInputStream(dis);
        try
        {
          BitSet bs = (BitSet) ois.readObject();
          return new LegacyBloomFilter(hashes, bs);
        } catch (ClassNotFoundException e)
        {
          throw new RuntimeException(e);
        }
    }
}
