package com.langia.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.GenerationJobStatus;
import com.langia.backend.model.JobType;
import com.langia.backend.model.TrailGenerationJob;

/**
 * Repository para entidade TrailGenerationJob (jobs de geração de trilha).
 */
@Repository
public interface TrailGenerationJobRepository extends JpaRepository<TrailGenerationJob, UUID> {

    /**
     * Busca jobs por status ordenados por prioridade e data de enfileiramento.
     * Prioridade 1 é mais alta, 10 é mais baixa.
     */
    List<TrailGenerationJob> findByStatusOrderByPriorityAscQueuedAtAsc(GenerationJobStatus status);

    /**
     * Busca próximo job da fila (QUEUED, menor prioridade, mais antigo).
     */
    @Query("SELECT j FROM TrailGenerationJob j " +
           "WHERE j.status = 'QUEUED' " +
           "ORDER BY j.priority ASC, j.queuedAt ASC " +
           "LIMIT 1")
    Optional<TrailGenerationJob> findNextQueued();

    /**
     * Busca jobs prontos para retry (FAILED, com tentativas restantes, data de retry passada).
     */
    @Query("SELECT j FROM TrailGenerationJob j " +
           "WHERE j.status = 'FAILED' " +
           "AND j.attemptCount < j.maxAttempts " +
           "AND j.nextRetryAt <= :now " +
           "ORDER BY j.priority ASC, j.nextRetryAt ASC")
    List<TrailGenerationJob> findReadyForRetry(@Param("now") OffsetDateTime now);

    /**
     * Busca jobs de uma trilha específica.
     */
    List<TrailGenerationJob> findByTrailIdOrderByCreatedAtDesc(UUID trailId);

    /**
     * Busca jobs de um estudante específico.
     */
    List<TrailGenerationJob> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    /**
     * Busca job ativo (não finalizado) de uma trilha.
     */
    @Query("SELECT j FROM TrailGenerationJob j " +
           "WHERE j.trail.id = :trailId " +
           "AND j.status IN ('QUEUED', 'PROCESSING') " +
           "ORDER BY j.createdAt DESC")
    Optional<TrailGenerationJob> findActiveByTrailId(@Param("trailId") UUID trailId);

    /**
     * Verifica se existe job ativo para uma trilha.
     */
    @Query("SELECT CASE WHEN COUNT(j) > 0 THEN true ELSE false END FROM TrailGenerationJob j " +
           "WHERE j.trail.id = :trailId " +
           "AND j.status IN ('QUEUED', 'PROCESSING')")
    boolean existsActiveByTrailId(@Param("trailId") UUID trailId);

    /**
     * Busca jobs por tipo.
     */
    List<TrailGenerationJob> findByJobTypeOrderByCreatedAtDesc(JobType jobType);

    /**
     * Busca jobs em processamento por um worker específico.
     */
    List<TrailGenerationJob> findByWorkerIdAndStatus(String workerId, GenerationJobStatus status);

    /**
     * Busca jobs em processamento há mais tempo que o limite (para timeout).
     */
    @Query("SELECT j FROM TrailGenerationJob j " +
           "WHERE j.status = 'PROCESSING' " +
           "AND j.startedAt < :timeout")
    List<TrailGenerationJob> findStaleProcessingJobs(@Param("timeout") OffsetDateTime timeout);

    /**
     * Cancela jobs de uma trilha.
     */
    @Modifying
    @Query("UPDATE TrailGenerationJob j SET j.status = 'CANCELLED', j.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE j.trail.id = :trailId AND j.status IN ('QUEUED', 'PROCESSING')")
    int cancelByTrailId(@Param("trailId") UUID trailId);

    /**
     * Conta jobs por status.
     */
    long countByStatus(GenerationJobStatus status);

    /**
     * Conta jobs de um estudante por status.
     */
    long countByStudentIdAndStatus(UUID studentId, GenerationJobStatus status);

    /**
     * Busca último job de uma trilha.
     */
    Optional<TrailGenerationJob> findFirstByTrailIdOrderByCreatedAtDesc(UUID trailId);

    /**
     * Soma total de tokens usados por um estudante.
     */
    @Query("SELECT COALESCE(SUM(j.tokensUsed), 0) FROM TrailGenerationJob j " +
           "WHERE j.student.id = :studentId " +
           "AND j.status = 'COMPLETED'")
    long sumTokensUsedByStudentId(@Param("studentId") UUID studentId);
}
