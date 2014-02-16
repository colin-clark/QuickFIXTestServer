package com.cep.messaging.util.exception;

public class UnavailableException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnavailableException(String message) {
		super(message);
	}

	public UnavailableException(String message, Exception e) {
		super(message, e);
	}

	public UnavailableException() {
		super();
	}

}
