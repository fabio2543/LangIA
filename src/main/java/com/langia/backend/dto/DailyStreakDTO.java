package com.langia.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.langia.backend.model.DailyStreak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStreakDTO {
    private UUID id;
    private String languageCode;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastStudyDate;
    private LocalDate streakStartedAt;
    private LocalDate streakFrozenUntil;
    private int totalStudyDays;

    public static DailyStreakDTO fromEntity(DailyStreak entity) {
        return DailyStreakDTO.builder()
                .id(entity.getId())
                .languageCode(entity.getLanguageCode())
                .currentStreak(entity.getCurrentStreak())
                .longestStreak(entity.getLongestStreak())
                .lastStudyDate(entity.getLastStudyDate())
                .streakStartedAt(entity.getStreakStartedAt())
                .streakFrozenUntil(entity.getStreakFrozenUntil())
                .totalStudyDays(entity.getTotalStudyDays())
                .build();
    }
}
