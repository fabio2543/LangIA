package com.langia.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.SrsCardWithProgressDTO;
import com.langia.backend.dto.SrsDueCardsResponse;
import com.langia.backend.dto.SrsReviewRequest;
import com.langia.backend.dto.SrsReviewResponse;
import com.langia.backend.dto.SrsStatsResponse;
import com.langia.backend.exception.ResourceNotFoundException;
import com.langia.backend.model.SrsProgress;
import com.langia.backend.model.User;
import com.langia.backend.model.VocabularyCard;
import com.langia.backend.repository.SrsProgressRepository;
import com.langia.backend.repository.UserRepository;
import com.langia.backend.repository.VocabularyCardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para o Sistema de Repetição Espaçada (SRS) usando algoritmo SM-2.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrsService {

    private final SrsProgressRepository srsProgressRepository;
    private final VocabularyCardRepository vocabularyCardRepository;
    private final UserRepository userRepository;

    /**
     * Busca cards pendentes de revisão para hoje.
     */
    public SrsDueCardsResponse getDueCards(UUID userId, String languageCode, Integer limit) {
        LocalDate today = LocalDate.now();

        List<SrsProgress> dueProgress = srsProgressRepository.findDueCards(userId, languageCode, today);

        if (limit != null && limit > 0 && dueProgress.size() > limit) {
            dueProgress = dueProgress.subList(0, limit);
        }

        List<SrsCardWithProgressDTO> cards = dueProgress.stream()
                .map(progress -> SrsCardWithProgressDTO.fromEntities(progress.getCard(), progress))
                .toList();

        // Conta cards revisados hoje
        int reviewedToday = srsProgressRepository.countReviewedToday(userId, languageCode, today);

        return SrsDueCardsResponse.builder()
                .cards(cards)
                .totalDue(dueProgress.size())
                .reviewedToday(reviewedToday)
                .build();
    }

    /**
     * Registra revisão de um card usando algoritmo SM-2.
     */
    @Transactional
    public SrsReviewResponse reviewCard(UUID userId, SrsReviewRequest request) {
        SrsProgress progress = srsProgressRepository.findByUserIdAndCardId(userId, request.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found for card: " + request.getCardId()));

        // Aplica algoritmo SM-2
        progress.review(request.getQuality());

        progress = srsProgressRepository.save(progress);
        log.info("User {} reviewed card {} with quality {}", userId, request.getCardId(), request.getQuality());

        return SrsReviewResponse.builder()
                .nextReviewDate(progress.getNextReviewDate())
                .intervalDays(progress.getIntervalDays())
                .easinessFactor(progress.getEasinessFactor())
                .build();
    }

    /**
     * Busca estatísticas do SRS do usuário.
     */
    public SrsStatsResponse getStats(UUID userId, String languageCode) {
        LocalDate today = LocalDate.now();

        int totalCards = vocabularyCardRepository.countByUserIdOrSystemCard(userId, languageCode);
        int dueToday = srsProgressRepository.countDueCards(userId, languageCode, today);
        int reviewedToday = srsProgressRepository.countReviewedToday(userId, languageCode, today);

        // Cards com intervalDays >= 21 são considerados "dominados"
        int mastered = srsProgressRepository.countMastered(userId, languageCode);

        // Cards em aprendizado (com pelo menos 1 review)
        int learning = srsProgressRepository.countLearning(userId, languageCode);

        // Cards novos (sem review)
        int newCards = totalCards - mastered - learning;

        return SrsStatsResponse.builder()
                .totalCards(totalCards)
                .mastered(mastered)
                .learning(learning)
                .newCards(Math.max(0, newCards))
                .dueToday(dueToday)
                .reviewedToday(reviewedToday)
                .build();
    }

    /**
     * Adiciona um card ao sistema SRS do usuário.
     */
    @Transactional
    public void addCardToSrs(UUID userId, UUID cardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        VocabularyCard card = vocabularyCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        // Verifica se já existe progresso para este card
        if (srsProgressRepository.existsByUserIdAndCardId(userId, cardId)) {
            log.warn("Card {} already in SRS for user {}", cardId, userId);
            return;
        }

        SrsProgress progress = new SrsProgress();
        progress.setUser(user);
        progress.setCard(card);
        progress.setNextReviewDate(LocalDate.now());

        srsProgressRepository.save(progress);
        log.info("Card {} added to SRS for user {}", cardId, userId);
    }

    /**
     * Remove um card do sistema SRS do usuário.
     */
    @Transactional
    public void removeCardFromSrs(UUID userId, UUID cardId) {
        SrsProgress progress = srsProgressRepository.findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found for card: " + cardId));

        srsProgressRepository.delete(progress);
        log.info("Card {} removed from SRS for user {}", cardId, userId);
    }
}
