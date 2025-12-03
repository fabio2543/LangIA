package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.ContentBlock;
import com.langia.backend.model.LessonType;

/**
 * Repository para entidade ContentBlock (blocos de conteúdo reutilizáveis).
 */
@Repository
public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {

    /**
     * Busca bloco de conteúdo pelo hash SHA-1.
     */
    Optional<ContentBlock> findByContentHash(String contentHash);

    /**
     * Verifica se existe bloco com o hash especificado.
     */
    boolean existsByContentHash(String contentHash);

    /**
     * Busca blocos aprovados por descritor e idioma.
     */
    @Query("SELECT cb FROM ContentBlock cb " +
           "WHERE cb.descriptor.id = :descriptorId " +
           "AND cb.language.code = :languageCode " +
           "AND cb.isApproved = true " +
           "ORDER BY cb.qualityScore DESC NULLS LAST, cb.usageCount DESC")
    List<ContentBlock> findApprovedByDescriptorAndLanguage(
            @Param("descriptorId") UUID descriptorId,
            @Param("languageCode") String languageCode);

    /**
     * Busca blocos aprovados por descritor, idioma e tipo.
     */
    @Query("SELECT cb FROM ContentBlock cb " +
           "WHERE cb.descriptor.id = :descriptorId " +
           "AND cb.language.code = :languageCode " +
           "AND cb.type = :type " +
           "AND cb.isApproved = true " +
           "ORDER BY cb.qualityScore DESC NULLS LAST, cb.usageCount DESC")
    List<ContentBlock> findApprovedByDescriptorLanguageAndType(
            @Param("descriptorId") UUID descriptorId,
            @Param("languageCode") String languageCode,
            @Param("type") LessonType type);

    /**
     * Busca blocos não aprovados (pendentes de revisão).
     */
    List<ContentBlock> findByIsApprovedFalseOrderByCreatedAtDesc();

    /**
     * Busca blocos por fonte de geração.
     */
    List<ContentBlock> findByGenerationSourceOrderByCreatedAtDesc(String generationSource);

    /**
     * Incrementa contador de uso de um bloco.
     */
    @Modifying
    @Query("UPDATE ContentBlock cb SET cb.usageCount = cb.usageCount + 1 WHERE cb.id = :id")
    void incrementUsageCount(@Param("id") UUID id);

    /**
     * Busca blocos mais usados.
     */
    @Query("SELECT cb FROM ContentBlock cb WHERE cb.isApproved = true ORDER BY cb.usageCount DESC")
    List<ContentBlock> findMostUsed();

    /**
     * Calcula total de tokens usados em um período.
     */
    @Query("SELECT COALESCE(SUM(cb.tokensUsed), 0) FROM ContentBlock cb " +
           "WHERE cb.createdAt >= :startDate")
    Long sumTokensUsedSince(@Param("startDate") java.time.LocalDateTime startDate);

    /**
     * Busca blocos por modelo LLM.
     */
    List<ContentBlock> findByLlmModelOrderByCreatedAtDesc(String llmModel);
}
