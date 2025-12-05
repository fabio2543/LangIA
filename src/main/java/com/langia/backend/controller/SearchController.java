package com.langia.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.DocumentEmbeddingRequest;
import com.langia.backend.dto.DocumentEmbeddingResponse;
import com.langia.backend.dto.SemanticSearchRequest;
import com.langia.backend.exception.DocumentNotFoundException;
import com.langia.backend.model.DocumentEmbedding;
import com.langia.backend.service.EmbeddingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para busca semântica e gerenciamento de documentos.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final EmbeddingService embeddingService;

    /**
     * Busca semântica de documentos.
     *
     * @param request requisição com query e parâmetros
     * @return lista de documentos similares
     */
    @PostMapping("/search/semantic")
    public ResponseEntity<List<DocumentEmbeddingResponse>> semanticSearch(
            @Valid @RequestBody SemanticSearchRequest request) {
        log.info("Busca semântica: query='{}', type={}, limit={}",
                request.getQuery(), request.getContentType(), request.getLimit());

        List<DocumentEmbedding> results;

        if (request.getMaxDistance() != null) {
            results = embeddingService.findSimilarWithThreshold(
                    request.getQuery(),
                    request.getContentType(),
                    request.getMaxDistance(),
                    request.getLimit());
        } else {
            results = embeddingService.findSimilar(
                    request.getQuery(),
                    request.getContentType(),
                    request.getLimit());
        }

        List<DocumentEmbeddingResponse> response = results.stream()
                .map(DocumentEmbeddingResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Adiciona documento com embedding.
     *
     * @param request dados do documento
     * @return documento criado
     */
    @PostMapping("/documents/embed")
    public ResponseEntity<DocumentEmbeddingResponse> addDocument(
            @Valid @RequestBody DocumentEmbeddingRequest request) {
        log.info("Adicionando documento: type={}", request.getContentType());

        DocumentEmbedding document = embeddingService.saveDocument(
                request.getContent(),
                request.getContentType(),
                request.getMetadata());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DocumentEmbeddingResponse.fromEntity(document));
    }

    /**
     * Obtém documento por ID.
     *
     * @param id ID do documento
     * @return documento ou 404
     */
    @GetMapping("/documents/{id}")
    public ResponseEntity<DocumentEmbeddingResponse> getDocument(@PathVariable UUID id) {
        DocumentEmbedding document = embeddingService.getDocument(id);

        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(DocumentEmbeddingResponse.fromEntity(document));
    }

    /**
     * Lista documentos por tipo.
     *
     * @param contentType tipo de conteúdo
     * @return lista de documentos
     */
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentEmbeddingResponse>> listDocuments(
            @RequestParam(required = false) String contentType) {
        List<DocumentEmbedding> documents;

        if (contentType != null) {
            documents = embeddingService.listByType(contentType);
        } else {
            documents = embeddingService.listByType(null);
        }

        List<DocumentEmbeddingResponse> response = documents.stream()
                .map(DocumentEmbeddingResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza embedding de um documento.
     *
     * @param id ID do documento
     * @return documento atualizado ou 404 se não encontrado
     */
    @PutMapping("/documents/{id}/embedding")
    public ResponseEntity<DocumentEmbeddingResponse> updateEmbedding(@PathVariable UUID id) {
        try {
            DocumentEmbedding document = embeddingService.updateEmbedding(id);
            return ResponseEntity.ok(DocumentEmbeddingResponse.fromEntity(document));
        } catch (DocumentNotFoundException e) {
            log.warn("Documento não encontrado para atualização de embedding: {}", id);
            return ResponseEntity.notFound().build();
        }
        // Outros erros (infraestrutura, etc.) propagam para GlobalExceptionHandler → 500
    }

    /**
     * Remove documento.
     *
     * @param id ID do documento
     * @return 204 No Content
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        embeddingService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
