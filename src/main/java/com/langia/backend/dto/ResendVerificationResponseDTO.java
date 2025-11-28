package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para reenvio de e-mail de verificacao.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationResponseDTO {

    private boolean success;
    private String maskedEmail;
    private String message;
    private Integer remainingResends;
    private Long retryAfterSeconds;

    /**
     * Cria resposta de sucesso.
     */
    public static ResendVerificationResponseDTO success(String maskedEmail, int remainingResends) {
        return ResendVerificationResponseDTO.builder()
            .success(true)
            .maskedEmail(maskedEmail)
            .message("E-mail de verificacao reenviado com sucesso.")
            .remainingResends(remainingResends)
            .build();
    }

    /**
     * Cria resposta quando rate limit foi excedido.
     */
    public static ResendVerificationResponseDTO rateLimited(long retryAfterSeconds) {
        return ResendVerificationResponseDTO.builder()
            .success(false)
            .message("Limite de reenvios atingido. Tente novamente mais tarde.")
            .retryAfterSeconds(retryAfterSeconds)
            .build();
    }

    /**
     * Cria resposta quando e-mail ja foi verificado.
     */
    public static ResendVerificationResponseDTO alreadyVerified() {
        return ResendVerificationResponseDTO.builder()
            .success(false)
            .message("E-mail ja verificado.")
            .build();
    }
}
