package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillMetricsSummaryDTO {
    private String skillType;
    private int totalExercises;
    private double avgAccuracy;
    private double avgResponseTimeMs;
    private String trend; // "improving", "stable", "declining"
}
