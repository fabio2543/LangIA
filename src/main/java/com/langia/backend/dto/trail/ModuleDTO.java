package com.langia.backend.dto.trail;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.langia.backend.model.ModuleStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar um módulo com suas lições.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleDTO {

    private UUID id;
    private UUID trailId;
    private String title;
    private String description;
    private Integer orderIndex;
    private ModuleStatus status;
    private String competencyCode;
    private String competencyName;
    private List<LessonDTO> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Conta total de lições no módulo.
     */
    public int getTotalLessons() {
        return lessons != null ? lessons.size() : 0;
    }

    /**
     * Conta lições completadas.
     */
    public long getCompletedLessons() {
        if (lessons == null) return 0;
        return lessons.stream().filter(LessonDTO::isCompleted).count();
    }

    /**
     * Calcula progresso do módulo em percentual.
     */
    public double getProgressPercentage() {
        int total = getTotalLessons();
        if (total == 0) return 0.0;
        return (getCompletedLessons() * 100.0) / total;
    }
}
