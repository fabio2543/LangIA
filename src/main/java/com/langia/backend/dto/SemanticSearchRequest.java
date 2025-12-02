package com.langia.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de busca semântica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticSearchRequest {

    @NotBlank(message = "Query é obrigatória")
    private String query;

    private String contentType;

    @Min(value = 1, message = "Limite mínimo é 1")
    @Max(value = 100, message = "Limite máximo é 100")
    @Builder.Default
    private int limit = 10;

    @Min(value = 0, message = "Distância mínima é 0")
    @Max(value = 2, message = "Distância máxima é 2")
    private Double maxDistance;
}
