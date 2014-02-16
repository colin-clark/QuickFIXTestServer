package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;
import com.cep.messaging.impls.gossip.transport.MessageProducer;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;

/**
 * A truncate operation descriptor
 *
 * @author rantav@gmail.com
 *
 */
public class Truncation implements MessageProducer
{
    private static ICompactSerializer<Truncation> serializer;

    public final String keyspace;
    public final String columnFamily;

    static
    {
        serializer = new TruncationSerializer();
    }

    public static ICompactSerializer<Truncation> serializer()
    {
        return serializer;
    }

    public Truncation(String keyspace, String columnFamily)
    {
        this.keyspace = keyspace;
        this.columnFamily = columnFamily;
    }

    /**
     * This is equivalent to calling commit. Applies the changes to
     * to the table that is obtained by calling Table.open().
     */
    public void apply() throws IOException
    {
        Table.open(keyspace).getColumnFamilyStore(columnFamily).truncate();
    }

    public Message getMessage(Integer version) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        serializer().serialize(this, dos, version);
        return new Message(GossipUtilities.getLocalAddress(), Verb.TRUNCATE, bos.toByteArray(), version);
    }

    public String toString()
    {
        return "Truncation(" + "keyspace='" + keyspace + '\'' + ", cf='" + columnFamily + "\')";
    }
}

class TruncationSerializer implements ICompactSerializer<Truncation>
{
    public void serialize(Truncation t, DataOutputStream dos, int version) throws IOException
    {
        dos.writeUTF(t.keyspace);
        dos.writeUTF(t.columnFamily);
    }

    public Truncation deserialize(DataInputStream dis, int version) throws IOException
    {
        String keyspace = dis.readUTF();
        String columnFamily = dis.readUTF();
        return new Truncation(keyspace, columnFamily);
    }
}
