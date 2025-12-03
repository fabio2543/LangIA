package com.langia.backend.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.onboarding.OnboardingCompleteResponseDTO;
import com.langia.backend.dto.onboarding.OnboardingStatusDTO;
import com.langia.backend.dto.onboarding.OnboardingStatusDTO.OnboardingStepsStatus;
import com.langia.backend.dto.trail.TrailDTO;
import com.langia.backend.model.StudentLanguageEnrollment;
import com.langia.backend.model.User;
import com.langia.backend.repository.StudentLanguageEnrollmentRepository;
import com.langia.backend.repository.StudentLearningPreferencesRepository;
import com.langia.backend.repository.StudentSkillAssessmentRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerenciamento do processo de onboarding.
 * Orquestra a verificação de etapas e conclusão do onboarding.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserRepository userRepository;
    private final StudentLanguageEnrollmentRepository languageEnrollmentRepository;
    private final StudentLearningPreferencesRepository preferencesRepository;
    private final StudentSkillAssessmentRepository assessmentRepository;
    private final TrailService trailService;

    /**
     * Obtém o status atual do onboarding do usuário.
     *
     * @param userId ID do usuário
     * @return Status detalhado do onboarding
     */
    @Transactional(readOnly = true)
    public OnboardingStatusDTO getStatus(UUID userId) {
        log.debug("Verificando status de onboarding para usuário: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));

        // Se já completou, retorna imediatamente
        if (user.isOnboardingCompleted()) {
            return OnboardingStatusDTO.builder()
                    .completed(true)
                    .steps(OnboardingStepsStatus.builder()
                            .profileComplete(true)
                            .languageEnrolled(true)
                            .preferencesSet(true)
                            .assessmentDone(true)
                            .build())
                    .nextStep(null)
                    .build();
        }

        // Verificar cada etapa
        boolean hasProfile = user.getName() != null && !user.getName().isBlank();
        boolean hasLanguage = languageEnrollmentRepository.countByUserId(userId) > 0;
        boolean hasPreferences = preferencesRepository.existsByUserId(userId);
        boolean hasAssessment = !assessmentRepository.findByUserIdOrderByAssessedAtDesc(userId).isEmpty();

        // Determinar próxima etapa
        String nextStep = null;
        if (!hasProfile) {
            nextStep = "welcome";
        } else if (!hasLanguage) {
            nextStep = "language";
        } else if (!hasPreferences) {
            nextStep = "preferences";
        } else if (!hasAssessment) {
            nextStep = "assessment";
        } else {
            nextStep = "complete";
        }

        return OnboardingStatusDTO.builder()
                .completed(false)
                .steps(OnboardingStepsStatus.builder()
                        .profileComplete(hasProfile)
                        .languageEnrolled(hasLanguage)
                        .preferencesSet(hasPreferences)
                        .assessmentDone(hasAssessment)
                        .build())
                .nextStep(nextStep)
                .build();
    }

    /**
     * Completa o processo de onboarding.
     * Valida que todas as etapas foram concluídas e gera a trilha inicial.
     *
     * @param userId ID do usuário
     * @return Resposta com informações da trilha gerada
     */
    @Transactional
    public OnboardingCompleteResponseDTO complete(UUID userId) {
        log.info("Completando onboarding para usuário: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));

        // Verificar se já completou
        if (user.isOnboardingCompleted()) {
            log.info("Onboarding já completado para usuário: {}", userId);
            return OnboardingCompleteResponseDTO.builder()
                    .success(true)
                    .message("Onboarding já foi completado anteriormente")
                    .redirectUrl("/dashboard")
                    .build();
        }

        // Verificar todas as etapas
        OnboardingStatusDTO status = getStatus(userId);
        OnboardingStepsStatus steps = status.getSteps();

        if (!steps.isLanguageEnrolled()) {
            log.warn("Tentativa de completar onboarding sem idioma inscrito: {}", userId);
            return OnboardingCompleteResponseDTO.builder()
                    .success(false)
                    .message("É necessário selecionar pelo menos um idioma para estudar")
                    .build();
        }

        // Buscar idioma primário para gerar trilha
        Optional<StudentLanguageEnrollment> primaryLanguage =
                languageEnrollmentRepository.findByUserIdAndIsPrimaryTrue(userId);

        if (primaryLanguage.isEmpty()) {
            log.warn("Nenhum idioma primário encontrado para usuário: {}", userId);
            return OnboardingCompleteResponseDTO.builder()
                    .success(false)
                    .message("Nenhum idioma primário configurado")
                    .build();
        }

        String languageCode = primaryLanguage.get().getLanguage().getCode();

        // Marcar onboarding como completo
        user.setOnboardingCompleted(true);
        user.setOnboardingCompletedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Onboarding marcado como completo para usuário: {}", userId);

        // Gerar trilha para o idioma primário
        TrailDTO trail = null;
        try {
            trail = trailService.getOrCreateTrail(userId, languageCode);
            log.info("Trilha gerada com sucesso: {} para idioma: {}", trail.getId(), languageCode);
        } catch (Exception e) {
            log.error("Erro ao gerar trilha durante onboarding: {}", e.getMessage(), e);
            // Não falha o onboarding se a trilha não for gerada
        }

        return OnboardingCompleteResponseDTO.builder()
                .success(true)
                .message("Onboarding completado com sucesso! Sua trilha de aprendizado está pronta.")
                .trailId(trail != null ? trail.getId() : null)
                .languageCode(languageCode)
                .redirectUrl(trail != null ? "/trails/" + trail.getId() : "/trails")
                .build();
    }

    /**
     * Verifica se o usuário precisa completar o onboarding.
     *
     * @param userId ID do usuário
     * @return true se o onboarding ainda não foi completado
     */
    @Transactional(readOnly = true)
    public boolean needsOnboarding(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> !user.isOnboardingCompleted())
                .orElse(false);
    }
}
