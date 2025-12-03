package com.langia.backend.exception;

/**
 * Exceção lançada quando o limite de trilhas ativas é excedido.
 */
public class TrailLimitExceededException extends RuntimeException {

    private static final int DEFAULT_LIMIT = 3;

    public TrailLimitExceededException() {
        super("Limite de " + DEFAULT_LIMIT + " trilhas ativas atingido. Arquive uma trilha existente para criar uma nova.");
    }

    public TrailLimitExceededException(int limit) {
        super("Limite de " + limit + " trilhas ativas atingido. Arquive uma trilha existente para criar uma nova.");
    }

    public TrailLimitExceededException(String message) {
        super(message);
    }
}
