package com.langia.backend.repository;

import com.langia.backend.model.SocraticInteraction;
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
public interface SocraticInteractionRepository extends JpaRepository<SocraticInteraction, UUID> {

    List<SocraticInteraction> findByUserId(UUID userId);

    Page<SocraticInteraction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<SocraticInteraction> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<SocraticInteraction> findByUserIdAndSkillType(UUID userId, String skillType);

    @Query("SELECT si FROM SocraticInteraction si WHERE si.user.id = :userId AND si.languageCode = :languageCode AND si.createdAt >= :since ORDER BY si.createdAt DESC")
    List<SocraticInteraction> findRecentByUserAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("since") LocalDateTime since);

    @Query("SELECT si FROM SocraticInteraction si WHERE si.user.id = :userId AND si.selfCorrectionAchieved = true ORDER BY si.createdAt DESC")
    List<SocraticInteraction> findSuccessfulSelfCorrections(@Param("userId") UUID userId);

    @Query("SELECT COUNT(si), COUNT(CASE WHEN si.selfCorrectionAchieved = true THEN 1 END) FROM SocraticInteraction si WHERE si.user.id = :userId AND si.languageCode = :languageCode")
    List<Object[]> getSelfCorrectionStats(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT si.skillType, COUNT(si), AVG(si.interactionRounds) FROM SocraticInteraction si WHERE si.user.id = :userId GROUP BY si.skillType")
    List<Object[]> getInteractionStatsBySkill(@Param("userId") UUID userId);

    @Query("SELECT AVG(si.userRating) FROM SocraticInteraction si WHERE si.user.id = :userId AND si.userRating IS NOT NULL")
    Double getAverageUserRating(@Param("userId") UUID userId);

    @Query("SELECT SUM(si.tokensUsed) FROM SocraticInteraction si WHERE si.user.id = :userId AND si.createdAt >= :since")
    Long getTotalTokensUsed(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    void deleteByUserId(UUID userId);
}
