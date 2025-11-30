package com.langia.backend.exception;

/**
 * Exception thrown when an email change verification code is invalid or expired.
 */
public class InvalidEmailChangeCodeException extends RuntimeException {

    public InvalidEmailChangeCodeException(String message) {
        super(message);
    }

    public InvalidEmailChangeCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
