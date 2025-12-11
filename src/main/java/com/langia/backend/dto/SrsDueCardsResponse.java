package com.langia.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsDueCardsResponse {
    private List<SrsCardWithProgressDTO> cards;
    private int totalDue;
    private int reviewedToday;
}
