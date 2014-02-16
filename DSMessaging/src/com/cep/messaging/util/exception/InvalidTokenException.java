package com.cep.messaging.util.exception;

public class InvalidTokenException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public InvalidTokenException(String message) {
		super(message);
	}

	public InvalidTokenException(String message, Exception e) {
		super(message, e);
	}

	public InvalidTokenException() {
		super();
	}

}
