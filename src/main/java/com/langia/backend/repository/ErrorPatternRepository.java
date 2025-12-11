package com.langia.backend.repository;

import com.langia.backend.model.ErrorPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ErrorPatternRepository extends JpaRepository<ErrorPattern, UUID> {

    List<ErrorPattern> findByUserId(UUID userId);

    List<ErrorPattern> findByUserIdAndLanguageCode(UUID userId, String languageCode);

    Optional<ErrorPattern> findByUserIdAndLanguageCodeAndErrorCategory(UUID userId, String languageCode, String errorCategory);

    List<ErrorPattern> findByUserIdAndIsResolvedFalseOrderByOccurrenceCountDesc(UUID userId);

    @Query("SELECT ep FROM ErrorPattern ep WHERE ep.user.id = :userId AND ep.languageCode = :languageCode AND ep.isResolved = false ORDER BY ep.occurrenceCount DESC")
    List<ErrorPattern> findUnresolvedByUserAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query(value = "SELECT * FROM error_patterns WHERE user_id = :userId AND language_code = :languageCode AND is_resolved = false ORDER BY occurrence_count DESC LIMIT :limit", nativeQuery = true)
    List<ErrorPattern> findTopErrorsByUserAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("limit") int limit);

    @Query("SELECT ep FROM ErrorPattern ep WHERE ep.user.id = :userId AND ep.skillType = :skillType AND ep.isResolved = false ORDER BY ep.occurrenceCount DESC")
    List<ErrorPattern> findByUserAndSkillType(@Param("userId") UUID userId, @Param("skillType") String skillType);

    @Query("SELECT ep.skillType, COUNT(ep), SUM(ep.occurrenceCount) FROM ErrorPattern ep WHERE ep.user.id = :userId AND ep.isResolved = false GROUP BY ep.skillType ORDER BY SUM(ep.occurrenceCount) DESC")
    List<Object[]> getErrorStatsBySkill(@Param("userId") UUID userId);

    long countByUserIdAndIsResolvedFalse(UUID userId);

    void deleteByUserId(UUID userId);
}
