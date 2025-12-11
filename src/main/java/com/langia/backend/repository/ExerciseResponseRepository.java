package com.langia.backend.repository;

import com.langia.backend.model.ExerciseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseResponseRepository extends JpaRepository<ExerciseResponse, UUID> {

    List<ExerciseResponse> findByUserId(UUID userId);

    Page<ExerciseResponse> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<ExerciseResponse> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<ExerciseResponse> findByUserIdAndSkillType(UUID userId, String skillType);

    List<ExerciseResponse> findByUserIdAndIsCorrectFalse(UUID userId);

    @Query("SELECT er FROM ExerciseResponse er WHERE er.user.id = :userId AND er.languageCode = :languageCode AND er.createdAt >= :since ORDER BY er.createdAt DESC")
    List<ExerciseResponse> findRecentByUserAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("since") LocalDateTime since);

    @Query("SELECT er.skillType, COUNT(er), SUM(CASE WHEN er.isCorrect = true THEN 1 ELSE 0 END) FROM ExerciseResponse er WHERE er.user.id = :userId AND er.languageCode = :languageCode GROUP BY er.skillType")
    List<Object[]> getSkillStatistics(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT AVG(er.responseTimeMs) FROM ExerciseResponse er WHERE er.user.id = :userId AND er.skillType = :skillType AND er.responseTimeMs IS NOT NULL")
    Double getAverageResponseTime(@Param("userId") UUID userId, @Param("skillType") String skillType);

    @Query("SELECT COUNT(er) FROM ExerciseResponse er WHERE er.user.id = :userId AND er.createdAt >= :since")
    long countExercisesSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(er) FROM ExerciseResponse er WHERE er.user.id = :userId AND er.isCorrect = true AND er.createdAt >= :since")
    long countCorrectExercisesSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    void deleteByUserId(UUID userId);
}
