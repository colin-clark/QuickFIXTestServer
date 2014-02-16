package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;

import com.cep.messaging.impls.gossip.partitioning.bounds.AbstractBounds;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer2;
import com.cep.messaging.impls.gossip.thrift.IndexClause;
import com.cep.messaging.impls.gossip.thrift.SlicePredicate;
import com.cep.messaging.impls.gossip.thrift.TBinaryProtocol;
import com.cep.messaging.impls.gossip.transport.MessageProducer;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;
import com.cep.messaging.util.DataOutputBuffer;

public class IndexScanCommand implements MessageProducer
{
    private static final IndexScanCommandSerializer serializer = new IndexScanCommandSerializer();

    public final String keyspace;
    public final String column_family;
    public final IndexClause index_clause;
    public final SlicePredicate predicate;
    public final AbstractBounds range;

    public IndexScanCommand(String keyspace, String column_family, IndexClause index_clause, SlicePredicate predicate, AbstractBounds range)
    {

        this.keyspace = keyspace;
        this.column_family = column_family;
        this.index_clause = index_clause;
        this.predicate = predicate;
        this.range = range;
    }

    public Message getMessage(Integer version)
    {
        DataOutputBuffer dob = new DataOutputBuffer();
        try
        {
            serializer.serialize(this, dob);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        return new Message(GossipUtilities.getLocalAddress(),
                           Verb.INDEX_SCAN,
                           Arrays.copyOf(dob.getData(), dob.getLength()),
                           version);
    }

    public static IndexScanCommand read(Message message) throws IOException
    {
        byte[] bytes = message.getMessageBody();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return serializer.deserialize(new DataInputStream(bis));
    }

    private static class IndexScanCommandSerializer implements ICompactSerializer2<IndexScanCommand>
    {
        public void serialize(IndexScanCommand o, DataOutput out) throws IOException
        {
            out.writeUTF(o.keyspace);
            out.writeUTF(o.column_family);
            TSerializer ser = new TSerializer(new TBinaryProtocol.Factory());
            GossipUtilities.serialize(ser, o.index_clause, out);
            GossipUtilities.serialize(ser, o.predicate, out);
            AbstractBounds.serializer().serialize(o.range, out);
        }

        public IndexScanCommand deserialize(DataInput in) throws IOException
        {
            String keyspace = in.readUTF();
            String columnFamily = in.readUTF();

            TDeserializer dser = new TDeserializer(new TBinaryProtocol.Factory());
            IndexClause indexClause = new IndexClause();
            GossipUtilities.deserialize(dser, indexClause, in);
            SlicePredicate predicate = new SlicePredicate();
            GossipUtilities.deserialize(dser, predicate, in);
            AbstractBounds range = AbstractBounds.serializer().deserialize(in);

            return new IndexScanCommand(keyspace, columnFamily, indexClause, predicate, range);
        }
    }
}
