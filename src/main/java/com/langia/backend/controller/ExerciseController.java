package com.langia.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.ErrorPatternDTO;
import com.langia.backend.dto.MessageResponse;
import com.langia.backend.dto.SessionData;
import com.langia.backend.service.ExerciseTrackingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para tracking de exercícios e padrões de erros.
 */
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Slf4j
public class ExerciseController {

    private final ExerciseTrackingService exerciseTrackingService;

    /**
     * Busca padrões de erros do usuário.
     */
    @GetMapping("/errors")
    public ResponseEntity<List<ErrorPatternDTO>> getErrorPatterns(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode) {
        List<ErrorPatternDTO> errors = exerciseTrackingService.getErrorPatterns(session.getUserId(), languageCode);
        return ResponseEntity.ok(errors);
    }

    /**
     * Busca top N erros mais frequentes.
     */
    @GetMapping("/errors/top")
    public ResponseEntity<List<ErrorPatternDTO>> getTopErrors(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode,
            @RequestParam(defaultValue = "5") int limit) {
        List<ErrorPatternDTO> errors = exerciseTrackingService.getTopErrors(session.getUserId(), languageCode, limit);
        return ResponseEntity.ok(errors);
    }

    /**
     * Marca um padrão de erro como resolvido.
     */
    @PatchMapping("/errors/{errorId}/resolve")
    public ResponseEntity<MessageResponse> markErrorResolved(
            @AuthenticationPrincipal SessionData session,
            @PathVariable UUID errorId) {
        log.info("User {} marking error {} as resolved", session.getUserId(), errorId);

        exerciseTrackingService.markErrorResolved(session.getUserId(), errorId);
        return ResponseEntity.ok(new MessageResponse("Error marked as resolved"));
    }
}
