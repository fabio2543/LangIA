package com.langia.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.EnrollLanguageRequest;
import com.langia.backend.dto.LanguageDTO;
import com.langia.backend.dto.LanguageEnrollmentDTO;
import com.langia.backend.dto.UpdateLanguageEnrollmentRequest;
import com.langia.backend.exception.BusinessException;
import com.langia.backend.exception.ResourceNotFoundException;
import com.langia.backend.model.Language;
import com.langia.backend.model.StudentLanguageEnrollment;
import com.langia.backend.model.User;
import com.langia.backend.repository.LanguageRepository;
import com.langia.backend.repository.StudentLanguageEnrollmentRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para gerenciamento de idiomas de estudantes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLanguageService {

    private static final int MAX_LANGUAGES = 3;

    private final StudentLanguageEnrollmentRepository enrollmentRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;

    /**
     * Lista todos os idiomas disponíveis na plataforma.
     */
    public List<LanguageDTO> getAvailableLanguages() {
        return languageRepository.findByActiveTrueOrderByNamePtAsc()
                .stream()
                .map(LanguageDTO::fromEntity)
                .toList();
    }

    /**
     * Lista os enrollments de idiomas de um usuário.
     */
    public List<LanguageEnrollmentDTO> getEnrollments(UUID userId) {
        return enrollmentRepository.findByUserIdOrderByPrimaryFirst(userId)
                .stream()
                .map(LanguageEnrollmentDTO::fromEntity)
                .toList();
    }

    /**
     * Adiciona um novo idioma para o estudante.
     */
    @Transactional
    public LanguageEnrollmentDTO enroll(UUID userId, EnrollLanguageRequest request) {
        // Verifica se usuário existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verifica se idioma existe e está ativo
        Language language = languageRepository.findById(request.getLanguageCode())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found: " + request.getLanguageCode()));

        if (!language.isActive()) {
            throw new BusinessException("Language is not available: " + request.getLanguageCode());
        }

        // Verifica se usuário já tem este idioma
        if (enrollmentRepository.existsByUserIdAndLanguageCode(userId, request.getLanguageCode())) {
            throw new BusinessException("User already enrolled in this language");
        }

        // Verifica limite de 3 idiomas
        long currentCount = enrollmentRepository.countByUserId(userId);
        if (currentCount >= MAX_LANGUAGES) {
            throw new BusinessException("Maximum of " + MAX_LANGUAGES + " languages allowed per student");
        }

        // Cria enrollment
        StudentLanguageEnrollment enrollment = StudentLanguageEnrollment.builder()
                .user(user)
                .language(language)
                .cefrLevel(request.getCefrLevel())
                .isPrimary(request.isPrimary() || currentCount == 0) // Primeiro idioma é primário por padrão
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("User {} enrolled in language {}", userId, request.getLanguageCode());

        return LanguageEnrollmentDTO.fromEntity(enrollment);
    }

    /**
     * Atualiza o enrollment de um idioma.
     */
    @Transactional
    public LanguageEnrollmentDTO updateEnrollment(UUID userId, String languageCode, UpdateLanguageEnrollmentRequest request) {
        StudentLanguageEnrollment enrollment = enrollmentRepository.findByUserIdAndLanguageCode(userId, languageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for language: " + languageCode));

        if (request.getCefrLevel() != null) {
            enrollment.setCefrLevel(request.getCefrLevel());
        }

        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            enrollment.setPrimary(true);
            // Trigger no banco garante que outros idiomas perdem o flag primário
        }

        enrollment = enrollmentRepository.save(enrollment);
        log.info("User {} updated enrollment for language {}", userId, languageCode);

        return LanguageEnrollmentDTO.fromEntity(enrollment);
    }

    /**
     * Remove um idioma do estudante.
     */
    @Transactional
    public void unenroll(UUID userId, String languageCode) {
        StudentLanguageEnrollment enrollment = enrollmentRepository.findByUserIdAndLanguageCode(userId, languageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for language: " + languageCode));

        // Se era primário e há outros idiomas, promove o próximo
        if (enrollment.isPrimary()) {
            List<StudentLanguageEnrollment> others = enrollmentRepository.findByUserId(userId)
                    .stream()
                    .filter(e -> !e.getId().equals(enrollment.getId()))
                    .toList();

            if (!others.isEmpty()) {
                StudentLanguageEnrollment newPrimary = others.get(0);
                newPrimary.setPrimary(true);
                enrollmentRepository.save(newPrimary);
            }
        }

        enrollmentRepository.delete(enrollment);
        log.info("User {} unenrolled from language {}", userId, languageCode);
    }

    /**
     * Define um idioma como primário.
     */
    @Transactional
    public LanguageEnrollmentDTO setPrimary(UUID userId, String languageCode) {
        StudentLanguageEnrollment enrollment = enrollmentRepository.findByUserIdAndLanguageCode(userId, languageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for language: " + languageCode));

        enrollment.setPrimary(true);
        enrollment = enrollmentRepository.save(enrollment);
        // Trigger no banco garante que outros idiomas perdem o flag primário

        log.info("User {} set language {} as primary", userId, languageCode);
        return LanguageEnrollmentDTO.fromEntity(enrollment);
    }
}
