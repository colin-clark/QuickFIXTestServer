package com.cep.utils;


public class ConfigurationException extends java.lang.Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigurationException(Throwable e) {
		super(e);
	}

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Exception e) {
		super(message, e);
	}

}
