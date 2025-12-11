package com.langia.backend.dto;

import java.util.List;
import java.util.UUID;

import com.langia.backend.model.VocabularyCard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyCardDTO {
    private UUID id;
    private String languageCode;
    private String cefrLevel;
    private String cardType;
    private String front;
    private String back;
    private String context;
    private String exampleSentence;
    private String audioUrl;
    private String imageUrl;
    private List<String> tags;
    private boolean isSystemCard;

    public static VocabularyCardDTO fromEntity(VocabularyCard entity) {
        return VocabularyCardDTO.builder()
                .id(entity.getId())
                .languageCode(entity.getLanguageCode())
                .cefrLevel(entity.getCefrLevel())
                .cardType(entity.getCardType())
                .front(entity.getFront())
                .back(entity.getBack())
                .context(entity.getContext())
                .exampleSentence(entity.getExampleSentence())
                .audioUrl(entity.getAudioUrl())
                .imageUrl(entity.getImageUrl())
                .tags(entity.getTags())
                .isSystemCard(entity.getIsSystemCard() != null && entity.getIsSystemCard())
                .build();
    }
}
