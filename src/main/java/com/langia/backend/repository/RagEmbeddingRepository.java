package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.RagEmbedding;
import com.langia.backend.model.RagSourceType;

/**
 * Repository para RagEmbedding com suporte a busca vetorial.
 * Específico para embeddings do módulo de trilhas.
 */
@Repository
public interface RagEmbeddingRepository extends JpaRepository<RagEmbedding, UUID> {

    /**
     * Busca embeddings similares usando distância cosseno.
     * A query usa o operador <=> do pgvector para calcular distância.
     *
     * @param queryVector vetor de embedding da query (como string formatada)
     * @param sourceType tipo de fonte para filtrar (opcional, null para todos)
     * @param limit número máximo de resultados
     * @return lista de embeddings ordenados por similaridade
     */
    @Query(value = """
            SELECT * FROM rag_embeddings
            WHERE (:sourceType IS NULL OR source_type = :sourceType)
            AND embedding IS NOT NULL
            ORDER BY embedding <=> cast(:queryVector as vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<RagEmbedding> findSimilar(
            @Param("queryVector") String queryVector,
            @Param("sourceType") String sourceType,
            @Param("limit") int limit);

    /**
     * Busca embeddings similares com filtro de distância mínima.
     *
     * @param queryVector vetor de embedding da query
     * @param sourceType tipo de fonte
     * @param maxDistance distância máxima (0-2 para cosseno, onde 0 = idêntico)
     * @param limit número máximo de resultados
     * @return lista de embeddings
     */
    @Query(value = """
            SELECT * FROM rag_embeddings
            WHERE (:sourceType IS NULL OR source_type = :sourceType)
            AND embedding IS NOT NULL
            AND (embedding <=> cast(:queryVector as vector)) < :maxDistance
            ORDER BY embedding <=> cast(:queryVector as vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<RagEmbedding> findSimilarWithThreshold(
            @Param("queryVector") String queryVector,
            @Param("sourceType") String sourceType,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);

    /**
     * Busca embeddings por fonte (tipo + id).
     */
    List<RagEmbedding> findBySourceTypeAndSourceIdOrderByChunkIndexAsc(
            RagSourceType sourceType, UUID sourceId);

    /**
     * Busca embedding específico por fonte e chunk.
     */
    Optional<RagEmbedding> findBySourceTypeAndSourceIdAndChunkIndex(
            RagSourceType sourceType, UUID sourceId, Integer chunkIndex);

    /**
     * Busca embeddings por tipo de fonte.
     */
    List<RagEmbedding> findBySourceTypeOrderByCreatedAtDesc(RagSourceType sourceType);

    /**
     * Busca embeddings por modelo usado.
     */
    List<RagEmbedding> findByModelOrderByCreatedAtDesc(String model);

    /**
     * Conta embeddings por tipo de fonte.
     */
    long countBySourceType(RagSourceType sourceType);

    /**
     * Conta embeddings por modelo.
     */
    long countByModel(String model);

    /**
     * Verifica se existe embedding para uma fonte.
     */
    boolean existsBySourceTypeAndSourceId(RagSourceType sourceType, UUID sourceId);

    /**
     * Remove embeddings de uma fonte específica.
     */
    @Modifying
    @Query("DELETE FROM RagEmbedding r WHERE r.sourceType = :sourceType AND r.sourceId = :sourceId")
    int deleteBySource(@Param("sourceType") RagSourceType sourceType, @Param("sourceId") UUID sourceId);

    /**
     * Busca embeddings que contêm texto específico.
     */
    @Query("SELECT r FROM RagEmbedding r WHERE LOWER(r.contentText) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<RagEmbedding> findByContentTextContaining(@Param("searchTerm") String searchTerm);

    /**
     * Busca embeddings sem vetor (para processamento pendente).
     */
    @Query("SELECT r FROM RagEmbedding r WHERE r.embedding IS NULL ORDER BY r.createdAt ASC")
    List<RagEmbedding> findWithoutEmbedding();

    /**
     * Conta total de chunks de uma fonte.
     */
    @Query("SELECT COUNT(r) FROM RagEmbedding r WHERE r.sourceType = :sourceType AND r.sourceId = :sourceId")
    long countChunksForSource(@Param("sourceType") RagSourceType sourceType, @Param("sourceId") UUID sourceId);
}
