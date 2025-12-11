package com.langia.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.SkillMetricDTO;
import com.langia.backend.dto.SkillMetricsSummaryDTO;
import com.langia.backend.service.MetricsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para métricas de habilidades.
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Busca métricas por habilidade desde uma data.
     */
    @GetMapping("/skills")
    public ResponseEntity<List<SkillMetricDTO>> getSkillMetrics(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode,
            @RequestParam(required = false) String since) {
        LocalDate sinceDate = since != null ? LocalDate.parse(since) : null;
        List<SkillMetricDTO> metrics = metricsService.getSkillMetrics(session.getUserId(), languageCode, sinceDate);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Busca resumo de métricas por habilidade com tendência.
     */
    @GetMapping("/summary")
    public ResponseEntity<List<SkillMetricsSummaryDTO>> getSummary(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode) {
        List<SkillMetricsSummaryDTO> summary = metricsService.getSummary(session.getUserId(), languageCode);
        return ResponseEntity.ok(summary);
    }

    /**
     * Busca progresso diário para gráfico.
     */
    @GetMapping("/daily-progress")
    public ResponseEntity<List<MetricsService.DailyProgressDTO>> getDailyProgress(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode,
            @RequestParam(defaultValue = "30") int days) {
        List<MetricsService.DailyProgressDTO> progress = metricsService.getDailyProgress(
                session.getUserId(), languageCode, days);
        return ResponseEntity.ok(progress);
    }
}
