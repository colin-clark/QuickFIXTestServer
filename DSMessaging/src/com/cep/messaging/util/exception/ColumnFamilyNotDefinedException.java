package com.cep.messaging.util.exception;

import com.cep.messaging.impls.gossip.thrift.InvalidRequestException;

public class ColumnFamilyNotDefinedException extends InvalidRequestException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ColumnFamilyNotDefinedException(String message)
    {
        super(message);
    }
}
