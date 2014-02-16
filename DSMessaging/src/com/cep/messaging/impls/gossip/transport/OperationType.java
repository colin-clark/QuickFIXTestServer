package com.cep.messaging.impls.gossip.transport;

/**
 * Streaming operation type.
 */
public enum OperationType
{
    AES,
    BOOTSTRAP,
    UNBOOTSTRAP,
    RESTORE_REPLICA_COUNT,
    BULK_LOAD;
}

