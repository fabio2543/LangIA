package com.langia.backend.repository;

import com.langia.backend.model.SrsProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SrsProgressRepository extends JpaRepository<SrsProgress, UUID> {

    Optional<SrsProgress> findByUserIdAndCardId(UUID userId, UUID cardId);

    List<SrsProgress> findByUserId(UUID userId);

    List<SrsProgress> findByUserIdAndNextReviewDateLessThanEqual(UUID userId, LocalDate date);

    @Query("SELECT sp FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode AND sp.nextReviewDate <= :date ORDER BY sp.nextReviewDate, sp.easinessFactor")
    List<SrsProgress> findDueCards(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("date") LocalDate date);

    @Query("SELECT sp FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode AND sp.nextReviewDate <= CURRENT_DATE ORDER BY sp.nextReviewDate")
    List<SrsProgress> findDueToday(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT COUNT(sp) FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode AND sp.nextReviewDate <= CURRENT_DATE")
    long countDueToday(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT sp FROM SrsProgress sp WHERE sp.user.id = :userId AND sp.status = :status")
    List<SrsProgress> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT AVG(sp.easinessFactor) FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode")
    Double getAverageEasinessFactor(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT SUM(sp.totalReviews) FROM SrsProgress sp WHERE sp.user.id = :userId")
    Long getTotalReviews(@Param("userId") UUID userId);

    @Query("SELECT SUM(sp.correctReviews) FROM SrsProgress sp WHERE sp.user.id = :userId")
    Long getTotalCorrectReviews(@Param("userId") UUID userId);

    @Query("SELECT COUNT(sp) FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode AND sp.nextReviewDate <= :date")
    int countDueCards(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("date") LocalDate date);

    @Query("SELECT COUNT(sp) FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode AND DATE(sp.lastReviewedAt) = :date")
    int countReviewedToday(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("date") LocalDate date);

    @Query("SELECT COUNT(sp) FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode AND sp.intervalDays >= 21")
    int countMastered(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT COUNT(sp) FROM SrsProgress sp JOIN sp.card c WHERE sp.user.id = :userId AND c.languageCode = :languageCode AND sp.repetitions > 0 AND sp.intervalDays < 21")
    int countLearning(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    boolean existsByUserIdAndCardId(UUID userId, UUID cardId);

    void deleteByUserId(UUID userId);
}
