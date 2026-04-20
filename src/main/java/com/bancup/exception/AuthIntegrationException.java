package com.bancup.exception;

public class AuthIntegrationException extends RuntimeException {

    public AuthIntegrationException(String message) {
        super(message);
    }

    public AuthIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
