package com.cep.messaging.impls.gossip.keyspace.marshal;

public class MarshalException extends RuntimeException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MarshalException(String message)
    {
        super(message);
    }

    public MarshalException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
