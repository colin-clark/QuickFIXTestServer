package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.commands.CounterMutation;
import com.cep.messaging.impls.gossip.keyspace.commands.RowMutation;
import com.cep.messaging.impls.gossip.thrift.ConsistencyLevel;

public class CounterMutationSerializer implements ICompactSerializer<CounterMutation>
{
    public void serialize(CounterMutation cm, DataOutputStream dos, int version) throws IOException
    {
        RowMutation.serializer().serialize(cm.rowMutation(), dos, version);
        dos.writeUTF(cm.consistency().name());
    }

    public CounterMutation deserialize(DataInputStream dis, int version) throws IOException
    {
        RowMutation rm = RowMutation.serializer().deserialize(dis, version);
        ConsistencyLevel consistency = Enum.valueOf(ConsistencyLevel.class, dis.readUTF());
        return new CounterMutation(rm, consistency);
    }
}
