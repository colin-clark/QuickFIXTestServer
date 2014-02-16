package com.cep.messaging.impls.gossip.serialization;

public interface ICompactSerializer3<T> extends ICompactSerializer2<T>
{
    public long serializedSize(T t);
}
