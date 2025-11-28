package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para confirmacao de e-mail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponseDTO {

    private boolean success;
    private String error;
    private String message;

    /**
     * Cria resposta de sucesso.
     */
    public static EmailVerificationResponseDTO success() {
        return EmailVerificationResponseDTO.builder()
            .success(true)
            .message("E-mail confirmado com sucesso! Sua conta esta ativa.")
            .build();
    }

    /**
     * Cria resposta para token invalido.
     */
    public static EmailVerificationResponseDTO tokenInvalid() {
        return EmailVerificationResponseDTO.builder()
            .success(false)
            .error("TOKEN_INVALID")
            .message("Link de verificacao invalido.")
            .build();
    }

    /**
     * Cria resposta para token expirado.
     */
    public static EmailVerificationResponseDTO tokenExpired() {
        return EmailVerificationResponseDTO.builder()
            .success(false)
            .error("TOKEN_EXPIRED")
            .message("Link de verificacao expirado. Solicite um novo e-mail.")
            .build();
    }

    /**
     * Cria resposta para token ja utilizado.
     */
    public static EmailVerificationResponseDTO tokenUsed() {
        return EmailVerificationResponseDTO.builder()
            .success(false)
            .error("TOKEN_USED")
            .message("Este link ja foi utilizado.")
            .build();
    }
}
