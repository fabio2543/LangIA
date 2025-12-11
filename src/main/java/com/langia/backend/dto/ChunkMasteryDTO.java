package com.langia.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.langia.backend.model.ChunkMastery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMasteryDTO {
    private UUID id;
    private UUID chunkId;
    private LinguisticChunkDTO chunk;
    private int masteryLevel;
    private int timesPracticed;
    private LocalDateTime lastPracticedAt;
    private List<String> contextsUsed;

    public static ChunkMasteryDTO fromEntity(ChunkMastery entity) {
        return ChunkMasteryDTO.builder()
                .id(entity.getId())
                .chunkId(entity.getChunk().getId())
                .chunk(LinguisticChunkDTO.fromEntity(entity.getChunk()))
                .masteryLevel(entity.getMasteryLevel())
                .timesPracticed(entity.getTimesPracticed())
                .lastPracticedAt(entity.getLastPracticedAt())
                .contextsUsed(entity.getContextsUsed())
                .build();
    }
}
