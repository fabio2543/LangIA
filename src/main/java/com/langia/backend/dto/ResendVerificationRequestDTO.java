package com.langia.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requisicao para reenvio de e-mail de verificacao.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationRequestDTO {

    @NotNull(message = "O ID do usuario e obrigatorio")
    private UUID userId;
}
