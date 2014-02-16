package com.cep.messaging.impls.gossip.keyspace;

/**
 * column family type enum
 */
public enum ColumnFamilyType
{
    Standard,
    Super;

    public static ColumnFamilyType create(String name)
    {
        try
        {
            // TODO thrift optional parameter in CfDef is leaking down here which it shouldn't
            return name == null ? ColumnFamilyType.Standard : ColumnFamilyType.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
