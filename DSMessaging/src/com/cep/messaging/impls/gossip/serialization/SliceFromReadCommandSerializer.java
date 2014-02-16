package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.commands.ReadCommand;
import com.cep.messaging.impls.gossip.keyspace.commands.SliceFromReadCommand;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class SliceFromReadCommandSerializer extends ReadCommandSerializer
{
    @Override
    public void serialize(ReadCommand rm, DataOutputStream dos, int version) throws IOException
    {
        SliceFromReadCommand realRM = (SliceFromReadCommand)rm;
        dos.writeBoolean(realRM.isDigestQuery());
        dos.writeUTF(realRM.table);
        ByteBufferUtil.writeWithShortLength(realRM.key, dos);
        realRM.queryPath.serialize(dos);
        ByteBufferUtil.writeWithShortLength(realRM.start, dos);
        ByteBufferUtil.writeWithShortLength(realRM.finish, dos);
        dos.writeBoolean(realRM.reversed);
        dos.writeInt(realRM.count);
    }

    @Override
    public ReadCommand deserialize(DataInputStream dis, int version) throws IOException
    {
        boolean isDigest = dis.readBoolean();
        SliceFromReadCommand rm = new SliceFromReadCommand(dis.readUTF(),
                                                           ByteBufferUtil.readWithShortLength(dis),
                                                           QueryPath.deserialize(dis),
                                                           ByteBufferUtil.readWithShortLength(dis),
                                                           ByteBufferUtil.readWithShortLength(dis),
                                                           dis.readBoolean(),
                                                           dis.readInt());
        rm.setDigestQuery(isDigest);
        return rm;
    }
}
