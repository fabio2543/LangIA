package com.langia.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.onboarding.OnboardingCompleteResponseDTO;
import com.langia.backend.dto.onboarding.OnboardingStatusDTO;
import com.langia.backend.service.OnboardingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para endpoints de onboarding.
 * Gerencia o processo de configuração inicial do usuário.
 */
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

    private final OnboardingService onboardingService;

    /**
     * Retorna o status atual do onboarding do usuário autenticado.
     *
     * GET /api/onboarding/status
     *
     * @param session Dados da sessão do usuário autenticado
     * @return Status detalhado do onboarding
     */
    @GetMapping("/status")
    public ResponseEntity<OnboardingStatusDTO> getStatus(@AuthenticationPrincipal SessionData session) {
        log.debug("GET /api/onboarding/status - Usuário: {}", session.getUserId());

        OnboardingStatusDTO status = onboardingService.getStatus(session.getUserId());
        return ResponseEntity.ok(status);
    }

    /**
     * Completa o processo de onboarding.
     * Valida que todas as etapas obrigatórias foram concluídas e gera a trilha inicial.
     *
     * POST /api/onboarding/complete
     *
     * @param session Dados da sessão do usuário autenticado
     * @return Resposta com informações da conclusão e trilha gerada
     */
    @PostMapping("/complete")
    public ResponseEntity<OnboardingCompleteResponseDTO> complete(@AuthenticationPrincipal SessionData session) {
        log.info("POST /api/onboarding/complete - Usuário: {}", session.getUserId());

        OnboardingCompleteResponseDTO response = onboardingService.complete(session.getUserId());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verifica se o usuário precisa completar o onboarding.
     *
     * GET /api/onboarding/needs
     *
     * @param session Dados da sessão do usuário autenticado
     * @return true se o onboarding é necessário
     */
    @GetMapping("/needs")
    public ResponseEntity<Boolean> needsOnboarding(@AuthenticationPrincipal SessionData session) {
        log.debug("GET /api/onboarding/needs - Usuário: {}", session.getUserId());

        boolean needs = onboardingService.needsOnboarding(session.getUserId());
        return ResponseEntity.ok(needs);
    }
}
