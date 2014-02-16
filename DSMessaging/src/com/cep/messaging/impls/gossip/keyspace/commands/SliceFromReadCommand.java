package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryFilter;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.thrift.ColumnParent;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class SliceFromReadCommand extends ReadCommand
{
    public final ByteBuffer start, finish;
    public final boolean reversed;
    public final int count;

    public SliceFromReadCommand(String table, ByteBuffer key, ColumnParent column_parent, ByteBuffer start, ByteBuffer finish, boolean reversed, int count)
    {
        this(table, key, new QueryPath(column_parent), start, finish, reversed, count);
    }

    public SliceFromReadCommand(String table, ByteBuffer key, QueryPath path, ByteBuffer start, ByteBuffer finish, boolean reversed, int count)
    {
        super(table, key, path, CMD_TYPE_GET_SLICE);
        this.start = start;
        this.finish = finish;
        this.reversed = reversed;
        this.count = count;
    }

    public ReadCommand copy()
    {
        ReadCommand readCommand = new SliceFromReadCommand(table, key, queryPath, start, finish, reversed, count);
        readCommand.setDigestQuery(isDigestQuery());
        return readCommand;
    }

    public Row getRow(Table table) throws IOException
    {
        DecoratedKey<?> dk = StorageService.getPartitioner().decorateKey(key);
        return table.getRow(QueryFilter.getSliceFilter(dk, queryPath, start, finish, reversed, count));
    }

    @Override
    public String toString()
    {
        return "SliceFromReadCommand(" +
               "table='" + table + '\'' +
               ", key='" + ByteBufferUtil.bytesToHex(key) + '\'' +
               ", column_parent='" + queryPath + '\'' +
               ", start='" + getComparator().getString(start) + '\'' +
               ", finish='" + getComparator().getString(finish) + '\'' +
               ", reversed=" + reversed +
               ", count=" + count +
               ')';
    }
}

