package com.cep.messaging.impls.gossip.keyspace.filter;

import com.cep.messaging.impls.gossip.keyspace.SuperColumn;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class IdentityQueryFilter extends SliceQueryFilter
{
    /**
     * Only for use in testing; will read entire CF into memory.
     */
    public IdentityQueryFilter()
    {
        super(ByteBufferUtil.EMPTY_BYTE_BUFFER, ByteBufferUtil.EMPTY_BYTE_BUFFER, false, Integer.MAX_VALUE);
    }

    public SuperColumn filterSuperColumn(SuperColumn superColumn, int gcBefore)
    {
        // no filtering done, deliberately
        return superColumn;
    }
}
