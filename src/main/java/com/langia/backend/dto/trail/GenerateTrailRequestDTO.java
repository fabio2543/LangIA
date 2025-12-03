package com.langia.backend.dto.trail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de geração de trilha.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateTrailRequestDTO {

    @NotBlank(message = "Código do idioma é obrigatório")
    @Size(min = 2, max = 10, message = "Código do idioma deve ter entre 2 e 10 caracteres")
    private String languageCode;

    /**
     * Se true, força geração mesmo se já existir trilha.
     */
    @Builder.Default
    private Boolean forceRegenerate = false;
}
