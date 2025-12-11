package com.langia.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.ChunkMasteryDTO;
import com.langia.backend.dto.LinguisticChunkDTO;
import com.langia.backend.exception.ResourceNotFoundException;
import com.langia.backend.model.ChunkMastery;
import com.langia.backend.model.LinguisticChunk;
import com.langia.backend.model.User;
import com.langia.backend.repository.ChunkMasteryRepository;
import com.langia.backend.repository.LinguisticChunkRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para gerenciamento de chunks linguísticos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChunkService {

    private final LinguisticChunkRepository chunkRepository;
    private final ChunkMasteryRepository masteryRepository;
    private final UserRepository userRepository;

    /**
     * Busca chunks por idioma e nível.
     */
    public List<LinguisticChunkDTO> getChunks(String languageCode, String cefrLevel, String category) {
        List<LinguisticChunk> chunks;

        if (cefrLevel != null && category != null) {
            chunks = chunkRepository.findByLanguageCodeAndCefrLevelAndCategory(languageCode, cefrLevel, category);
        } else if (cefrLevel != null) {
            chunks = chunkRepository.findByLanguageCodeAndCefrLevel(languageCode, cefrLevel);
        } else if (category != null) {
            chunks = chunkRepository.findByLanguageCodeAndCategory(languageCode, category);
        } else {
            chunks = chunkRepository.findByLanguageCode(languageCode);
        }

        return chunks.stream()
                .map(LinguisticChunkDTO::fromEntity)
                .toList();
    }

    /**
     * Busca chunks essenciais (core) de um nível.
     */
    public List<LinguisticChunkDTO> getCoreChunks(String languageCode, String cefrLevel) {
        return chunkRepository.findCoreChunks(languageCode, cefrLevel)
                .stream()
                .map(LinguisticChunkDTO::fromEntity)
                .toList();
    }

    /**
     * Busca domínio do usuário sobre chunks.
     */
    public List<ChunkMasteryDTO> getMastery(UUID userId, String languageCode) {
        return masteryRepository.findByUserIdAndChunkLanguageCode(userId, languageCode)
                .stream()
                .map(ChunkMasteryDTO::fromEntity)
                .toList();
    }

    /**
     * Atualiza domínio de um chunk após prática.
     */
    @Transactional
    public ChunkMasteryDTO updateMastery(UUID userId, UUID chunkId, int quality, String context) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LinguisticChunk chunk = chunkRepository.findById(chunkId)
                .orElseThrow(() -> new ResourceNotFoundException("Chunk not found: " + chunkId));

        ChunkMastery mastery = masteryRepository.findByUserIdAndChunkId(userId, chunkId)
                .orElseGet(() -> {
                    ChunkMastery newMastery = new ChunkMastery();
                    newMastery.setUser(user);
                    newMastery.setChunk(chunk);
                    newMastery.setMasteryLevel(0);
                    newMastery.setTimesPracticed(0);
                    newMastery.setTimesCorrect(0);
                    return newMastery;
                });

        // Atualiza nível de domínio baseado na qualidade da resposta
        int newLevel = calculateNewMasteryLevel(mastery.getMasteryLevel(), quality);
        mastery.setMasteryLevel(newLevel);
        mastery.setTimesPracticed(mastery.getTimesPracticed() + 1);
        mastery.setLastPracticedAt(LocalDateTime.now());

        if (context != null && !context.isBlank()) {
            List<String> contexts = mastery.getContextsUsed();
            if (!contexts.contains(context)) {
                contexts.add(context);
                mastery.setContextsUsed(contexts);
            }
        }

        mastery = masteryRepository.save(mastery);

        log.info("User {} practiced chunk {} with quality {}, new mastery level: {}",
                userId, chunkId, quality, newLevel);

        return ChunkMasteryDTO.fromEntity(mastery);
    }

    /**
     * Busca chunks recomendados para praticar (baixo domínio).
     */
    public List<LinguisticChunkDTO> getRecommended(UUID userId, String languageCode, int limit) {
        return masteryRepository.findChunksNeedingPractice(userId, languageCode)
                .stream()
                .limit(limit)
                .map(m -> LinguisticChunkDTO.fromEntity(m.getChunk()))
                .toList();
    }

    private int calculateNewMasteryLevel(int currentLevel, int quality) {
        // quality: 0-5 (como SM-2)
        // mastery: 0-5

        if (quality >= 4) {
            return Math.min(5, currentLevel + 1);
        } else if (quality >= 3) {
            return currentLevel;
        } else if (quality >= 2) {
            return Math.max(0, currentLevel - 1);
        } else {
            return Math.max(0, currentLevel - 2);
        }
    }
}
