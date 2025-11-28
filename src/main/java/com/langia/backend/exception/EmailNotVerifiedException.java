package com.langia.backend.exception;

import java.util.UUID;

import lombok.Getter;

/**
 * Excecao lancada quando usuario tenta login sem verificacao de e-mail.
 */
@Getter
public class EmailNotVerifiedException extends RuntimeException {

    private final UUID userId;
    private final String maskedEmail;

    public EmailNotVerifiedException(UUID userId, String maskedEmail) {
        super("Email not verified");
        this.userId = userId;
        this.maskedEmail = maskedEmail;
    }
}
