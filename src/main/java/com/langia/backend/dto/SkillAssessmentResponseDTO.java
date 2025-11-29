package com.langia.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.langia.backend.model.CefrLevel;
import com.langia.backend.model.DifficultyLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning skill assessment (GET response).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAssessmentResponseDTO {
    private UUID id;
    private String language;
    private DifficultyLevel listeningDifficulty;
    private DifficultyLevel speakingDifficulty;
    private DifficultyLevel readingDifficulty;
    private DifficultyLevel writingDifficulty;
    private List<String> listeningDetails;
    private List<String> speakingDetails;
    private List<String> readingDetails;
    private List<String> writingDetails;
    private CefrLevel selfCefrLevel;
    private LocalDateTime assessedAt;
}
