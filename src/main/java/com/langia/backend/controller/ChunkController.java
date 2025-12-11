package com.langia.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.ChunkMasteryDTO;
import com.langia.backend.dto.LinguisticChunkDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.service.ChunkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para gerenciamento de chunks linguísticos.
 */
@RestController
@RequestMapping("/api/chunks")
@RequiredArgsConstructor
@Slf4j
public class ChunkController {

    private final ChunkService chunkService;

    /**
     * Busca chunks por idioma e nível.
     */
    @GetMapping
    public ResponseEntity<List<LinguisticChunkDTO>> getChunks(
            @RequestParam String languageCode,
            @RequestParam(required = false) String cefrLevel,
            @RequestParam(required = false) String category) {
        List<LinguisticChunkDTO> chunks = chunkService.getChunks(languageCode, cefrLevel, category);
        return ResponseEntity.ok(chunks);
    }

    /**
     * Busca chunks essenciais (core) de um nível.
     */
    @GetMapping("/core")
    public ResponseEntity<List<LinguisticChunkDTO>> getCoreChunks(
            @RequestParam String languageCode,
            @RequestParam String cefrLevel) {
        List<LinguisticChunkDTO> chunks = chunkService.getCoreChunks(languageCode, cefrLevel);
        return ResponseEntity.ok(chunks);
    }

    /**
     * Busca domínio do usuário sobre chunks.
     */
    @GetMapping("/mastery")
    public ResponseEntity<List<ChunkMasteryDTO>> getMastery(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode) {
        List<ChunkMasteryDTO> mastery = chunkService.getMastery(session.getUserId(), languageCode);
        return ResponseEntity.ok(mastery);
    }

    /**
     * Atualiza domínio de um chunk após prática.
     */
    @PostMapping("/{chunkId}/practice")
    public ResponseEntity<ChunkMasteryDTO> updateMastery(
            @AuthenticationPrincipal SessionData session,
            @PathVariable UUID chunkId,
            @RequestBody PracticeRequest request) {
        log.info("User {} practicing chunk {} with quality {}",
                session.getUserId(), chunkId, request.getQuality());

        ChunkMasteryDTO mastery = chunkService.updateMastery(
                session.getUserId(), chunkId, request.getQuality(), request.getContext());
        return ResponseEntity.ok(mastery);
    }

    /**
     * Busca chunks recomendados para praticar.
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<LinguisticChunkDTO>> getRecommended(
            @AuthenticationPrincipal SessionData session,
            @RequestParam String languageCode,
            @RequestParam(defaultValue = "10") int limit) {
        List<LinguisticChunkDTO> chunks = chunkService.getRecommended(session.getUserId(), languageCode, limit);
        return ResponseEntity.ok(chunks);
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PracticeRequest {
        private int quality;
        private String context;
    }
}
