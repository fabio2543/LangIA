package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.model.Blueprint;

/**
 * Repository para entidade Blueprint (templates de trilha).
 */
@Repository
public interface BlueprintRepository extends JpaRepository<Blueprint, UUID> {

    /**
     * Busca blueprints aprovados para um idioma e nível.
     */
    @Query("SELECT b FROM Blueprint b " +
           "WHERE b.language.code = :languageCode " +
           "AND b.level.code = :levelCode " +
           "AND b.isApproved = true " +
           "ORDER BY b.usageCount DESC, b.avgCompletionRate DESC NULLS LAST")
    List<Blueprint> findApprovedByLanguageAndLevel(
            @Param("languageCode") String languageCode,
            @Param("levelCode") String levelCode);

    /**
     * Busca blueprint mais adequado baseado em padrão de preferências (JSON).
     * Usa operador de contenção do PostgreSQL.
     */
    @Query(value = "SELECT b.* FROM blueprints b " +
                   "WHERE b.language_code = :languageCode " +
                   "AND b.level_id = :levelId " +
                   "AND b.is_approved = true " +
                   "AND b.preferences_pattern @> CAST(:preferencesJson AS jsonb) " +
                   "ORDER BY b.usage_count DESC, b.avg_completion_rate DESC NULLS LAST " +
                   "LIMIT 1",
           nativeQuery = true)
    Optional<Blueprint> findMatchingBlueprint(
            @Param("languageCode") String languageCode,
            @Param("levelId") UUID levelId,
            @Param("preferencesJson") String preferencesJson);

    /**
     * Busca blueprints não aprovados (pendentes de revisão).
     */
    List<Blueprint> findByIsApprovedFalseOrderByCreatedAtDesc();

    /**
     * Busca blueprints criados por um usuário.
     */
    List<Blueprint> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);

    /**
     * Busca blueprints mais usados.
     */
    @Query("SELECT b FROM Blueprint b WHERE b.isApproved = true ORDER BY b.usageCount DESC")
    List<Blueprint> findMostUsed();

    /**
     * Incrementa contador de uso de um blueprint.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Blueprint b SET b.usageCount = b.usageCount + 1 WHERE b.id = :id")
    void incrementUsageCount(@Param("id") UUID id);

    /**
     * Busca blueprint pelo nome e idioma.
     */
    Optional<Blueprint> findByNameAndLanguageCode(String name, String languageCode);
}
