package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ICompactSerializer2<T> {
	/**
	 * Serialize the specified type into the specified DataOutput instance.
	 * 
	 * @param t
	 *            type that needs to be serialized
	 * @param dos
	 *            DataOutput into which serialization needs to happen.
	 * @throws IOException
	 */
	public void serialize(T t, DataOutput dos) throws IOException;

	/**
	 * Deserialize from the specified DataInput instance.
	 * 
	 * @param dis
	 *            DataInput from which deserialization needs to happen.
	 * @throws IOException
	 * @return the type that was deserialized
	 */
	public T deserialize(DataInput dis) throws IOException;
}
