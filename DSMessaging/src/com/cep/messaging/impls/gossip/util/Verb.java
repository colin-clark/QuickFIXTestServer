package com.cep.messaging.impls.gossip.util;

public enum Verb
{
    MUTATION,
    BINARY, // Deprecated
    READ_REPAIR,
    READ,
    REQUEST_RESPONSE, // client-initiated reads and writes
    STREAM_INITIATE, // Deprecated
    STREAM_INITIATE_DONE, // Deprecated
    STREAM_REPLY,
    STREAM_REQUEST,
    RANGE_SLICE,
    BOOTSTRAP_TOKEN,
    TREE_REQUEST,
    TREE_RESPONSE,
    JOIN, // Deprecated
    GOSSIP_DIGEST_SYN,
    GOSSIP_DIGEST_ACK,
    GOSSIP_DIGEST_ACK2,
    DEFINITIONS_ANNOUNCE, // Deprecated
    DEFINITIONS_UPDATE,
    TRUNCATE,
    SCHEMA_CHECK,
    INDEX_SCAN,
    REPLICATION_FINISHED,
    INTERNAL_RESPONSE, // responses to internal calls
    COUNTER_MUTATION,
    // use as padding for backwards compatability where a previous version needs to validate a verb from the future.
    SAVE,
    SEND,
    UNUSED_3,
    ;
    // remember to add new verbs at the end, since we serialize by ordinal
}
