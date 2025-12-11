package com.langia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsStatsResponse {
    private int totalCards;
    private int mastered;
    private int learning;
    private int newCards;
    private int dueToday;
    private int reviewedToday;
}
