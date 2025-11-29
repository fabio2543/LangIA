package com.langia.backend.dto;

import java.util.List;
import java.util.Map;

import com.langia.backend.model.CefrLevel;
import com.langia.backend.model.LearningFormat;
import com.langia.backend.model.LearningObjective;
import com.langia.backend.model.StudyDayOfWeek;
import com.langia.backend.model.TimeAvailable;
import com.langia.backend.model.TimeOfDay;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for learning preferences (GET and PUT).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPreferencesDTO {

    @NotEmpty(message = "Select at least one language")
    @Size(max = 3, message = "Maximum 3 languages")
    private List<String> studyLanguages;

    @NotBlank(message = "Primary language is required")
    private String primaryLanguage;

    private Map<String, CefrLevel> selfLevelByLanguage;

    private TimeAvailable dailyTimeAvailable;

    @NotEmpty(message = "Select at least one day")
    private List<StudyDayOfWeek> preferredDays;

    private List<TimeOfDay> preferredTimes;

    @Min(value = 1, message = "Minimum 1 hour per week")
    @Max(value = 168, message = "Maximum 168 hours per week")
    private Integer weeklyHoursGoal;

    @NotEmpty(message = "Select at least one topic")
    private List<String> topicsOfInterest;

    @Size(max = 5, message = "Maximum 5 custom topics")
    private List<String> customTopics;

    @NotEmpty(message = "Select at least one format")
    private List<LearningFormat> preferredFormats;

    private List<LearningFormat> formatRanking;

    private LearningObjective primaryObjective;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String objectiveDescription;

    private String objectiveDeadline;
}
