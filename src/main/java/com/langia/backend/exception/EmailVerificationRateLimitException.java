package com.langia.backend.exception;

import lombok.Getter;

/**
 * Excecao lancada quando o limite de reenvios de verificacao e excedido.
 */
@Getter
public class EmailVerificationRateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public EmailVerificationRateLimitException(long retryAfterSeconds) {
        super("Email verification rate limit exceeded");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
