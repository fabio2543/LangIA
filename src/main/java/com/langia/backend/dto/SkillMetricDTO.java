package com.langia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.langia.backend.model.SkillMetric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillMetricDTO {
    private UUID id;
    private String languageCode;
    private String skillType;
    private LocalDate metricDate;
    private int exercisesCompleted;
    private int correctAnswers;
    private BigDecimal accuracyPercentage;
    private Integer avgResponseTimeMs;
    private int totalPracticeTimeMinutes;
    private int xpEarned;

    public static SkillMetricDTO fromEntity(SkillMetric entity) {
        return SkillMetricDTO.builder()
                .id(entity.getId())
                .languageCode(entity.getLanguageCode())
                .skillType(entity.getSkillType())
                .metricDate(entity.getMetricDate())
                .exercisesCompleted(entity.getExercisesCompleted())
                .correctAnswers(entity.getCorrectAnswers())
                .accuracyPercentage(entity.getAccuracyPercentage())
                .avgResponseTimeMs(entity.getAvgResponseTimeMs())
                .totalPracticeTimeMinutes(entity.getTotalPracticeTimeMinutes())
                .xpEarned(entity.getXpEarned())
                .build();
    }
}
