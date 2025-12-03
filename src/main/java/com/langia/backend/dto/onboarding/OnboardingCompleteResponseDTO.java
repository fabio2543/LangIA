package com.langia.backend.dto.onboarding;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta quando o onboarding é completado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingCompleteResponseDTO {

    /**
     * Indica que o onboarding foi completado com sucesso.
     */
    private boolean success;

    /**
     * Mensagem de status.
     */
    private String message;

    /**
     * ID da trilha gerada automaticamente.
     */
    private UUID trailId;

    /**
     * URL para onde redirecionar o usuário.
     */
    private String redirectUrl;

    /**
     * Código do idioma da trilha gerada.
     */
    private String languageCode;
}
