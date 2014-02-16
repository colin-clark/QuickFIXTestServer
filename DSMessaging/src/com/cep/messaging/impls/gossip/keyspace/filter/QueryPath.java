package com.cep.messaging.impls.gossip.keyspace.filter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.thrift.ColumnParent;
import com.cep.messaging.impls.gossip.thrift.ColumnPath;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class QueryPath
{
    public final String columnFamilyName;
    public final ByteBuffer superColumnName;
    public final ByteBuffer columnName;

    public QueryPath(String columnFamilyName, ByteBuffer superColumnName, ByteBuffer columnName)
    {
        this.columnFamilyName = columnFamilyName;
        this.superColumnName = superColumnName;
        this.columnName = columnName;
    }

    public QueryPath(ColumnParent columnParent)
    {
        this(columnParent.column_family, columnParent.super_column, null);
    }

    public QueryPath(String columnFamilyName, ByteBuffer superColumnName)
    {
        this(columnFamilyName, superColumnName, null);
    }

    public QueryPath(String columnFamilyName)
    {
        this(columnFamilyName, null);
    }

    public QueryPath(ColumnPath column_path)
    {
        this(column_path.column_family, column_path.super_column, column_path.column);
    }

    public static QueryPath column(ByteBuffer columnName)
    {
        return new QueryPath(null, null, columnName);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "(" +
               "columnFamilyName='" + columnFamilyName + '\'' +
               ", superColumnName='" + superColumnName + '\'' +
               ", columnName='" + columnName + '\'' +
               ')';
    }

    public void serialize(DataOutputStream dos) throws IOException
    {
        assert !"".equals(columnFamilyName);
        assert superColumnName == null || superColumnName.remaining() > 0;
        assert columnName == null || columnName.remaining() > 0;
        dos.writeUTF(columnFamilyName == null ? "" : columnFamilyName);
        ByteBufferUtil.writeWithShortLength(superColumnName == null ? ByteBufferUtil.EMPTY_BYTE_BUFFER : superColumnName, dos);
        ByteBufferUtil.writeWithShortLength(columnName == null ? ByteBufferUtil.EMPTY_BYTE_BUFFER : columnName, dos);
    }

    public static QueryPath deserialize(DataInputStream din) throws IOException
    {
        String cfName = din.readUTF();
        ByteBuffer scName = ByteBufferUtil.readWithShortLength(din);
        ByteBuffer cName = ByteBufferUtil.readWithShortLength(din);
        return new QueryPath(cfName.isEmpty() ? null : cfName, 
                             scName.remaining() == 0 ? null : scName, 
                             cName.remaining() == 0 ? null : cName);
    }
}
