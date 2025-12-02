package com.langia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.langia.backend.model.StudentLanguageEnrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar o enrollment de um estudante em um idioma.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageEnrollmentDTO {

    private UUID id;
    private String languageCode;
    private String languageNamePt;
    private String languageNameEn;
    private String languageNameEs;
    private String cefrLevel;

    @JsonProperty("isPrimary")
    private boolean isPrimary;

    private LocalDateTime enrolledAt;
    private LocalDateTime lastStudiedAt;

    /**
     * Converte entidade StudentLanguageEnrollment para DTO.
     */
    public static LanguageEnrollmentDTO fromEntity(StudentLanguageEnrollment enrollment) {
        return LanguageEnrollmentDTO.builder()
                .id(enrollment.getId())
                .languageCode(enrollment.getLanguage().getCode())
                .languageNamePt(enrollment.getLanguage().getNamePt())
                .languageNameEn(enrollment.getLanguage().getNameEn())
                .languageNameEs(enrollment.getLanguage().getNameEs())
                .cefrLevel(enrollment.getCefrLevel())
                .isPrimary(enrollment.isPrimary())
                .enrolledAt(enrollment.getEnrolledAt())
                .lastStudiedAt(enrollment.getLastStudiedAt())
                .build();
    }
}
