package com.langia.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.langia.backend.dto.SkillMetricDTO;
import com.langia.backend.dto.SkillMetricsSummaryDTO;
import com.langia.backend.repository.SkillMetricRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para métricas de habilidades.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final SkillMetricRepository skillMetricRepository;

    /**
     * Busca métricas por habilidade desde uma data.
     */
    public List<SkillMetricDTO> getSkillMetrics(UUID userId, String languageCode, LocalDate since) {
        LocalDate startDate = since != null ? since : LocalDate.now().minusDays(30);

        return skillMetricRepository.findRecentMetrics(userId, languageCode, startDate)
                .stream()
                .map(SkillMetricDTO::fromEntity)
                .toList();
    }

    /**
     * Busca resumo de métricas por habilidade com tendência.
     */
    public List<SkillMetricsSummaryDTO> getSummary(UUID userId, String languageCode) {
        LocalDate since = LocalDate.now().minusDays(30);

        List<Object[]> results = skillMetricRepository.getAverageMetricsBySkill(userId, languageCode, since);

        List<SkillMetricsSummaryDTO> summaries = new ArrayList<>();

        for (Object[] row : results) {
            String skillType = (String) row[0];
            Double avgAccuracy = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
            Double avgResponseTime = row[2] != null ? ((Number) row[2]).doubleValue() : 0;

            // Calcula tendência comparando últimos 7 dias com 7-14 dias atrás
            String trend = calculateTrend(userId, languageCode, skillType);

            // Total de exercícios
            List<Object[]> totalStats = skillMetricRepository.getTotalStats(userId, since);
            int totalExercises = 0;
            if (!totalStats.isEmpty() && totalStats.get(0)[0] != null) {
                totalExercises = ((Number) totalStats.get(0)[0]).intValue();
            }

            summaries.add(SkillMetricsSummaryDTO.builder()
                    .skillType(skillType)
                    .totalExercises(totalExercises)
                    .avgAccuracy(avgAccuracy)
                    .avgResponseTimeMs(avgResponseTime)
                    .trend(trend)
                    .build());
        }

        return summaries;
    }

    /**
     * Busca progresso diário para gráfico.
     */
    public List<DailyProgressDTO> getDailyProgress(UUID userId, String languageCode, int days) {
        LocalDate since = LocalDate.now().minusDays(days);

        List<Object[]> results = skillMetricRepository.getDailyProgress(userId, languageCode, since);

        List<DailyProgressDTO> progress = new ArrayList<>();

        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            int exercises = row[1] != null ? ((Number) row[1]).intValue() : 0;
            double accuracy = row[2] != null ? ((Number) row[2]).doubleValue() : 0;

            progress.add(DailyProgressDTO.builder()
                    .date(date)
                    .exercisesCompleted(exercises)
                    .accuracyPercentage(accuracy)
                    .build());
        }

        return progress;
    }

    private String calculateTrend(UUID userId, String languageCode, String skillType) {
        LocalDate now = LocalDate.now();
        LocalDate weekAgo = now.minusDays(7);
        LocalDate twoWeeksAgo = now.minusDays(14);

        // Busca métricas das últimas duas semanas para calcular tendência
        List<Object[]> recentMetrics = skillMetricRepository.getAverageMetricsBySkill(userId, languageCode, weekAgo);
        List<Object[]> olderMetrics = skillMetricRepository.getAverageMetricsBySkill(userId, languageCode, twoWeeksAgo);

        if (recentMetrics.isEmpty() || olderMetrics.isEmpty()) {
            return "stable";
        }

        // Encontra as métricas para o skillType específico
        Double recentAccuracy = null;
        Double olderAccuracy = null;

        for (Object[] row : recentMetrics) {
            if (skillType.equals(row[0])) {
                recentAccuracy = row[1] != null ? ((Number) row[1]).doubleValue() : null;
                break;
            }
        }

        for (Object[] row : olderMetrics) {
            if (skillType.equals(row[0])) {
                olderAccuracy = row[1] != null ? ((Number) row[1]).doubleValue() : null;
                break;
            }
        }

        if (recentAccuracy == null || olderAccuracy == null) {
            return "stable";
        }

        double diff = recentAccuracy - olderAccuracy;

        if (diff > 5) {
            return "improving";
        } else if (diff < -5) {
            return "declining";
        } else {
            return "stable";
        }
    }

    /**
     * DTO interno para progresso diário.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DailyProgressDTO {
        private LocalDate date;
        private int exercisesCompleted;
        private double accuracyPercentage;
    }
}
