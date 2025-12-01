package com.autoadda.apis.part.exception;

/**
 * throw exception when username not exist
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException() {
        super();
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
