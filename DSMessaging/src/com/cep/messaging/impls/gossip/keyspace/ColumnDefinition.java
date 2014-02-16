package com.cep.messaging.impls.gossip.keyspace;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.marshal.TypeParser;
import com.cep.messaging.impls.gossip.thrift.ColumnDef;
import com.cep.messaging.impls.gossip.thrift.IndexType;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.util.exception.ConfigurationException;

@SuppressWarnings("rawtypes")
public class ColumnDefinition
{
    public final static String D_COLDEF_INDEXTYPE = "KEYS";
    public final static String D_COLDEF_INDEXNAME = null;
    public final ByteBuffer name;
	private AbstractType validator;
    private IndexType index_type;
    private String index_name;

    public ColumnDefinition(ByteBuffer name, AbstractType validator, IndexType index_type, String index_name)
    {
        this.name = name;
        this.index_type = index_type;
        this.index_name = index_name;
        this.validator = validator;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ColumnDefinition that = (ColumnDefinition) o;
        if (index_name != null ? !index_name.equals(that.index_name) : that.index_name != null)
            return false;
        if (index_type != null ? !index_type.equals(that.index_type) : that.index_type != null)
            return false;
        if (!name.equals(that.name))
            return false;
        return !(validator != null ? !validator.equals(that.validator) : that.validator != null);
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (validator != null ? validator.hashCode() : 0);
        result = 31 * result + (index_type != null ? index_type.hashCode() : 0);
        result = 31 * result + (index_name != null ? index_name.hashCode() : 0);
        return result;
    }

    public static ColumnDefinition fromColumnDef(ColumnDef thriftColumnDef) throws ConfigurationException
    {
        AbstractType validatorType = TypeParser.parse(thriftColumnDef.validation_class);
        return new ColumnDefinition(ByteBufferUtil.clone(thriftColumnDef.name), validatorType, thriftColumnDef.index_type, thriftColumnDef.index_name);
    }
    
    public static Map<ByteBuffer, ColumnDefinition> fromColumnDef(List<ColumnDef> thriftDefs) throws ConfigurationException
    {
        if (thriftDefs == null)
            return new HashMap<ByteBuffer,ColumnDefinition>();

        Map<ByteBuffer, ColumnDefinition> cds = new TreeMap<ByteBuffer, ColumnDefinition>();
        for (ColumnDef thriftColumnDef : thriftDefs)
            cds.put(ByteBufferUtil.clone(thriftColumnDef.name), fromColumnDef(thriftColumnDef));

        return cds;
    }
    
    @Override
    public String toString()
    {
        return "ColumnDefinition{" +
               "name=" + ByteBufferUtil.bytesToHex(name) +
               ", validator=" + validator +
               ", index_type=" + index_type +
               ", index_name='" + index_name + '\'' +
               '}';
    }

    public String getIndexName()
    {
        return index_name;
    }
    
    public void setIndexName(String s)
    {
        index_name = s;
    }


    public IndexType getIndexType()
    {
        return index_type;
    }

    public void setIndexType(IndexType index_type)
    {
        this.index_type = index_type;
    }

    public AbstractType getValidator()
    {
        return validator;
    }

    public void setValidator(AbstractType validator)
    {
        this.validator = validator;
    }
}
