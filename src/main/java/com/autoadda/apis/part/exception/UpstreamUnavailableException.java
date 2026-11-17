package com.autoadda.apis.part.exception;

/** Another AutoAdda service needed for the request did not answer (503). */
public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(String message) {
        super(message);
    }
}
