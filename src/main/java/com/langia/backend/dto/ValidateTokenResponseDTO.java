package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para validação de token de reset.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponseDTO {

    private boolean valid;
    private String email;  // Mascarado: u***@e***.com
    private String error;
    private String message;

    /**
     * Cria uma resposta para token válido.
     *
     * @param maskedEmail Email mascarado do usuário
     */
    public static ValidateTokenResponseDTO valid(String maskedEmail) {
        return ValidateTokenResponseDTO.builder()
            .valid(true)
            .email(maskedEmail)
            .build();
    }

    /**
     * Cria uma resposta para token inválido ou expirado.
     */
    public static ValidateTokenResponseDTO invalid() {
        return ValidateTokenResponseDTO.builder()
            .valid(false)
            .error("TOKEN_INVALID")
            .message("Este link é inválido ou expirou. Solicite uma nova recuperação de senha.")
            .build();
    }
}
