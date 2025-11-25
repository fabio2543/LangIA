package com.langia.backend.exception;

/**
 * Exceção lançada quando a sessão não é encontrada no Redis ou está expirada.
 */
public class InvalidSessionException extends RuntimeException {

    public InvalidSessionException() {
        super("Session not found or expired");
    }

    public InvalidSessionException(String message) {
        super(message);
    }
}
