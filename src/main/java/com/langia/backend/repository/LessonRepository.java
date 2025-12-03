package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Lesson;
import com.langia.backend.model.LessonType;

/**
 * Repository para entidade Lesson (lições de um módulo).
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    /**
     * Busca lições de um módulo ordenadas por índice.
     */
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(UUID moduleId);

    /**
     * Busca lição por módulo e índice de ordem.
     */
    Optional<Lesson> findByModuleIdAndOrderIndex(UUID moduleId, Integer orderIndex);

    /**
     * Conta lições de um módulo.
     */
    long countByModuleId(UUID moduleId);

    /**
     * Conta lições completas de um módulo.
     */
    long countByModuleIdAndCompletedAtIsNotNull(UUID moduleId);

    /**
     * Conta lições de uma trilha.
     */
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.trail.id = :trailId")
    long countByTrailId(@Param("trailId") UUID trailId);

    /**
     * Conta lições completas de uma trilha.
     */
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.trail.id = :trailId AND l.completedAt IS NOT NULL")
    long countCompletedByTrailId(@Param("trailId") UUID trailId);

    /**
     * Busca lições placeholder de um módulo (para geração).
     */
    List<Lesson> findByModuleIdAndIsPlaceholderTrueOrderByOrderIndexAsc(UUID moduleId);

    /**
     * Busca lições por tipo.
     */
    List<Lesson> findByModuleIdAndTypeOrderByOrderIndexAsc(UUID moduleId, LessonType type);

    /**
     * Busca próxima lição não completada de um módulo.
     */
    @Query("SELECT l FROM Lesson l " +
           "WHERE l.module.id = :moduleId " +
           "AND l.completedAt IS NULL " +
           "ORDER BY l.orderIndex ASC " +
           "LIMIT 1")
    Optional<Lesson> findNextIncompleteByModuleId(@Param("moduleId") UUID moduleId);

    /**
     * Busca próxima lição não completada de uma trilha.
     */
    @Query("SELECT l FROM Lesson l " +
           "WHERE l.module.trail.id = :trailId " +
           "AND l.completedAt IS NULL " +
           "ORDER BY l.module.orderIndex ASC, l.orderIndex ASC " +
           "LIMIT 1")
    Optional<Lesson> findNextIncompleteByTrailId(@Param("trailId") UUID trailId);

    /**
     * Marca lição como completa.
     */
    @Modifying
    @Query("UPDATE Lesson l SET l.completedAt = CURRENT_TIMESTAMP, l.score = :score, " +
           "l.timeSpentSeconds = :timeSpent, l.updatedAt = CURRENT_TIMESTAMP WHERE l.id = :id")
    void markAsCompleted(
            @Param("id") UUID id,
            @Param("score") java.math.BigDecimal score,
            @Param("timeSpent") Integer timeSpent);

    /**
     * Calcula média de score das lições completas de uma trilha.
     */
    @Query("SELECT AVG(l.score) FROM Lesson l " +
           "WHERE l.module.trail.id = :trailId " +
           "AND l.completedAt IS NOT NULL " +
           "AND l.score IS NOT NULL")
    java.math.BigDecimal calculateAverageScoreByTrailId(@Param("trailId") UUID trailId);

    /**
     * Calcula tempo total gasto em uma trilha.
     */
    @Query("SELECT COALESCE(SUM(l.timeSpentSeconds), 0) FROM Lesson l " +
           "WHERE l.module.trail.id = :trailId " +
           "AND l.timeSpentSeconds IS NOT NULL")
    Long sumTimeSpentByTrailId(@Param("trailId") UUID trailId);

    /**
     * Busca todas as lições de uma trilha.
     */
    @Query("SELECT l FROM Lesson l " +
           "WHERE l.module.trail.id = :trailId " +
           "ORDER BY l.module.orderIndex ASC, l.orderIndex ASC")
    List<Lesson> findByTrailIdOrdered(@Param("trailId") UUID trailId);

    /**
     * Remove lições de um módulo.
     */
    void deleteByModuleId(UUID moduleId);

    /**
     * Busca lições que usam um content block específico.
     */
    List<Lesson> findByContentBlockId(UUID contentBlockId);
}
