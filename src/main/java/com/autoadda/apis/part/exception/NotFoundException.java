package com.autoadda.apis.part.exception;

/**
 * throw exception when category not exist
 */
public class NotFoundException extends Exception {

	private String message;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
