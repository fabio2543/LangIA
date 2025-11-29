package com.langia.backend.dto;

import java.util.List;

import com.langia.backend.model.CefrLevel;
import com.langia.backend.model.DifficultyLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating skill assessment (POST/PUT request).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillAssessmentDTO {

    @NotBlank(message = "Language is required")
    private String language;

    @NotNull
    private DifficultyLevel listeningDifficulty;

    @NotNull
    private DifficultyLevel speakingDifficulty;

    @NotNull
    private DifficultyLevel readingDifficulty;

    @NotNull
    private DifficultyLevel writingDifficulty;

    private List<String> listeningDetails;
    private List<String> speakingDetails;
    private List<String> readingDetails;
    private List<String> writingDetails;

    private CefrLevel selfCefrLevel;
}
