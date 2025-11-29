package com.langia.backend.dto;

import com.langia.backend.model.Language;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar um idioma disponível na plataforma.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageDTO {

    private String code;
    private String namePt;
    private String nameEn;
    private String nameEs;
    private boolean active;

    /**
     * Converte entidade Language para DTO.
     */
    public static LanguageDTO fromEntity(Language language) {
        return LanguageDTO.builder()
                .code(language.getCode())
                .namePt(language.getNamePt())
                .nameEn(language.getNameEn())
                .nameEs(language.getNameEs())
                .active(language.isActive())
                .build();
    }
}
