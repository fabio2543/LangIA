package com.langia.backend.exception;

import lombok.Getter;

/**
 * Excecao lancada quando um token de verificacao de e-mail e invalido.
 */
@Getter
public class InvalidVerificationTokenException extends RuntimeException {

    /**
     * Codigo do erro: TOKEN_INVALID, TOKEN_EXPIRED, TOKEN_USED
     */
    private final String errorCode;

    public InvalidVerificationTokenException(String errorCode) {
        super("Invalid verification token: " + errorCode);
        this.errorCode = errorCode;
    }

    public static InvalidVerificationTokenException invalid() {
        return new InvalidVerificationTokenException("TOKEN_INVALID");
    }

    public static InvalidVerificationTokenException expired() {
        return new InvalidVerificationTokenException("TOKEN_EXPIRED");
    }

    public static InvalidVerificationTokenException used() {
        return new InvalidVerificationTokenException("TOKEN_USED");
    }
}
