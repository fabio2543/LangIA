package com.langia.backend.exception;

/**
 * Exceção lançada quando um token de reset é inválido ou expirado.
 */
public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Invalid or expired reset token");
    }

    public InvalidResetTokenException(String message) {
        super(message);
    }
}
