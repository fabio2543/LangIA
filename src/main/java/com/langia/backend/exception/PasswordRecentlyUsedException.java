package com.langia.backend.exception;

/**
 * Exceção lançada quando a senha foi utilizada recentemente.
 */
public class PasswordRecentlyUsedException extends RuntimeException {

    public PasswordRecentlyUsedException() {
        super("Password was recently used");
    }

    public PasswordRecentlyUsedException(String message) {
        super(message);
    }
}
