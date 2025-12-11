package com.langia.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.ActivitySummaryDTO;
import com.langia.backend.dto.DailyActivityLogDTO;
import com.langia.backend.dto.DailyStreakDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.service.StreakService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para gerenciamento de streaks e atividade diária.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class StreakController {

    private final StreakService streakService;

    /**
     * Busca streak do usuário para um idioma.
     */
    @GetMapping("/streaks/{languageCode}")
    public ResponseEntity<DailyStreakDTO> getStreak(
            @AuthenticationPrincipal SessionData session,
            @PathVariable String languageCode) {
        DailyStreakDTO streak = streakService.getStreak(session.getUserId(), languageCode);
        return ResponseEntity.ok(streak);
    }

    /**
     * Busca todas as streaks do usuário.
     */
    @GetMapping("/streaks")
    public ResponseEntity<List<DailyStreakDTO>> getAllStreaks(
            @AuthenticationPrincipal SessionData session) {
        List<DailyStreakDTO> streaks = streakService.getAllStreaks(session.getUserId());
        return ResponseEntity.ok(streaks);
    }

    /**
     * Congela a streak por um dia.
     */
    @PostMapping("/streaks/{languageCode}/freeze")
    public ResponseEntity<DailyStreakDTO> freezeStreak(
            @AuthenticationPrincipal SessionData session,
            @PathVariable String languageCode) {
        log.info("User {} freezing streak for language {}", session.getUserId(), languageCode);

        DailyStreakDTO streak = streakService.freezeStreak(session.getUserId(), languageCode);
        return ResponseEntity.ok(streak);
    }

    /**
     * Busca log de atividade diária.
     */
    @GetMapping("/activity/log")
    public ResponseEntity<List<DailyActivityLogDTO>> getActivityLog(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode,
            @RequestParam(defaultValue = "30") int days) {
        List<DailyActivityLogDTO> logs = streakService.getActivityLog(session.getUserId(), languageCode, days);
        return ResponseEntity.ok(logs);
    }

    /**
     * Busca resumo de atividade.
     */
    @GetMapping("/activity/summary")
    public ResponseEntity<ActivitySummaryDTO> getActivitySummary(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode,
            @RequestParam(required = false) String since) {
        LocalDate sinceDate = since != null ? LocalDate.parse(since) : null;
        ActivitySummaryDTO summary = streakService.getActivitySummary(session.getUserId(), languageCode, sinceDate);
        return ResponseEntity.ok(summary);
    }
}
