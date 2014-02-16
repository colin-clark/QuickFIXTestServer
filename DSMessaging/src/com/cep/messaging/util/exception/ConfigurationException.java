package com.cep.messaging.util.exception;

public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Exception e) {
		super(message, e);
	}
}
