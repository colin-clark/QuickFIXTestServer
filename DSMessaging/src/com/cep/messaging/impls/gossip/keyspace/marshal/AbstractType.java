package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.IndexHelper.IndexInfo;
import com.cep.messaging.util.exception.ConfigurationException;

/**
 * Specifies a Comparator for a specific type of ByteBuffer.
 *
 * Note that empty ByteBuffer are used to represent "start at the beginning"
 * or "stop at the end" arguments to get_slice, so the Comparator
 * should always handle those values even if they normally do not
 * represent a valid ByteBuffer for the type being compared.
 */
public abstract class AbstractType<T> implements Comparator<ByteBuffer>
{
    public final Comparator<IndexInfo> indexComparator;
    public final Comparator<IndexInfo> indexReverseComparator;
    public final Comparator<IColumn> columnComparator;
    public final Comparator<IColumn> columnReverseComparator;
    public final Comparator<ByteBuffer> reverseComparator;

    protected AbstractType()
    {
        indexComparator = new Comparator<IndexInfo>()
        {
            public int compare(IndexInfo o1, IndexInfo o2)
            {
                return AbstractType.this.compare(o1.lastName, o2.lastName);
            }
        };
        indexReverseComparator = new Comparator<IndexInfo>()
        {
            public int compare(IndexInfo o1, IndexInfo o2)
            {
                return AbstractType.this.compare(o1.firstName, o2.firstName);
            }
        };
        columnComparator = new Comparator<IColumn>()
        {
            public int compare(IColumn c1, IColumn c2)
            {
                return AbstractType.this.compare(c1.name(), c2.name());
            }
        };
        columnReverseComparator = new Comparator<IColumn>()
        {
            public int compare(IColumn c1, IColumn c2)
            {
                return AbstractType.this.compare(c2.name(), c1.name());
            }
        };
        reverseComparator = new Comparator<ByteBuffer>()
        {
            public int compare(ByteBuffer o1, ByteBuffer o2)
            {
                if (o1.remaining() == 0)
                {
                    return o2.remaining() == 0 ? 0 : -1;
                }
                if (o2.remaining() == 0)
                {
                    return 1;
                }

                return -AbstractType.this.compare(o1, o2);
            }
        };
    }
    
    public abstract T compose(ByteBuffer bytes);
    
    public abstract ByteBuffer decompose(T value);

    /** get a string representation of a particular type. */
    public abstract String toString(T t);

    /** get a string representation of the bytes suitable for log messages */
    public abstract String getString(ByteBuffer bytes);

    /** get a byte representation of the given string.
     *  defaults to unsupportedoperation so people deploying custom Types can update at their leisure. */
    public ByteBuffer fromString(String source) throws MarshalException
    {
        throw new UnsupportedOperationException();
    }

    /* validate that the byte array is a valid sequence for the type we are supposed to be comparing */
    public abstract void validate(ByteBuffer bytes) throws MarshalException;

    /** @deprecated; use reverseComparator field instead */
    public Comparator<ByteBuffer> getReverseComparator()
    {
        return reverseComparator;
    }

    /* convenience method */
    public String getString(Collection<ByteBuffer> names)
    {
        StringBuilder builder = new StringBuilder();
        for (ByteBuffer name : names)
        {
            builder.append(getString(name)).append(",");
        }
        return builder.toString();
    }

    /* convenience method */
    public String getColumnsString(Collection<IColumn> columns)
    {
        StringBuilder builder = new StringBuilder();
        for (IColumn column : columns)
        {
            builder.append(column.getString(this)).append(",");
        }
        return builder.toString();
    }

    public boolean isCommutative()
    {
        return false;
    }
    
    /** returns the class this AbstractType represents. */
    public abstract Class<T> getType();

    //
    // JDBC metadata
    //

    public abstract boolean isSigned();
    public abstract boolean isCaseSensitive();
    public abstract boolean isCurrency();
    public abstract int getPrecision(T obj);
    public abstract int getScale(T obj);
    public abstract int getJdbcType();
    public abstract boolean needsQuotes();

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static AbstractType parseDefaultParameters(AbstractType baseType, TypeParser parser) throws ConfigurationException
    {
        Map<String, String> parameters = parser.getKeyValueParameters();
        String reversed = parameters.get("reversed");
        if (reversed != null && (reversed.isEmpty() || reversed.equals("true")))
        {
            return ReversedType.getInstance(baseType);
        }
        else
        {
            return baseType;
        }
    }

    /**
     * This must be overriden by subclasses if necessary so that for any
     * AbstractType, this == TypeParser.parse(toString()).
     */
    @Override
    public String toString()
    {
        return getClass().getName();
    }
}
