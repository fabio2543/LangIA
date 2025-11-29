package com.langia.backend.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de enrollment em um idioma.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLanguageEnrollmentRequest {

    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)?$", message = "CEFR level must be A1, A2, B1, B2, C1, or C2")
    private String cefrLevel;

    private Boolean isPrimary;
}
