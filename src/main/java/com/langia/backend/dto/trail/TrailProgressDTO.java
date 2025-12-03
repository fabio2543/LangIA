package com.langia.backend.dto.trail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar o progresso consolidado de uma trilha.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailProgressDTO {

    private UUID id;
    private UUID trailId;
    private Integer totalLessons;
    private Integer lessonsCompleted;
    private BigDecimal progressPercentage;
    private BigDecimal averageScore;
    private Integer timeSpentMinutes;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Retorna tempo gasto formatado (ex: "2h 30min").
     */
    public String getFormattedTimeSpent() {
        if (timeSpentMinutes == null || timeSpentMinutes == 0) {
            return "0min";
        }
        int hours = timeSpentMinutes / 60;
        int minutes = timeSpentMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "min";
        }
        return minutes + "min";
    }

    /**
     * Retorna lições restantes.
     */
    public int getRemainingLessons() {
        if (totalLessons == null || lessonsCompleted == null) return 0;
        return Math.max(0, totalLessons - lessonsCompleted);
    }
}
