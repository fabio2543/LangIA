package com.langia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsReviewResponse {
    private LocalDate nextReviewDate;
    private int intervalDays;
    private BigDecimal easinessFactor;
}
