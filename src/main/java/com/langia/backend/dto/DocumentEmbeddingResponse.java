package com.langia.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.langia.backend.model.DocumentEmbedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de documento com embedding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEmbeddingResponse {

    private UUID id;
    private String content;
    private String contentType;
    private String metadata;
    private boolean hasEmbedding;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Converte entidade para DTO.
     */
    public static DocumentEmbeddingResponse fromEntity(DocumentEmbedding entity) {
        return DocumentEmbeddingResponse.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .contentType(entity.getContentType())
                .metadata(entity.getMetadata())
                .hasEmbedding(entity.getEmbedding() != null && entity.getEmbedding().length > 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
