package com.langia.backend.dto.trail;

import java.time.LocalDateTime;
import java.util.UUID;

import com.langia.backend.model.GenerationJobStatus;
import com.langia.backend.model.TrailStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para status de geração de trilha (usado em SSE).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailGenerationStatusDTO {

    private UUID trailId;
    private UUID jobId;
    private TrailStatus trailStatus;
    private GenerationJobStatus jobStatus;
    private Integer progressPercentage;
    private String currentStep;
    private String message;
    private Integer modulesGenerated;
    private Integer totalModules;
    private Integer lessonsGenerated;
    private Integer totalLessons;
    private LocalDateTime startedAt;
    private LocalDateTime estimatedCompletionAt;
    private String errorMessage;
    private Integer attemptNumber;
    private Integer maxAttempts;

    /**
     * Verifica se a geração foi concluída (sucesso ou falha).
     */
    public boolean isFinished() {
        return jobStatus == GenerationJobStatus.COMPLETED ||
               jobStatus == GenerationJobStatus.FAILED ||
               jobStatus == GenerationJobStatus.CANCELLED;
    }

    /**
     * Verifica se a geração foi bem sucedida.
     */
    public boolean isSuccess() {
        return jobStatus == GenerationJobStatus.COMPLETED;
    }

    /**
     * Verifica se a geração falhou.
     */
    public boolean isFailed() {
        return jobStatus == GenerationJobStatus.FAILED;
    }
}
