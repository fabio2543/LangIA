package com.langia.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.LearningPreferencesDTO;
import com.langia.backend.dto.NotificationSettingsDTO;
import com.langia.backend.dto.RequestEmailChangeDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.SkillAssessmentDTO;
import com.langia.backend.dto.SkillAssessmentResponseDTO;
import com.langia.backend.dto.VerifyEmailChangeDTO;
import com.langia.backend.dto.student.PersonalDataDTO;
import com.langia.backend.dto.student.UpdatePersonalDataRequest;
import com.langia.backend.service.EmailChangeService;
import com.langia.backend.service.StudentProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller para gerenciamento de perfil do estudante.
 * Implementa os critérios de aceite AC-DP-001 a AC-DP-004.
 */
@RestController
@RequestMapping("/api/v1/students/me")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentProfileService profileService;
    private final EmailChangeService emailChangeService;

    // ========== Personal Data (AC-DP-001 to AC-DP-004) ==========

    /**
     * Obtém os dados pessoais do estudante autenticado.
     * Implementa AC-DP-001: Visualização de dados pessoais.
     *
     * @param session Dados da sessão do usuário autenticado
     * @return Dados pessoais do estudante
     */
    @GetMapping("/personal-data")
    public ResponseEntity<PersonalDataDTO> getPersonalData(
            @AuthenticationPrincipal SessionData session) {
        log.info("AC-DP-001: Getting personal data for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getPersonalData(session.getUserId()));
    }

    /**
     * Atualiza os dados pessoais do estudante autenticado.
     * Implementa:
     * - AC-DP-002: Atualização de nome válido
     * - AC-DP-003: Rejeição de nome inválido (validação automática)
     * - AC-DP-004: Validação de idade mínima (validação automática)
     *
     * @param session Dados da sessão do usuário autenticado
     * @param request Dados a serem atualizados (campos opcionais)
     * @return Dados pessoais atualizados
     */
    @PatchMapping("/personal-data")
    public ResponseEntity<PersonalDataDTO> updatePersonalData(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody UpdatePersonalDataRequest request) {
        log.info("AC-DP-002/003/004: Updating personal data for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.updatePersonalData(session.getUserId(), request));
    }

    // ========== Email Change (AC-EM-001 to AC-EM-003) ==========

    /**
     * Solicita alteração de e-mail do estudante.
     * Implementa AC-EM-001: Solicitação de alteração de e-mail.
     * Envia código de verificação de 6 dígitos para o novo e-mail.
     *
     * @param session Dados da sessão do usuário autenticado
     * @param request Novo e-mail desejado
     * @return Mensagem de confirmação
     */
    @PostMapping("/email/change-request")
    public ResponseEntity<Map<String, String>> requestEmailChange(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody RequestEmailChangeDTO request) {
        log.info("AC-EM-001: Email change requested for user {}", session.getUserId());
        emailChangeService.requestEmailChange(session.getUserId(), request.getNewEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Código de verificação enviado para o novo e-mail",
                "expiresInMinutes", "15"
        ));
    }

    /**
     * Confirma alteração de e-mail com código de verificação.
     * Implementa:
     * - AC-EM-002: Confirmação com código válido
     * - AC-EM-003: Rejeição de código inválido/expirado
     *
     * @param session Dados da sessão do usuário autenticado
     * @param request Código de verificação de 6 dígitos
     * @return Mensagem de confirmação
     */
    @PostMapping("/email/verify")
    public ResponseEntity<Map<String, String>> verifyEmailChange(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody VerifyEmailChangeDTO request) {
        log.info("AC-EM-002/003: Email change verification for user {}", session.getUserId());
        emailChangeService.confirmEmailChange(session.getUserId(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "E-mail alterado com sucesso"));
    }

    // ========== Learning Preferences (AC-LP-001 to AC-LP-003) ==========

    /**
     * Obtém as preferências de aprendizado do estudante.
     * Implementa AC-LP-001: Visualização de preferências.
     *
     * @param session Dados da sessão do usuário autenticado
     * @return Preferências de aprendizado
     */
    @GetMapping("/learning-preferences")
    public ResponseEntity<LearningPreferencesDTO> getLearningPreferences(
            @AuthenticationPrincipal SessionData session) {
        log.info("AC-LP-001: Getting learning preferences for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getLearningPreferences(session.getUserId()));
    }

    /**
     * Atualiza as preferências de aprendizado do estudante.
     * Implementa:
     * - AC-LP-002: Atualização de preferências válidas
     * - AC-LP-003: Validação de campos (dias, horas, formatos)
     *
     * @param session Dados da sessão do usuário autenticado
     * @param request Preferências a serem atualizadas
     * @return Preferências atualizadas
     */
    @PutMapping("/learning-preferences")
    public ResponseEntity<LearningPreferencesDTO> updateLearningPreferences(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody LearningPreferencesDTO request) {
        log.info("AC-LP-002/003: Updating learning preferences for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.updateLearningPreferences(session.getUserId(), request));
    }

    // ========== Skill Assessment (AC-SA-001 to AC-SA-003) ==========

    /**
     * Obtém as autoavaliações de habilidades do estudante.
     * Implementa AC-SA-001: Visualização de autoavaliações.
     *
     * @param session Dados da sessão do usuário autenticado
     * @return Lista de autoavaliações ordenadas por data (mais recente primeiro)
     */
    @GetMapping("/skill-assessments")
    public ResponseEntity<List<SkillAssessmentResponseDTO>> getSkillAssessments(
            @AuthenticationPrincipal SessionData session) {
        log.info("AC-SA-001: Getting skill assessments for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getSkillAssessments(session.getUserId()));
    }

    /**
     * Cria uma nova autoavaliação de habilidades.
     * Implementa:
     * - AC-SA-002: Criação de autoavaliação válida
     * - AC-SA-003: Validação de níveis de dificuldade e CEFR
     *
     * @param session Dados da sessão do usuário autenticado
     * @param request Dados da autoavaliação
     * @return Autoavaliação criada
     */
    @PostMapping("/skill-assessments")
    public ResponseEntity<SkillAssessmentResponseDTO> createSkillAssessment(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody SkillAssessmentDTO request) {
        log.info("AC-SA-002/003: Creating skill assessment for user {} in {}",
                session.getUserId(), request.getLanguage());
        return ResponseEntity.ok(profileService.createSkillAssessment(session.getUserId(), request));
    }

    // ========== Notification Settings (AC-NF-001 to AC-NF-002) ==========

    /**
     * Obtém as configurações de notificação do estudante.
     * Implementa AC-NF-001: Visualização de preferências de notificação.
     *
     * @param session Dados da sessão do usuário autenticado
     * @return Configurações de notificação
     */
    @GetMapping("/notifications")
    public ResponseEntity<NotificationSettingsDTO> getNotificationSettings(
            @AuthenticationPrincipal SessionData session) {
        log.info("AC-NF-001: Getting notification settings for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getNotificationSettings(session.getUserId()));
    }

    /**
     * Atualiza as configurações de notificação do estudante.
     * Implementa AC-NF-002: Atualização de preferências de notificação.
     *
     * @param session Dados da sessão do usuário autenticado
     * @param request Configurações a serem atualizadas
     * @return Configurações atualizadas
     */
    @PutMapping("/notifications")
    public ResponseEntity<NotificationSettingsDTO> updateNotificationSettings(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody NotificationSettingsDTO request) {
        log.info("AC-NF-002: Updating notification settings for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.updateNotificationSettings(session.getUserId(), request));
    }
}
