package com.langia.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.langia.backend.model.ErrorPattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorPatternDTO {
    private UUID id;
    private String languageCode;
    private String skillType;
    private String errorCategory;
    private String errorDescription;
    private List<String> exampleErrors;
    private int occurrenceCount;
    private LocalDateTime firstOccurredAt;
    private LocalDateTime lastOccurredAt;
    private boolean isResolved;

    public static ErrorPatternDTO fromEntity(ErrorPattern entity) {
        List<String> examples = null;
        if (entity.getExampleErrors() != null) {
            examples = entity.getExampleErrors().stream()
                    .map(m -> m.toString())
                    .collect(Collectors.toList());
        }

        return ErrorPatternDTO.builder()
                .id(entity.getId())
                .languageCode(entity.getLanguageCode())
                .skillType(entity.getSkillType())
                .errorCategory(entity.getErrorCategory())
                .errorDescription(entity.getErrorDescription())
                .exampleErrors(examples)
                .occurrenceCount(entity.getOccurrenceCount() != null ? entity.getOccurrenceCount() : 0)
                .firstOccurredAt(entity.getFirstOccurredAt())
                .lastOccurredAt(entity.getLastOccurredAt())
                .isResolved(entity.getIsResolved() != null && entity.getIsResolved())
                .build();
    }
}
