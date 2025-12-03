package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.ModuleStatus;
import com.langia.backend.model.TrailModule;

/**
 * Repository para entidade TrailModule (módulos de uma trilha).
 */
@Repository
public interface TrailModuleRepository extends JpaRepository<TrailModule, UUID> {

    /**
     * Busca módulos de uma trilha ordenados por índice.
     */
    List<TrailModule> findByTrailIdOrderByOrderIndexAsc(UUID trailId);

    /**
     * Busca módulo por trilha e índice de ordem.
     */
    Optional<TrailModule> findByTrailIdAndOrderIndex(UUID trailId, Integer orderIndex);

    /**
     * Conta módulos de uma trilha.
     */
    long countByTrailId(UUID trailId);

    /**
     * Conta módulos prontos de uma trilha.
     */
    long countByTrailIdAndStatus(UUID trailId, ModuleStatus status);

    /**
     * Busca primeiro módulo pendente de uma trilha (para geração).
     */
    @Query("SELECT m FROM TrailModule m " +
           "WHERE m.trail.id = :trailId " +
           "AND m.status = 'PENDING' " +
           "ORDER BY m.orderIndex ASC " +
           "LIMIT 1")
    Optional<TrailModule> findFirstPendingByTrailId(@Param("trailId") UUID trailId);

    /**
     * Busca módulos por competência.
     */
    List<TrailModule> findByCompetencyIdOrderByOrderIndexAsc(UUID competencyId);

    /**
     * Atualiza status de um módulo.
     */
    @Modifying
    @Query("UPDATE TrailModule m SET m.status = :status, m.updatedAt = CURRENT_TIMESTAMP WHERE m.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") ModuleStatus status);

    /**
     * Busca próximo módulo após o índice especificado.
     */
    @Query("SELECT m FROM TrailModule m " +
           "WHERE m.trail.id = :trailId " +
           "AND m.orderIndex > :currentIndex " +
           "ORDER BY m.orderIndex ASC " +
           "LIMIT 1")
    Optional<TrailModule> findNextModule(
            @Param("trailId") UUID trailId,
            @Param("currentIndex") Integer currentIndex);

    /**
     * Busca módulo anterior ao índice especificado.
     */
    @Query("SELECT m FROM TrailModule m " +
           "WHERE m.trail.id = :trailId " +
           "AND m.orderIndex < :currentIndex " +
           "ORDER BY m.orderIndex DESC " +
           "LIMIT 1")
    Optional<TrailModule> findPreviousModule(
            @Param("trailId") UUID trailId,
            @Param("currentIndex") Integer currentIndex);

    /**
     * Remove todos os módulos de uma trilha.
     */
    void deleteByTrailId(UUID trailId);

    /**
     * Busca módulos de uma trilha com lições carregadas (evita N+1).
     */
    @Query("SELECT DISTINCT m FROM TrailModule m " +
           "LEFT JOIN FETCH m.lessons " +
           "WHERE m.trail.id = :trailId " +
           "ORDER BY m.orderIndex ASC")
    List<TrailModule> findByTrailIdWithLessons(@Param("trailId") UUID trailId);
}
