package com.langia.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.langia.backend.model.DailyActivityLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyActivityLogDTO {
    private UUID id;
    private String languageCode;
    private LocalDate activityDate;
    private int lessonsStarted;
    private int lessonsCompleted;
    private int exercisesCompleted;
    private int cardsReviewed;
    private int minutesStudied;
    private int xpEarned;
    private List<String> skillsPracticed;

    public static DailyActivityLogDTO fromEntity(DailyActivityLog entity) {
        return DailyActivityLogDTO.builder()
                .id(entity.getId())
                .languageCode(entity.getLanguageCode())
                .activityDate(entity.getActivityDate())
                .lessonsStarted(entity.getLessonsStarted())
                .lessonsCompleted(entity.getLessonsCompleted())
                .exercisesCompleted(entity.getExercisesCompleted())
                .cardsReviewed(entity.getCardsReviewed())
                .minutesStudied(entity.getMinutesStudied())
                .xpEarned(entity.getXpEarned())
                .skillsPracticed(entity.getSkillsPracticed())
                .build();
    }
}
