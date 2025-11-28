package com.langia.backend.dto;

import java.util.UUID;

import com.langia.backend.model.User;
import com.langia.backend.util.EmailMaskUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta quando usuario tenta login sem verificacao de e-mail.
 * Retornado com status HTTP 403.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingVerificationResponseDTO {

    private UUID userId;
    private String maskedEmail;
    private boolean emailVerificationRequired;
    private String message;

    /**
     * Cria resposta para verificacao pendente.
     */
    public static PendingVerificationResponseDTO fromUser(User user) {
        return PendingVerificationResponseDTO.builder()
            .userId(user.getId())
            .maskedEmail(EmailMaskUtil.mask(user.getEmail()))
            .emailVerificationRequired(true)
            .message("Por favor, verifique seu e-mail antes de fazer login.")
            .build();
    }
}
