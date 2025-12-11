package com.langia.backend.repository;

import com.langia.backend.model.DailyStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyStreakRepository extends JpaRepository<DailyStreak, UUID> {

    Optional<DailyStreak> findByUserIdAndLanguageCode(UUID userId, String languageCode);

    List<DailyStreak> findByUserId(UUID userId);

    @Query("SELECT ds FROM DailyStreak ds WHERE ds.user.id = :userId ORDER BY ds.currentStreak DESC")
    List<DailyStreak> findByUserIdOrderByStreakDesc(@Param("userId") UUID userId);

    @Query("SELECT ds FROM DailyStreak ds WHERE ds.languageCode = :languageCode AND ds.currentStreak > 0 ORDER BY ds.currentStreak DESC")
    List<DailyStreak> findTopStreaksByLanguage(@Param("languageCode") String languageCode);

    @Query("SELECT ds FROM DailyStreak ds WHERE ds.lastStudyDate < :date AND ds.currentStreak > 0 AND (ds.streakFrozenUntil IS NULL OR ds.streakFrozenUntil < :date)")
    List<DailyStreak> findStreaksToBreak(@Param("date") LocalDate date);

    @Query("SELECT ds FROM DailyStreak ds WHERE ds.user.id = :userId AND ds.lastStudyDate = :yesterday AND ds.currentStreak > 0")
    List<DailyStreak> findStreaksAtRisk(@Param("userId") UUID userId, @Param("yesterday") LocalDate yesterday);

    @Query("SELECT MAX(ds.longestStreak) FROM DailyStreak ds WHERE ds.user.id = :userId")
    Integer getMaxLongestStreak(@Param("userId") UUID userId);

    @Query("SELECT SUM(ds.totalStudyDays) FROM DailyStreak ds WHERE ds.user.id = :userId")
    Integer getTotalStudyDays(@Param("userId") UUID userId);

    void deleteByUserId(UUID userId);
}
