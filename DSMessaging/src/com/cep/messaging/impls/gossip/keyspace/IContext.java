package com.cep.messaging.impls.gossip.keyspace;

import java.nio.ByteBuffer;

/**
 * An opaque commutative context.
 *
 * Maintains a ByteBuffer context that represents a partitioned commutative value.
 */
public interface IContext
{
    public static enum ContextRelationship
    {
        EQUAL,
        GREATER_THAN,
        LESS_THAN,
        DISJOINT
    };

    /**
     * Determine the relationship between two contexts.
     *
     * EQUAL:        Equal set of nodes and every count is equal.
     * GREATER_THAN: Superset of nodes and every count is equal or greater than its corollary.
     * LESS_THAN:    Subset of nodes and every count is equal or less than its corollary.
     * DISJOINT:     Node sets are not equal and/or counts are not all greater or less than.
     *
     * @param left
     *            context.
     * @param right
     *            context.
     * @return the ContextRelationship between the contexts.
     */
    public ContextRelationship diff(ByteBuffer left, ByteBuffer right);

    /**
     * Return a context w/ an aggregated count for each node id.
     *
     * @param left
     *            context.
     * @param right
     *            context.
     */
    public ByteBuffer merge(ByteBuffer left, ByteBuffer right);

    /**
     * Human-readable String from context.
     *
     * @param context
     *            context.
     * @return a human-readable String of the context.
     */
    public String toString(ByteBuffer context);
}
