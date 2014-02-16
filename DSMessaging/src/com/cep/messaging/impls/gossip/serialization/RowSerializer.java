package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class RowSerializer implements ICompactSerializer<Row>
{
    public void serialize(Row row, DataOutputStream dos, int version) throws IOException
    {
        ByteBufferUtil.writeWithShortLength(row.key.key, dos);
        ColumnFamily.serializer().serialize(row.cf, dos);
    }

    public Row deserialize(DataInputStream dis, int version, boolean fromRemote) throws IOException
    {
        return new Row(StorageService.getPartitioner().decorateKey(ByteBufferUtil.readWithShortLength(dis)),
                       ColumnFamily.serializer().deserialize(dis, false, fromRemote));
    }

    public Row deserialize(DataInputStream dis, int version) throws IOException
    {
        return deserialize(dis, version, false);
    }
}
