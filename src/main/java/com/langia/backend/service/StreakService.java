package com.langia.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.ActivitySummaryDTO;
import com.langia.backend.dto.DailyActivityLogDTO;
import com.langia.backend.dto.DailyStreakDTO;
import com.langia.backend.exception.BusinessException;
import com.langia.backend.exception.ResourceNotFoundException;
import com.langia.backend.model.DailyActivityLog;
import com.langia.backend.model.DailyStreak;
import com.langia.backend.model.User;
import com.langia.backend.repository.DailyActivityLogRepository;
import com.langia.backend.repository.DailyStreakRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para gerenciamento de streaks e atividade diária.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {

    private final DailyStreakRepository streakRepository;
    private final DailyActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * Busca streak do usuário para um idioma.
     */
    public DailyStreakDTO getStreak(UUID userId, String languageCode) {
        DailyStreak streak = streakRepository.findByUserIdAndLanguageCode(userId, languageCode)
                .orElseGet(() -> {
                    DailyStreak newStreak = new DailyStreak();
                    newStreak.setLanguageCode(languageCode);
                    newStreak.setCurrentStreak(0);
                    newStreak.setLongestStreak(0);
                    newStreak.setTotalStudyDays(0);
                    return newStreak;
                });

        return DailyStreakDTO.fromEntity(streak);
    }

    /**
     * Busca todas as streaks do usuário.
     */
    public List<DailyStreakDTO> getAllStreaks(UUID userId) {
        return streakRepository.findByUserId(userId).stream()
                .map(DailyStreakDTO::fromEntity)
                .toList();
    }

    /**
     * Congela a streak por um dia.
     */
    @Transactional
    public DailyStreakDTO freezeStreak(UUID userId, String languageCode) {
        DailyStreak streak = streakRepository.findByUserIdAndLanguageCode(userId, languageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Streak not found for language: " + languageCode));

        if (streak.getStreakFrozenUntil() != null && streak.getStreakFrozenUntil().isAfter(LocalDate.now())) {
            throw new BusinessException("Streak is already frozen");
        }

        streak.setStreakFrozenUntil(LocalDate.now().plusDays(1));
        streak = streakRepository.save(streak);

        log.info("User {} froze streak for language {}", userId, languageCode);
        return DailyStreakDTO.fromEntity(streak);
    }

    /**
     * Busca log de atividade diária.
     */
    public List<DailyActivityLogDTO> getActivityLog(UUID userId, String languageCode, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDate endDate = LocalDate.now();

        return activityLogRepository.findByUserIdAndLanguageCodeAndActivityDateBetweenOrderByActivityDate(
                userId, languageCode, startDate, endDate)
                .stream()
                .map(DailyActivityLogDTO::fromEntity)
                .toList();
    }

    /**
     * Busca resumo de atividade.
     */
    public ActivitySummaryDTO getActivitySummary(UUID userId, String languageCode, LocalDate since) {
        LocalDate startDate = since != null ? since : LocalDate.now().minusDays(30);

        List<Object[]> results = activityLogRepository.getActivitySummary(userId, startDate);

        if (results.isEmpty() || results.get(0)[0] == null) {
            return ActivitySummaryDTO.builder()
                    .totalLessons(0)
                    .totalExercises(0)
                    .totalCardsReviewed(0)
                    .totalMinutes(0)
                    .totalXp(0)
                    .activeDays(0)
                    .avgMinutesPerDay(0)
                    .build();
        }

        Object[] row = results.get(0);
        long activeDays = activityLogRepository.countActiveDays(userId, startDate);
        Double avgMinutes = activityLogRepository.getAverageStudyTime(userId, startDate);

        return ActivitySummaryDTO.builder()
                .totalLessons(((Number) row[0]).intValue())
                .totalExercises(((Number) row[1]).intValue())
                .totalCardsReviewed(((Number) row[2]).intValue())
                .totalMinutes(((Number) row[3]).intValue())
                .totalXp(((Number) row[4]).intValue())
                .activeDays((int) activeDays)
                .avgMinutesPerDay(avgMinutes != null ? avgMinutes : 0)
                .build();
    }

    /**
     * Registra atividade de estudo.
     */
    @Transactional
    public DailyActivityLogDTO logActivity(UUID userId, String languageCode, int lessonsCompleted,
            int exercisesCompleted, int cardsReviewed, int minutesStudied, int xpEarned, List<String> skillsPracticed) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate today = LocalDate.now();

        DailyActivityLog activity = activityLogRepository
                .findByUserIdAndLanguageCodeAndActivityDate(userId, languageCode, today)
                .orElseGet(() -> {
                    DailyActivityLog newActivity = new DailyActivityLog();
                    newActivity.setUser(user);
                    newActivity.setLanguageCode(languageCode);
                    newActivity.setActivityDate(today);
                    return newActivity;
                });

        activity.setLessonsCompleted(activity.getLessonsCompleted() + lessonsCompleted);
        activity.setExercisesCompleted(activity.getExercisesCompleted() + exercisesCompleted);
        activity.setCardsReviewed(activity.getCardsReviewed() + cardsReviewed);
        activity.setMinutesStudied(activity.getMinutesStudied() + minutesStudied);
        activity.setXpEarned(activity.getXpEarned() + xpEarned);

        if (skillsPracticed != null && !skillsPracticed.isEmpty()) {
            List<String> currentSkills = activity.getSkillsPracticed();
            for (String skill : skillsPracticed) {
                if (!currentSkills.contains(skill)) {
                    currentSkills.add(skill);
                }
            }
            activity.setSkillsPracticed(currentSkills);
        }

        activity = activityLogRepository.save(activity);

        // Atualiza streak
        updateStreak(userId, languageCode);

        log.info("User {} logged activity for language {}: {} lessons, {} exercises, {} minutes",
                userId, languageCode, lessonsCompleted, exercisesCompleted, minutesStudied);

        return DailyActivityLogDTO.fromEntity(activity);
    }

    /**
     * Atualiza streak do usuário.
     */
    @Transactional
    public void updateStreak(UUID userId, String languageCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate today = LocalDate.now();

        DailyStreak streak = streakRepository.findByUserIdAndLanguageCode(userId, languageCode)
                .orElseGet(() -> {
                    DailyStreak newStreak = new DailyStreak();
                    newStreak.setUser(user);
                    newStreak.setLanguageCode(languageCode);
                    newStreak.setCurrentStreak(0);
                    newStreak.setLongestStreak(0);
                    newStreak.setTotalStudyDays(0);
                    return newStreak;
                });

        // Se já estudou hoje, não faz nada
        if (today.equals(streak.getLastStudyDate())) {
            return;
        }

        // Verifica se manteve o streak (estudou ontem ou está congelado)
        LocalDate yesterday = today.minusDays(1);
        boolean maintainedStreak = yesterday.equals(streak.getLastStudyDate()) ||
                (streak.getStreakFrozenUntil() != null && streak.getStreakFrozenUntil().isAfter(yesterday));

        if (maintainedStreak) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
            streak.setStreakStartedAt(today);
        }

        streak.setLastStudyDate(today);
        streak.setTotalStudyDays(streak.getTotalStudyDays() + 1);

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streakRepository.save(streak);
        log.info("User {} streak for {}: {} days", userId, languageCode, streak.getCurrentStreak());
    }
}
