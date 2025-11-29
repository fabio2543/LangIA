package com.langia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de enrollment em um idioma.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollLanguageRequest {

    @NotBlank(message = "Language code is required")
    @Size(max = 10, message = "Language code must be at most 10 characters")
    private String languageCode;

    @Pattern(regexp = "^(A1|A2|B1|B2|C1|C2)?$", message = "CEFR level must be A1, A2, B1, B2, C1, or C2")
    private String cefrLevel;

    private boolean isPrimary;
}
