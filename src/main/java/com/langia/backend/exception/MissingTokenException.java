package com.langia.backend.exception;

/**
 * Exceção lançada quando o header Authorization está ausente ou com formato inválido.
 * Esperado formato: "Bearer <token>"
 */
public class MissingTokenException extends RuntimeException {

    public MissingTokenException() {
        super("Missing or invalid Authorization header");
    }

    public MissingTokenException(String message) {
        super(message);
    }
}
