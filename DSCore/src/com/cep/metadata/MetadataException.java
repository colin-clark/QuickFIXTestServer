package com.cep.metadata;

public class MetadataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MetadataException(Throwable e) {
		super(e);
	}

	public MetadataException(String message) {
		super(message);
	}

	public MetadataException(String message, Exception e) {
		super(message, e);
	}

}
