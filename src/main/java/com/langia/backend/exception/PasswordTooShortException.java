package com.langia.backend.exception;

public class PasswordTooShortException extends RuntimeException {

    public PasswordTooShortException(String message) {
        super(message);
    }

    public PasswordTooShortException(String message, Throwable cause) {
        super(message, cause);
    }
}

