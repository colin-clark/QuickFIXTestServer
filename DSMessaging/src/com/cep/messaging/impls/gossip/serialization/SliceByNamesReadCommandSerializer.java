package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.cep.messaging.impls.gossip.keyspace.commands.ReadCommand;
import com.cep.messaging.impls.gossip.keyspace.commands.SliceByNamesReadCommand;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class SliceByNamesReadCommandSerializer extends ReadCommandSerializer
{
    @Override
    public void serialize(ReadCommand rm, DataOutputStream dos, int version) throws IOException
    {
        SliceByNamesReadCommand realRM = (SliceByNamesReadCommand)rm;
        dos.writeBoolean(realRM.isDigestQuery());
        dos.writeUTF(realRM.table);
        ByteBufferUtil.writeWithShortLength(realRM.key, dos);
        realRM.queryPath.serialize(dos);
        dos.writeInt(realRM.columnNames.size());
        if (realRM.columnNames.size() > 0)
        {
            for (ByteBuffer cName : realRM.columnNames)
            {
                ByteBufferUtil.writeWithShortLength(cName, dos);
            }
        }
    }

    @Override
    public ReadCommand deserialize(DataInputStream dis, int version) throws IOException
    {
        boolean isDigest = dis.readBoolean();
        String table = dis.readUTF();
        ByteBuffer key = ByteBufferUtil.readWithShortLength(dis);
        QueryPath columnParent = QueryPath.deserialize(dis);

        int size = dis.readInt();
        List<ByteBuffer> columns = new ArrayList<ByteBuffer>();
        for (int i = 0; i < size; ++i)
        {
            columns.add(ByteBufferUtil.readWithShortLength(dis));
        }
        SliceByNamesReadCommand rm = new SliceByNamesReadCommand(table, key, columnParent, columns);
        rm.setDigestQuery(isDigest);
        return rm;
    }
}
