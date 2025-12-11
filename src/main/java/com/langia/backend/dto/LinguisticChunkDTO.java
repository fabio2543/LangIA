package com.langia.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.langia.backend.model.LinguisticChunk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinguisticChunkDTO {
    private UUID id;
    private String languageCode;
    private String cefrLevel;
    private String chunkText;
    private String translation;
    private String category;
    private String usageContext;
    private List<String> variations;
    private String audioUrl;
    private BigDecimal difficultyScore;
    private boolean isCore;

    public static LinguisticChunkDTO fromEntity(LinguisticChunk entity) {
        return LinguisticChunkDTO.builder()
                .id(entity.getId())
                .languageCode(entity.getLanguageCode())
                .cefrLevel(entity.getCefrLevel())
                .chunkText(entity.getChunkText())
                .translation(entity.getTranslation())
                .category(entity.getCategory())
                .usageContext(entity.getUsageContext())
                .variations(entity.getVariations())
                .audioUrl(entity.getAudioUrl())
                .difficultyScore(entity.getDifficultyScore())
                .isCore(entity.getIsCore() != null && entity.getIsCore())
                .build();
    }
}
