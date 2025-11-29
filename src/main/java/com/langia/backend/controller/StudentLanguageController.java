package com.langia.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.EnrollLanguageRequest;
import com.langia.backend.dto.LanguageDTO;
import com.langia.backend.dto.LanguageEnrollmentDTO;
import com.langia.backend.dto.MessageResponse;
import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.UpdateLanguageEnrollmentRequest;
import com.langia.backend.service.StudentLanguageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para gerenciamento de idiomas de estudantes.
 */
@RestController
@RequestMapping("/api/profile/languages")
@RequiredArgsConstructor
@Slf4j
public class StudentLanguageController {

    private final StudentLanguageService studentLanguageService;

    /**
     * Lista todos os idiomas disponíveis na plataforma.
     *
     * @return lista de idiomas disponíveis
     */
    @GetMapping("/available")
    public ResponseEntity<List<LanguageDTO>> getAvailableLanguages() {
        List<LanguageDTO> languages = studentLanguageService.getAvailableLanguages();
        return ResponseEntity.ok(languages);
    }

    /**
     * Lista os idiomas em que o usuário está matriculado.
     *
     * @param session sessão do usuário autenticado
     * @return lista de enrollments do usuário
     */
    @GetMapping
    public ResponseEntity<List<LanguageEnrollmentDTO>> getEnrollments(
            @AuthenticationPrincipal SessionData session) {
        List<LanguageEnrollmentDTO> enrollments = studentLanguageService.getEnrollments(session.getUserId());
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Adiciona um novo idioma para o estudante.
     *
     * @param session sessão do usuário autenticado
     * @param request dados do enrollment
     * @return enrollment criado
     */
    @PostMapping
    public ResponseEntity<LanguageEnrollmentDTO> enroll(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody EnrollLanguageRequest request) {
        log.info("User {} enrolling in language {}", session.getUserId(), request.getLanguageCode());

        LanguageEnrollmentDTO enrollment = studentLanguageService.enroll(session.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    /**
     * Atualiza o enrollment de um idioma (nível CEFR ou primário).
     *
     * @param session sessão do usuário autenticado
     * @param languageCode código do idioma
     * @param request dados para atualização
     * @return enrollment atualizado
     */
    @PutMapping("/{languageCode}")
    public ResponseEntity<LanguageEnrollmentDTO> updateEnrollment(
            @AuthenticationPrincipal SessionData session,
            @PathVariable String languageCode,
            @Valid @RequestBody UpdateLanguageEnrollmentRequest request) {
        log.info("User {} updating enrollment for language {}", session.getUserId(), languageCode);

        LanguageEnrollmentDTO enrollment = studentLanguageService.updateEnrollment(
                session.getUserId(), languageCode, request);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Remove um idioma do estudante.
     *
     * @param session sessão do usuário autenticado
     * @param languageCode código do idioma
     * @return mensagem de sucesso
     */
    @DeleteMapping("/{languageCode}")
    public ResponseEntity<MessageResponse> unenroll(
            @AuthenticationPrincipal SessionData session,
            @PathVariable String languageCode) {
        log.info("User {} unenrolling from language {}", session.getUserId(), languageCode);

        studentLanguageService.unenroll(session.getUserId(), languageCode);
        return ResponseEntity.ok(new MessageResponse("Language removed successfully"));
    }

    /**
     * Define um idioma como primário.
     *
     * @param session sessão do usuário autenticado
     * @param languageCode código do idioma
     * @return enrollment atualizado
     */
    @PutMapping("/{languageCode}/primary")
    public ResponseEntity<LanguageEnrollmentDTO> setPrimary(
            @AuthenticationPrincipal SessionData session,
            @PathVariable String languageCode) {
        log.info("User {} setting language {} as primary", session.getUserId(), languageCode);

        LanguageEnrollmentDTO enrollment = studentLanguageService.setPrimary(session.getUserId(), languageCode);
        return ResponseEntity.ok(enrollment);
    }
}
