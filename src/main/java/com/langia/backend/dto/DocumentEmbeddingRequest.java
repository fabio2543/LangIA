package com.langia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de criação de documento com embedding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEmbeddingRequest {

    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(max = 50000, message = "Conteúdo não pode exceder 50000 caracteres")
    private String content;

    @NotBlank(message = "Tipo de conteúdo é obrigatório")
    @Size(max = 50, message = "Tipo de conteúdo não pode exceder 50 caracteres")
    private String contentType;

    private String metadata;
}
