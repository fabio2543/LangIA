package com.langia.backend.dto;

import java.util.List;

import com.langia.backend.model.LearningObjective;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for learning preferences (GET and PUT).
 * Note: Language enrollments are now managed separately via /api/profile/languages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPreferencesDTO {

    // Availability
    private String dailyTimeAvailable;

    private List<String> preferredDays;

    private List<String> preferredTimes;

    @Min(value = 1, message = "Minimum 1 hour per week")
    @Max(value = 168, message = "Maximum 168 hours per week")
    private Integer weeklyHoursGoal;

    // Interests
    private List<String> topicsOfInterest;

    @Size(max = 5, message = "Maximum 5 custom topics")
    private List<String> customTopics;

    // Formats
    private List<String> preferredFormats;

    private List<String> formatRanking;

    // Objectives
    private LearningObjective primaryObjective;

    private LearningObjective secondaryObjective;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String objectiveDescription;

    private String objectiveDeadline;
}
