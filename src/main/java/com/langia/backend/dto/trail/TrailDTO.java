package com.langia.backend.dto.trail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.langia.backend.model.TrailStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO completo para representar uma trilha com módulos e progresso.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailDTO {

    private UUID id;
    private UUID studentId;
    private String languageCode;
    private String languageName;
    private String languageFlag;
    private String levelCode;
    private String levelName;
    private TrailStatus status;
    private String contentHash;
    private String curriculumVersion;
    private BigDecimal estimatedDurationHours;
    private UUID blueprintId;
    private UUID previousTrailId;
    private String refreshReason;
    private List<ModuleDTO> modules;
    private TrailProgressDTO progress;
    private LocalDateTime archivedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Verifica se a trilha está pronta para uso.
     */
    public boolean isReady() {
        return status == TrailStatus.READY;
    }

    /**
     * Verifica se a trilha está em geração.
     */
    public boolean isGenerating() {
        return status == TrailStatus.GENERATING || status == TrailStatus.PARTIAL;
    }

    /**
     * Verifica se a trilha está arquivada.
     */
    public boolean isArchived() {
        return status == TrailStatus.ARCHIVED;
    }

    /**
     * Retorna total de módulos.
     */
    public int getTotalModules() {
        return modules != null ? modules.size() : 0;
    }
}
