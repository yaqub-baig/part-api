package com.autoadda.apis.part.exception;

/**
 * throw exception when category not exist
 */
public class NotFoundException extends RuntimeException {


    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
