package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryFilter;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.thrift.ColumnParent;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class SliceByNamesReadCommand extends ReadCommand
{
    public final SortedSet<ByteBuffer> columnNames;

    public SliceByNamesReadCommand(String table, ByteBuffer key, ColumnParent column_parent, Collection<ByteBuffer> columnNames)
    {
        this(table, key, new QueryPath(column_parent), columnNames);
    }

    @SuppressWarnings("unchecked")
	public SliceByNamesReadCommand(String table, ByteBuffer key, QueryPath path, Collection<ByteBuffer> columnNames)
    {
        super(table, key, path, CMD_TYPE_GET_SLICE_BY_NAMES);
        this.columnNames = new TreeSet<ByteBuffer>(getComparator());
        this.columnNames.addAll(columnNames);
    }

    public ReadCommand copy()
    {
        ReadCommand readCommand= new SliceByNamesReadCommand(table, key, queryPath, columnNames);
        readCommand.setDigestQuery(isDigestQuery());
        return readCommand;
    }
    
    public Row getRow(Table table) throws IOException
    {
        DecoratedKey<?> dk = StorageService.getPartitioner().decorateKey(key);
        return table.getRow(QueryFilter.getNamesFilter(dk, queryPath, columnNames));
    }

    @SuppressWarnings("unchecked")
	@Override
    public String toString()
    {
        return "SliceByNamesReadCommand(" +
               "table='" + table + '\'' +
               ", key=" + ByteBufferUtil.bytesToHex(key) +
               ", columnParent='" + queryPath + '\'' +
               ", columns=[" + getComparator().getString(columnNames) + "]" +
               ')';
    }

}

