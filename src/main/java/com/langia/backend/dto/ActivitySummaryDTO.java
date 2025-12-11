package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySummaryDTO {
    private int totalLessons;
    private int totalExercises;
    private int totalCardsReviewed;
    private int totalMinutes;
    private int totalXp;
    private int activeDays;
    private double avgMinutesPerDay;
}
