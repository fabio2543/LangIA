package com.langia.backend.dto.trail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.langia.backend.model.TrailStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO resumido para listagem de trilhas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailSummaryDTO {

    private UUID id;
    private String languageCode;
    private String languageName;
    private String languageFlag;
    private String levelCode;
    private String levelName;
    private TrailStatus status;
    private BigDecimal progressPercentage;
    private Integer lessonsCompleted;
    private Integer totalLessons;
    private BigDecimal averageScore;
    private Integer timeSpentMinutes;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;

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
}
