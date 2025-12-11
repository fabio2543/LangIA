package com.langia.backend.dto;

import java.util.List;
import java.util.UUID;

import com.langia.backend.model.SrsProgress;
import com.langia.backend.model.VocabularyCard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrsCardWithProgressDTO {
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
    private SrsProgressDTO progress;

    public static SrsCardWithProgressDTO fromEntities(VocabularyCard card, SrsProgress progress) {
        return SrsCardWithProgressDTO.builder()
                .id(card.getId())
                .languageCode(card.getLanguageCode())
                .cefrLevel(card.getCefrLevel())
                .cardType(card.getCardType())
                .front(card.getFront())
                .back(card.getBack())
                .context(card.getContext())
                .exampleSentence(card.getExampleSentence())
                .audioUrl(card.getAudioUrl())
                .imageUrl(card.getImageUrl())
                .tags(card.getTags())
                .isSystemCard(card.getIsSystemCard() != null && card.getIsSystemCard())
                .progress(SrsProgressDTO.fromEntity(progress))
                .build();
    }
}
