package com.langia.backend.dto.trail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.langia.backend.model.LessonType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar uma lição.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDTO {

    private UUID id;
    private UUID moduleId;
    private String title;
    private LessonType type;
    private Integer orderIndex;
    private Integer durationMinutes;
    private Object content;
    private Boolean isPlaceholder;
    private LocalDateTime completedAt;
    private BigDecimal score;
    private Integer timeSpentSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Indica se a lição foi completada.
     */
    public boolean isCompleted() {
        return completedAt != null;
    }
}
