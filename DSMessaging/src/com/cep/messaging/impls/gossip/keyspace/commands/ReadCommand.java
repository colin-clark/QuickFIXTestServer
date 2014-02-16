package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.serialization.ReadCommandSerializer;
import com.cep.messaging.impls.gossip.service.IReadCommand;
import com.cep.messaging.impls.gossip.transport.MessageProducer;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.Verb;


public abstract class ReadCommand implements MessageProducer, IReadCommand
{
    public static final byte CMD_TYPE_GET_SLICE_BY_NAMES = 1;
    public static final byte CMD_TYPE_GET_SLICE = 2;

    private static ReadCommandSerializer serializer = new ReadCommandSerializer();

    public static ReadCommandSerializer serializer()
    {
        return serializer;
    }

    public Message getMessage(Integer version) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        ReadCommand.serializer().serialize(this, dos, version);
        return new Message(GossipUtilities.getLocalAddress(), Verb.READ, bos.toByteArray(), version);
    }

    public final QueryPath queryPath;
    public final String table;
    public final ByteBuffer key;
    private boolean isDigestQuery = false;    
    public final byte commandType;

    protected ReadCommand(String table, ByteBuffer key, QueryPath queryPath, byte cmdType)
    {
        this.table = table;
        this.key = key;
        this.queryPath = queryPath;
        this.commandType = cmdType;
    }
    
    public boolean isDigestQuery()
    {
        return isDigestQuery;
    }

    public void setDigestQuery(boolean isDigestQuery)
    {
        this.isDigestQuery = isDigestQuery;
    }

    public String getColumnFamilyName()
    {
        return queryPath.columnFamilyName;
    }
    
    public abstract ReadCommand copy();

    public abstract Row getRow(Table table) throws IOException;

    @SuppressWarnings("rawtypes")
	protected AbstractType getComparator()
    {
        return ColumnFamily.getComparatorFor(table, getColumnFamilyName(), queryPath.superColumnName);
    }

    public String getKeyspace()
    {
        return table;
    }
}
