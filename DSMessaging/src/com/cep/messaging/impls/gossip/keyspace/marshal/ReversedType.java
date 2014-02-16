package com.cep.messaging.impls.gossip.keyspace.marshal;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cep.messaging.util.exception.ConfigurationException;

@SuppressWarnings({"rawtypes","unchecked"})
public class ReversedType<T> extends AbstractType<T>
{
    // interning instances
	private static final Map<AbstractType, ReversedType> instances = new HashMap<AbstractType, ReversedType>();

    // package protected for unit tests sake
    final AbstractType<T> baseType;

    public static <T> ReversedType<T> getInstance(TypeParser parser) throws ConfigurationException
    {
        List<AbstractType> types = parser.getTypeParameters();
        if (types.size() != 1)
            throw new ConfigurationException("ReversedType takes exactly one argument, " + types.size() + " given");
        return getInstance(types.get(0));
    }

    public static synchronized <T> ReversedType<T> getInstance(AbstractType<T> baseType)
    {
        ReversedType type = instances.get(baseType);
        if (type == null)
        {
            type = new ReversedType(baseType);
            instances.put(baseType, type);
        }
        return (ReversedType<T>) type;
    }

    private ReversedType(AbstractType<T> baseType)
    {
        this.baseType = baseType;
    }

    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        return -baseType.compare(o1, o2);
    }

    public String getString(ByteBuffer bytes)
    {
        return baseType.getString(bytes);
    }

    public ByteBuffer fromString(String source)
    {
        return baseType.fromString(source);
    }

    public void validate(ByteBuffer bytes) throws MarshalException
    {
        baseType.validate(bytes);
    }

    public T compose(ByteBuffer bytes)
    {
        return baseType.compose(bytes);
    }

    public ByteBuffer decompose(T value)
    {
        return baseType.decompose(value);
    }

    public Class<T> getType()
    {
        return baseType.getType();
    }

    public String toString(T t)
    {
        return baseType.toString(t);
    }

    public boolean isSigned()
    {
        return baseType.isSigned();
    }

    public boolean isCaseSensitive()
    {
        return baseType.isCaseSensitive();
    }

    public boolean isCurrency()
    {
        return baseType.isCurrency();
    }

    public int getPrecision(T obj)
    {
        return baseType.getPrecision(obj);
    }

    public int getScale(T obj)
    {
        return baseType.getScale(obj);
    }

    public int getJdbcType()
    {
        return baseType.getJdbcType();
    }

    public boolean needsQuotes()
    {
        return baseType.needsQuotes();
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "(" + baseType + ")";
    }
}
