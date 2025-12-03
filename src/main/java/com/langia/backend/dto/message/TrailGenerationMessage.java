package com.langia.backend.dto.message;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mensagem para fila de geração de trilhas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailGenerationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID trailId;
    private UUID studentId;
    private String languageCode;
    private String levelCode;
    private UUID blueprintId;
    private String preferencesJson;
    private String curriculumVersion;
    private LocalDateTime requestedAt;
    private Integer attemptNumber;
    private Integer maxAttempts;

    /**
     * Cria mensagem inicial de geração.
     */
    public static TrailGenerationMessage create(UUID trailId, UUID studentId, String languageCode,
                                                 String levelCode, UUID blueprintId, String curriculumVersion) {
        return TrailGenerationMessage.builder()
                .trailId(trailId)
                .studentId(studentId)
                .languageCode(languageCode)
                .levelCode(levelCode)
                .blueprintId(blueprintId)
                .curriculumVersion(curriculumVersion)
                .requestedAt(LocalDateTime.now())
                .attemptNumber(1)
                .maxAttempts(3)
                .build();
    }

    /**
     * Cria mensagem de retry.
     */
    public TrailGenerationMessage retry() {
        return TrailGenerationMessage.builder()
                .trailId(this.trailId)
                .studentId(this.studentId)
                .languageCode(this.languageCode)
                .levelCode(this.levelCode)
                .blueprintId(this.blueprintId)
                .preferencesJson(this.preferencesJson)
                .curriculumVersion(this.curriculumVersion)
                .requestedAt(this.requestedAt)
                .attemptNumber(this.attemptNumber + 1)
                .maxAttempts(this.maxAttempts)
                .build();
    }

    /**
     * Verifica se pode tentar novamente.
     */
    public boolean canRetry() {
        return attemptNumber < maxAttempts;
    }
}
