package com.langia.backend.repository;

import com.langia.backend.model.SkillMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillMetricRepository extends JpaRepository<SkillMetric, UUID> {

    Optional<SkillMetric> findByUserIdAndLanguageCodeAndSkillTypeAndMetricDate(UUID userId, String languageCode, String skillType, LocalDate metricDate);

    List<SkillMetric> findByUserIdAndLanguageCodeAndMetricDateBetweenOrderByMetricDate(UUID userId, String languageCode, LocalDate startDate, LocalDate endDate);

    List<SkillMetric> findByUserIdAndSkillTypeOrderByMetricDateDesc(UUID userId, String skillType);

    @Query("SELECT sm FROM SkillMetric sm WHERE sm.user.id = :userId AND sm.languageCode = :languageCode AND sm.metricDate >= :since ORDER BY sm.metricDate DESC")
    List<SkillMetric> findRecentMetrics(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("since") LocalDate since);

    @Query("SELECT sm.skillType, AVG(sm.accuracyPercentage), AVG(sm.avgResponseTimeMs) FROM SkillMetric sm WHERE sm.user.id = :userId AND sm.languageCode = :languageCode AND sm.metricDate >= :since GROUP BY sm.skillType")
    List<Object[]> getAverageMetricsBySkill(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("since") LocalDate since);

    @Query("SELECT SUM(sm.exercisesCompleted), SUM(sm.correctAnswers), SUM(sm.totalPracticeTimeMinutes), SUM(sm.xpEarned) FROM SkillMetric sm WHERE sm.user.id = :userId AND sm.metricDate >= :since")
    List<Object[]> getTotalStats(@Param("userId") UUID userId, @Param("since") LocalDate since);

    @Query("SELECT sm.metricDate, SUM(sm.exercisesCompleted), AVG(sm.accuracyPercentage) FROM SkillMetric sm WHERE sm.user.id = :userId AND sm.languageCode = :languageCode AND sm.metricDate >= :since GROUP BY sm.metricDate ORDER BY sm.metricDate")
    List<Object[]> getDailyProgress(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("since") LocalDate since);

    void deleteByUserId(UUID userId);
}
