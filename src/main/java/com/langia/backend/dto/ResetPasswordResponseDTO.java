package com.langia.backend.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para redefinição de senha.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponseDTO {

    private boolean success;
    private String message;
    private String error;
    private Map<String, List<String>> errors;

    /**
     * Cria uma resposta de sucesso.
     */
    public static ResetPasswordResponseDTO success() {
        return ResetPasswordResponseDTO.builder()
            .success(true)
            .message("Senha alterada com sucesso. Você será redirecionado para o login.")
            .build();
    }

    /**
     * Cria uma resposta para token inválido.
     */
    public static ResetPasswordResponseDTO tokenInvalid() {
        return ResetPasswordResponseDTO.builder()
            .success(false)
            .error("TOKEN_INVALID")
            .message("Este link é inválido ou expirou.")
            .build();
    }

    /**
     * Cria uma resposta para senha recentemente utilizada.
     */
    public static ResetPasswordResponseDTO passwordRecentlyUsed() {
        return ResetPasswordResponseDTO.builder()
            .success(false)
            .error("PASSWORD_RECENTLY_USED")
            .message("Esta senha foi utilizada recentemente. Escolha uma senha diferente.")
            .build();
    }

    /**
     * Cria uma resposta para erros de validação.
     *
     * @param errors Mapa de campo -> lista de erros
     */
    public static ResetPasswordResponseDTO validationError(Map<String, List<String>> errors) {
        return ResetPasswordResponseDTO.builder()
            .success(false)
            .error("VALIDATION_ERROR")
            .errors(errors)
            .build();
    }
}
