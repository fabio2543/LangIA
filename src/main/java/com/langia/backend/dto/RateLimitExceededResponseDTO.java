package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta quando o limite de tentativas é excedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitExceededResponseDTO {

    private String error;
    private String message;
    private long retryAfter;  // segundos até poder tentar novamente

    /**
     * Cria uma resposta de rate limit excedido.
     *
     * @param retryAfterSeconds Segundos até poder tentar novamente
     */
    public static RateLimitExceededResponseDTO create(long retryAfterSeconds) {
        return RateLimitExceededResponseDTO.builder()
            .error("RATE_LIMIT_EXCEEDED")
            .message("Muitas tentativas. Aguarde alguns minutos.")
            .retryAfter(retryAfterSeconds)
            .build();
    }
}
