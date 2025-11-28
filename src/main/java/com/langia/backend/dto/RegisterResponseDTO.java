package com.langia.backend.dto;

import java.util.UUID;

import com.langia.backend.model.User;
import com.langia.backend.util.EmailMaskUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para registro de usuario.
 * Indica que a verificacao de e-mail e necessaria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDTO {

    private UUID userId;
    private String name;
    private String maskedEmail;
    private boolean emailVerificationRequired;
    private String message;

    /**
     * Cria resposta indicando verificacao pendente.
     */
    public static RegisterResponseDTO pendingVerification(User user) {
        return RegisterResponseDTO.builder()
            .userId(user.getId())
            .name(user.getName())
            .maskedEmail(EmailMaskUtil.mask(user.getEmail()))
            .emailVerificationRequired(true)
            .message("Conta criada com sucesso. Verifique seu e-mail para ativar.")
            .build();
    }
}
