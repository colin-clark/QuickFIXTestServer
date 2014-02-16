package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;

import com.cep.messaging.impls.gossip.partitioning.bounds.AbstractBounds;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;
import com.cep.messaging.impls.gossip.service.IReadCommand;
import com.cep.messaging.impls.gossip.thrift.ColumnParent;
import com.cep.messaging.impls.gossip.thrift.SlicePredicate;
import com.cep.messaging.impls.gossip.thrift.TBinaryProtocol;
import com.cep.messaging.impls.gossip.transport.MessageProducer;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;
import com.cep.messaging.util.DataOutputBuffer;

public class RangeSliceCommand implements MessageProducer, IReadCommand
{
    private static final RangeSliceCommandSerializer serializer = new RangeSliceCommandSerializer();
    
    public final String keyspace;

    public final String column_family;
    public final ByteBuffer super_column;

    public final SlicePredicate predicate;

    public final AbstractBounds range;
    public final int max_keys;

    public RangeSliceCommand(String keyspace, ColumnParent column_parent, SlicePredicate predicate, AbstractBounds range, int max_keys)
    {
        this(keyspace, column_parent.getColumn_family(), column_parent.super_column, predicate, range, max_keys);
    }

    public RangeSliceCommand(String keyspace, String column_family, ByteBuffer super_column, SlicePredicate predicate, AbstractBounds range, int max_keys)
    {
        this.keyspace = keyspace;
        this.column_family = column_family;
        this.super_column = super_column;
        this.predicate = predicate;
        this.range = range;
        this.max_keys = max_keys;
    }

    public Message getMessage(Integer version) throws IOException
    {
        DataOutputBuffer dob = new DataOutputBuffer();
        serializer.serialize(this, dob, version);
        return new Message(GossipUtilities.getLocalAddress(),
                           Verb.RANGE_SLICE,
                           Arrays.copyOf(dob.getData(), dob.getLength()), version);
    }

    @Override
    public String toString()
    {
        return "RangeSliceCommand{" +
               "keyspace='" + keyspace + '\'' +
               ", column_family='" + column_family + '\'' +
               ", super_column=" + super_column +
               ", predicate=" + predicate +
               ", range=" + range +
               ", max_keys=" + max_keys +
               '}';
    }

    public static RangeSliceCommand read(Message message) throws IOException
    {
        byte[] bytes = message.getMessageBody();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return serializer.deserialize(new DataInputStream(bis), message.getVersion());
    }

    public String getKeyspace()
    {
        return keyspace;
    }
}

class RangeSliceCommandSerializer implements ICompactSerializer<RangeSliceCommand>
{
    public void serialize(RangeSliceCommand sliceCommand, DataOutputStream dos, int version) throws IOException
    {
        dos.writeUTF(sliceCommand.keyspace);
        dos.writeUTF(sliceCommand.column_family);
        ByteBuffer sc = sliceCommand.super_column;
        dos.writeInt(sc == null ? 0 : sc.remaining());
        if (sc != null)
            ByteBufferUtil.write(sc, dos);

        TSerializer ser = new TSerializer(new TBinaryProtocol.Factory());
        GossipUtilities.serialize(ser, sliceCommand.predicate, dos);
        AbstractBounds.serializer().serialize(sliceCommand.range, dos);
        dos.writeInt(sliceCommand.max_keys);
    }

    public RangeSliceCommand deserialize(DataInputStream dis, int version) throws IOException
    {
        String keyspace = dis.readUTF();
        String column_family = dis.readUTF();

        int scLength = dis.readInt();
        ByteBuffer super_column = null;
        if (scLength > 0)
            super_column = ByteBuffer.wrap(readBuf(scLength, dis));

        TDeserializer dser = new TDeserializer(new TBinaryProtocol.Factory());
        SlicePredicate pred = new SlicePredicate();
        GossipUtilities.deserialize(dser, pred, dis);

        AbstractBounds range = AbstractBounds.serializer().deserialize(dis);
        int max_keys = dis.readInt();
        return new RangeSliceCommand(keyspace, column_family, super_column, pred, range, max_keys);
    }

    static byte[] readBuf(int len, DataInputStream dis) throws IOException
    {
        byte[] buf = new byte[len];
        int read = 0;
        while (read < len)
            read = dis.read(buf, read, len - read);
        return buf;
    }
}
