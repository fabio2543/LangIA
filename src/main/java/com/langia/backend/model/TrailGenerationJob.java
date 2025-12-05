package com.langia.backend.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa um job de geração de trilha na fila.
 * Jobs são processados assincronamente por workers via RabbitMQ.
 *
 * Corresponde à tabela trail_generation_jobs (V018).
 */
@Entity
@Table(name = "trail_generation_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailGenerationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trail_id", nullable = false)
    private Trail trail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private GenerationJobStatus status = GenerationJobStatus.QUEUED;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    @Builder.Default
    private JobType jobType = JobType.full_generation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gaps", columnDefinition = "jsonb")
    private String gaps;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 5;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_details", columnDefinition = "jsonb")
    private String errorDetails;

    @Column(name = "tokens_used", nullable = false)
    @Builder.Default
    private Integer tokensUsed = 0;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Column(name = "queued_at", nullable = false)
    @Builder.Default
    private OffsetDateTime queuedAt = OffsetDateTime.now();

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "next_retry_at")
    private OffsetDateTime nextRetryAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /**
     * Verifica se o job pode ser reprocessado.
     */
    public boolean canRetry() {
        return attemptCount < maxAttempts &&
               (status == GenerationJobStatus.FAILED || status == GenerationJobStatus.QUEUED);
    }

    /**
     * Incrementa o contador de tentativas.
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }

    /**
     * Marca o job como em processamento.
     */
    public void markAsProcessing(String workerId) {
        this.status = GenerationJobStatus.PROCESSING;
        this.workerId = workerId;
        this.startedAt = OffsetDateTime.now();
    }

    /**
     * Marca o job como concluído com sucesso.
     */
    public void markAsCompleted(int tokensUsed, int processingTimeMs) {
        this.status = GenerationJobStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
        this.tokensUsed = tokensUsed;
        this.processingTimeMs = processingTimeMs;
    }

    /**
     * Marca o job como falho.
     * O errorDetails é armazenado como JSON no banco.
     */
    public void markAsFailed(String error, String errorDetails) {
        this.status = GenerationJobStatus.FAILED;
        this.failedAt = OffsetDateTime.now();
        this.lastError = error;
        // Garante que errorDetails seja JSON válido ou null
        if (errorDetails != null && !errorDetails.isBlank()) {
            // Se já for JSON, usa direto. Senão, encapsula como string JSON
            if (errorDetails.trim().startsWith("{") || errorDetails.trim().startsWith("[")) {
                this.errorDetails = errorDetails;
            } else {
                // Escapa a string para JSON
                this.errorDetails = "\"" + errorDetails.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t") + "\"";
            }
        } else {
            this.errorDetails = null;
        }
    }

    /**
     * Agenda nova tentativa.
     */
    public void scheduleRetry(OffsetDateTime nextRetryAt) {
        this.status = GenerationJobStatus.QUEUED;
        this.nextRetryAt = nextRetryAt;
    }
}
