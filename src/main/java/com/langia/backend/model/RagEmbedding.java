package com.langia.backend.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade para armazenar embeddings RAG com chunking.
 * Permite busca semântica de conteúdo do currículo e trilhas.
 *
 * Diferente de DocumentEmbedding (genérico), RagEmbedding é específico
 * para entidades do módulo de trilhas com suporte a chunking.
 *
 * Corresponde à tabela rag_embeddings (V018).
 */
@Entity
@Table(name = "rag_embeddings", uniqueConstraints = {
    @UniqueConstraint(name = "uq_rag_source_chunk", columnNames = {"source_type", "source_id", "chunk_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private RagSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "chunk_index", nullable = false)
    @Builder.Default
    private Integer chunkIndex = 0;

    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Verifica se o embedding está populado.
     */
    public boolean hasEmbedding() {
        return embedding != null && embedding.length > 0;
    }

    /**
     * Retorna a dimensão do embedding.
     */
    public int getEmbeddingDimension() {
        return embedding != null ? embedding.length : 0;
    }
}
