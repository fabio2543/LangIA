package com.langia.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.trail.TrailProgressDTO;
import com.langia.backend.mapper.TrailMapper;
import com.langia.backend.model.Trail;
import com.langia.backend.model.TrailProgress;
import com.langia.backend.repository.LessonRepository;
import com.langia.backend.repository.TrailProgressRepository;
import com.langia.backend.repository.TrailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerenciamento do progresso de trilhas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrailProgressService {

    private final TrailProgressRepository trailProgressRepository;
    private final TrailRepository trailRepository;
    private final LessonRepository lessonRepository;
    private final TrailMapper trailMapper;

    /**
     * Busca progresso de uma trilha.
     */
    @Transactional(readOnly = true)
    public Optional<TrailProgressDTO> getProgress(UUID trailId) {
        return trailProgressRepository.findByTrailId(trailId)
                .map(trailMapper::toTrailProgressDTO);
    }

    /**
     * Cria ou atualiza registro de progresso para uma trilha.
     * Se já existir, apenas atualiza o totalLessons.
     */
    @Transactional
    public TrailProgress createProgress(Trail trail, int totalLessons) {
        log.info("Criando registro de progresso para trilha: {}", trail.getId());

        // Verificar se já existe para evitar duplicate key
        Optional<TrailProgress> existing = trailProgressRepository.findByTrailId(trail.getId());
        if (existing.isPresent()) {
            log.debug("Progresso já existe para trilha: {}, atualizando totalLessons", trail.getId());
            TrailProgress progress = existing.get();
            progress.setTotalLessons(totalLessons);
            return trailProgressRepository.save(progress);
        }

        TrailProgress progress = TrailProgress.builder()
                .trail(trail)
                .totalLessons(totalLessons)
                .lessonsCompleted(0)
                .progressPercentage(BigDecimal.ZERO)
                .timeSpentMinutes(0)
                .build();

        return trailProgressRepository.save(progress);
    }

    /**
     * Recalcula e atualiza progresso de uma trilha.
     */
    @Transactional
    public TrailProgressDTO recalculateProgress(UUID trailId) {
        log.info("Recalculando progresso da trilha: {}", trailId);

        Trail trail = trailRepository.findById(trailId)
                .orElseThrow(() -> new RuntimeException("Trilha não encontrada: " + trailId));

        TrailProgress progress = trailProgressRepository.findByTrailId(trailId)
                .orElseGet(() -> createProgress(trail, 0));

        // Contar total de lições e completadas
        long totalLessons = lessonRepository.countByTrailId(trailId);
        long completedLessons = lessonRepository.countCompletedByTrailId(trailId);

        // Calcular percentual
        BigDecimal percentage = BigDecimal.ZERO;
        if (totalLessons > 0) {
            percentage = BigDecimal.valueOf(completedLessons)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
        }

        // Calcular média de score
        BigDecimal averageScore = lessonRepository.calculateAverageScoreByTrailId(trailId);

        // Calcular tempo total gasto (em segundos, convertendo para minutos)
        Long totalSeconds = lessonRepository.sumTimeSpentByTrailId(trailId);
        int timeSpentMinutes = (int) (totalSeconds / 60);

        // Atualizar progresso
        progress.setTotalLessons((int) totalLessons);
        progress.setLessonsCompleted((int) completedLessons);
        progress.setProgressPercentage(percentage);
        progress.setAverageScore(averageScore);
        progress.setTimeSpentMinutes(timeSpentMinutes);

        TrailProgress saved = trailProgressRepository.save(progress);
        log.info("Progresso atualizado - Trilha: {}, Lições: {}/{}, Percentual: {}%",
                trailId, completedLessons, totalLessons, percentage);

        return trailMapper.toTrailProgressDTO(saved);
    }

    /**
     * Atualiza total de lições de uma trilha (chamado após geração de módulos).
     */
    @Transactional
    public void updateTotalLessons(UUID trailId) {
        log.debug("Atualizando total de lições da trilha: {}", trailId);

        long totalLessons = lessonRepository.countByTrailId(trailId);

        Optional<TrailProgress> progressOpt = trailProgressRepository.findByTrailId(trailId);
        if (progressOpt.isPresent()) {
            TrailProgress progress = progressOpt.get();
            progress.setTotalLessons((int) totalLessons);
            trailProgressRepository.save(progress);
        } else {
            Trail trail = trailRepository.findById(trailId)
                    .orElseThrow(() -> new RuntimeException("Trilha não encontrada: " + trailId));
            createProgress(trail, (int) totalLessons);
        }
    }

    /**
     * Incrementa contador de lições completadas e recalcula progresso.
     */
    @Transactional
    public TrailProgressDTO incrementCompletedLesson(UUID trailId, BigDecimal score, int timeSpentSeconds) {
        log.debug("Incrementando lição completada - Trilha: {}, Score: {}, Tempo: {}s",
                trailId, score, timeSpentSeconds);

        // Recalcula progresso completo para manter consistência
        return recalculateProgress(trailId);
    }

    /**
     * Verifica se trilha está 100% completa.
     */
    @Transactional(readOnly = true)
    public boolean isTrailCompleted(UUID trailId) {
        return trailProgressRepository.findByTrailId(trailId)
                .map(p -> p.getProgressPercentage().compareTo(BigDecimal.valueOf(100)) >= 0)
                .orElse(false);
    }

    /**
     * Obtém lições restantes de uma trilha.
     */
    @Transactional(readOnly = true)
    public int getRemainingLessons(UUID trailId) {
        return trailProgressRepository.findByTrailId(trailId)
                .map(p -> p.getTotalLessons() - p.getLessonsCompleted())
                .orElse(0);
    }
}
