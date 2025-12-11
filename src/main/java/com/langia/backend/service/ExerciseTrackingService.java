package com.langia.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.ErrorPatternDTO;
import com.langia.backend.exception.ResourceNotFoundException;
import com.langia.backend.model.ErrorPattern;
import com.langia.backend.model.ExerciseResponse;
import com.langia.backend.model.User;
import com.langia.backend.repository.ErrorPatternRepository;
import com.langia.backend.repository.ExerciseResponseRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para tracking de exercícios e padrões de erros.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseTrackingService {

    private final ExerciseResponseRepository exerciseResponseRepository;
    private final ErrorPatternRepository errorPatternRepository;
    private final UserRepository userRepository;

    /**
     * Busca histórico de respostas de exercícios.
     */
    public List<ExerciseResponse> getHistory(UUID userId, String languageCode, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        return exerciseResponseRepository.findRecentByUserAndLanguage(userId, languageCode, since)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Busca padrões de erros do usuário.
     */
    public List<ErrorPatternDTO> getErrorPatterns(UUID userId, String languageCode) {
        return errorPatternRepository.findUnresolvedByUserAndLanguage(userId, languageCode)
                .stream()
                .map(ErrorPatternDTO::fromEntity)
                .toList();
    }

    /**
     * Busca top N erros mais frequentes.
     */
    public List<ErrorPatternDTO> getTopErrors(UUID userId, String languageCode, int limit) {
        return errorPatternRepository.findTopErrorsByUserAndLanguage(userId, languageCode, limit)
                .stream()
                .map(ErrorPatternDTO::fromEntity)
                .toList();
    }

    /**
     * Marca um padrão de erro como resolvido.
     */
    @Transactional
    public void markErrorResolved(UUID userId, UUID errorId) {
        ErrorPattern error = errorPatternRepository.findById(errorId)
                .orElseThrow(() -> new ResourceNotFoundException("Error pattern not found: " + errorId));

        if (!error.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Error pattern not found: " + errorId);
        }

        error.setIsResolved(true);
        errorPatternRepository.save(error);

        log.info("User {} marked error {} as resolved", userId, errorId);
    }

    /**
     * Submete resposta de exercício.
     */
    @Transactional
    public ExerciseResponse submitResponse(UUID userId, UUID lessonId, UUID exerciseId,
            String exerciseType, String skillType, String languageCode,
            String userResponse, String correctResponse, boolean isCorrect,
            Integer responseTimeMs, int hintsUsed) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ExerciseResponse response = new ExerciseResponse();
        response.setUser(user);
        response.setLessonId(lessonId);
        response.setExerciseId(exerciseId);
        response.setExerciseType(exerciseType);
        response.setSkillType(skillType);
        response.setLanguageCode(languageCode);
        response.setUserResponse(userResponse);
        response.setCorrectResponse(correctResponse);
        response.setIsCorrect(isCorrect);
        response.setResponseTimeMs(responseTimeMs);
        response.setHintsUsed(hintsUsed);

        response = exerciseResponseRepository.save(response);

        log.info("User {} submitted exercise response: {} (correct: {})", userId, exerciseId, isCorrect);

        return response;
    }
}
