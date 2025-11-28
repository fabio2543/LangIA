package com.langia.backend.exception;

import lombok.Getter;

/**
 * Exceção lançada quando o limite de tentativas é excedido.
 */
@Getter
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super("Rate limit exceeded");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
