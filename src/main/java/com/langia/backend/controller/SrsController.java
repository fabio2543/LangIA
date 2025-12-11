package com.langia.backend.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.MessageResponse;
import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.SrsDueCardsResponse;
import com.langia.backend.dto.SrsReviewRequest;
import com.langia.backend.dto.SrsReviewResponse;
import com.langia.backend.dto.SrsStatsResponse;
import com.langia.backend.service.SrsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para o Sistema de Repetição Espaçada (SRS).
 */
@RestController
@RequestMapping("/api/srs")
@RequiredArgsConstructor
@Slf4j
public class SrsController {

    private final SrsService srsService;

    /**
     * Busca cards pendentes de revisão para hoje.
     *
     * @param session sessão do usuário autenticado
     * @param languageCode código do idioma
     * @param limit limite opcional de cards
     * @return cards para revisão
     */
    @GetMapping("/due")
    public ResponseEntity<SrsDueCardsResponse> getDueCards(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode,
            @RequestParam(required = false) Integer limit) {
        log.debug("User {} fetching due cards for language {}", session.getUserId(), languageCode);

        SrsDueCardsResponse response = srsService.getDueCards(session.getUserId(), languageCode, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Registra revisão de um card.
     *
     * @param session sessão do usuário autenticado
     * @param request dados da revisão (cardId, quality 0-5)
     * @return resultado da revisão
     */
    @PostMapping("/review")
    public ResponseEntity<SrsReviewResponse> reviewCard(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody SrsReviewRequest request) {
        log.info("User {} reviewing card {} with quality {}",
                session.getUserId(), request.getCardId(), request.getQuality());

        SrsReviewResponse response = srsService.reviewCard(session.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca estatísticas do SRS do usuário.
     *
     * @param session sessão do usuário autenticado
     * @param languageCode código do idioma
     * @return estatísticas do SRS
     */
    @GetMapping("/stats")
    public ResponseEntity<SrsStatsResponse> getStats(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode) {
        SrsStatsResponse response = srsService.getStats(session.getUserId(), languageCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Adiciona um card ao sistema SRS.
     *
     * @param session sessão do usuário autenticado
     * @param cardId ID do card
     * @return mensagem de sucesso
     */
    @PostMapping("/cards/{cardId}")
    public ResponseEntity<MessageResponse> addCard(
            @AuthenticationPrincipal SessionData session,
            @PathVariable UUID cardId) {
        log.info("User {} adding card {} to SRS", session.getUserId(), cardId);

        srsService.addCardToSrs(session.getUserId(), cardId);
        return ResponseEntity.ok(new MessageResponse("Card added to SRS successfully"));
    }

    /**
     * Remove um card do sistema SRS.
     *
     * @param session sessão do usuário autenticado
     * @param cardId ID do card
     * @return mensagem de sucesso
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<MessageResponse> removeCard(
            @AuthenticationPrincipal SessionData session,
            @PathVariable UUID cardId) {
        log.info("User {} removing card {} from SRS", session.getUserId(), cardId);

        srsService.removeCardFromSrs(session.getUserId(), cardId);
        return ResponseEntity.ok(new MessageResponse("Card removed from SRS successfully"));
    }
}
