package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para solicitação de recuperação de senha.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponseDTO {

    private boolean success;
    private String message;

    /**
     * Cria uma resposta de sucesso.
     * Nota: A mensagem é genérica para não revelar se o email existe ou não.
     */
    public static ForgotPasswordResponseDTO success() {
        return ForgotPasswordResponseDTO.builder()
            .success(true)
            .message("Se o e-mail informado estiver cadastrado, você receberá instruções para redefinir sua senha em alguns minutos.")
            .build();
    }
}
