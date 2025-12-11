package com.langia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.langia.backend.model.SrsProgress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsProgressDTO {
    private UUID id;
    private UUID cardId;
    private BigDecimal easinessFactor;
    private int intervalDays;
    private int repetitions;
    private LocalDate nextReviewDate;
    private LocalDateTime lastReviewedAt;
    private Integer lastQuality;
    private int totalReviews;
    private int correctReviews;

    public static SrsProgressDTO fromEntity(SrsProgress entity) {
        return SrsProgressDTO.builder()
                .id(entity.getId())
                .cardId(entity.getCard().getId())
                .easinessFactor(entity.getEasinessFactor())
                .intervalDays(entity.getIntervalDays())
                .repetitions(entity.getRepetitions())
                .nextReviewDate(entity.getNextReviewDate())
                .lastReviewedAt(entity.getLastReviewedAt())
                .lastQuality(entity.getLastQuality())
                .totalReviews(entity.getTotalReviews())
                .correctReviews(entity.getCorrectReviews())
                .build();
    }
}
