package com.langia.backend.repository;

import com.langia.backend.model.DailyActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyActivityLogRepository extends JpaRepository<DailyActivityLog, UUID> {

    Optional<DailyActivityLog> findByUserIdAndLanguageCodeAndActivityDate(UUID userId, String languageCode, LocalDate activityDate);

    List<DailyActivityLog> findByUserIdAndActivityDateBetweenOrderByActivityDate(UUID userId, LocalDate startDate, LocalDate endDate);

    List<DailyActivityLog> findByUserIdAndLanguageCodeAndActivityDateBetweenOrderByActivityDate(UUID userId, String languageCode, LocalDate startDate, LocalDate endDate);

    @Query("SELECT dal FROM DailyActivityLog dal WHERE dal.user.id = :userId ORDER BY dal.activityDate DESC")
    List<DailyActivityLog> findRecentActivity(@Param("userId") UUID userId);

    @Query("SELECT SUM(dal.lessonsCompleted), SUM(dal.exercisesCompleted), SUM(dal.cardsReviewed), SUM(dal.minutesStudied), SUM(dal.xpEarned) FROM DailyActivityLog dal WHERE dal.user.id = :userId AND dal.activityDate >= :since")
    List<Object[]> getActivitySummary(@Param("userId") UUID userId, @Param("since") LocalDate since);

    @Query("SELECT dal.activityDate, SUM(dal.minutesStudied), SUM(dal.xpEarned) FROM DailyActivityLog dal WHERE dal.user.id = :userId AND dal.activityDate >= :since GROUP BY dal.activityDate ORDER BY dal.activityDate")
    List<Object[]> getDailyActivitySummary(@Param("userId") UUID userId, @Param("since") LocalDate since);

    @Query("SELECT COUNT(DISTINCT dal.activityDate) FROM DailyActivityLog dal WHERE dal.user.id = :userId AND dal.activityDate >= :since")
    long countActiveDays(@Param("userId") UUID userId, @Param("since") LocalDate since);

    @Query("SELECT AVG(dal.minutesStudied) FROM DailyActivityLog dal WHERE dal.user.id = :userId AND dal.activityDate >= :since")
    Double getAverageStudyTime(@Param("userId") UUID userId, @Param("since") LocalDate since);

    void deleteByUserId(UUID userId);
}
