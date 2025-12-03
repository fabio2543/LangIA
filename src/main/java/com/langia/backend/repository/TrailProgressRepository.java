package com.langia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.TrailProgress;

/**
 * Repository para entidade TrailProgress (progresso consolidado de uma trilha).
 */
@Repository
public interface TrailProgressRepository extends JpaRepository<TrailProgress, UUID> {

    /**
     * Busca progresso por trilha.
     */
    Optional<TrailProgress> findByTrailId(UUID trailId);

    /**
     * Verifica se existe progresso para a trilha.
     */
    boolean existsByTrailId(UUID trailId);

    /**
     * Atualiza progresso de uma trilha.
     */
    @Modifying
    @Query("UPDATE TrailProgress tp SET " +
           "tp.lessonsCompleted = :lessonsCompleted, " +
           "tp.progressPercentage = :progressPercentage, " +
           "tp.averageScore = :averageScore, " +
           "tp.timeSpentMinutes = :timeSpentMinutes, " +
           "tp.lastActivityAt = CURRENT_TIMESTAMP, " +
           "tp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE tp.trail.id = :trailId")
    void updateProgress(
            @Param("trailId") UUID trailId,
            @Param("lessonsCompleted") Integer lessonsCompleted,
            @Param("progressPercentage") java.math.BigDecimal progressPercentage,
            @Param("averageScore") java.math.BigDecimal averageScore,
            @Param("timeSpentMinutes") Integer timeSpentMinutes);

    /**
     * Atualiza total de lições de uma trilha.
     */
    @Modifying
    @Query("UPDATE TrailProgress tp SET tp.totalLessons = :totalLessons, tp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE tp.trail.id = :trailId")
    void updateTotalLessons(@Param("trailId") UUID trailId, @Param("totalLessons") Integer totalLessons);

    /**
     * Incrementa lições completas.
     */
    @Modifying
    @Query("UPDATE TrailProgress tp SET " +
           "tp.lessonsCompleted = tp.lessonsCompleted + 1, " +
           "tp.lastActivityAt = CURRENT_TIMESTAMP, " +
           "tp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE tp.trail.id = :trailId")
    void incrementLessonsCompleted(@Param("trailId") UUID trailId);

    /**
     * Remove progresso por trilha.
     */
    void deleteByTrailId(UUID trailId);
}
