package com.langia.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.DocumentEmbedding;

/**
 * Repositório para DocumentEmbedding com suporte a busca vetorial.
 */
@Repository
public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, UUID> {

    /**
     * Busca documentos similares usando distância cosseno.
     * A query usa o operador <=> do pgvector para calcular distância.
     *
     * @param queryVector vetor de embedding da query (como string formatada)
     * @param contentType tipo de conteúdo para filtrar (opcional, null para todos)
     * @param limit número máximo de resultados
     * @return lista de documentos ordenados por similaridade
     */
    @Query(value = """
            SELECT * FROM document_embeddings
            WHERE (:contentType IS NULL OR content_type = :contentType)
            AND embedding IS NOT NULL
            ORDER BY embedding <=> cast(:queryVector as vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentEmbedding> findSimilar(
            @Param("queryVector") String queryVector,
            @Param("contentType") String contentType,
            @Param("limit") int limit);

    /**
     * Busca documentos similares com filtro de distância mínima.
     *
     * @param queryVector vetor de embedding da query
     * @param contentType tipo de conteúdo
     * @param maxDistance distância máxima (0-2 para cosseno, onde 0 = idêntico)
     * @param limit número máximo de resultados
     * @return lista de documentos
     */
    @Query(value = """
            SELECT * FROM document_embeddings
            WHERE (:contentType IS NULL OR content_type = :contentType)
            AND embedding IS NOT NULL
            AND (embedding <=> cast(:queryVector as vector)) < :maxDistance
            ORDER BY embedding <=> cast(:queryVector as vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentEmbedding> findSimilarWithThreshold(
            @Param("queryVector") String queryVector,
            @Param("contentType") String contentType,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);

    /**
     * Busca documentos por tipo de conteúdo.
     */
    List<DocumentEmbedding> findByContentType(String contentType);

    /**
     * Conta documentos por tipo.
     */
    long countByContentType(String contentType);

    /**
     * Busca documentos que contenham texto específico no conteúdo.
     */
    @Query("SELECT d FROM DocumentEmbedding d WHERE LOWER(d.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentEmbedding> findByContentContaining(@Param("searchTerm") String searchTerm);
}
