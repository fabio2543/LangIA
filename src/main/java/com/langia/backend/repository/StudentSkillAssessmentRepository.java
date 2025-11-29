package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.StudentSkillAssessment;

/**
 * Repository for StudentSkillAssessment entity.
 */
@Repository
public interface StudentSkillAssessmentRepository extends JpaRepository<StudentSkillAssessment, UUID> {

    List<StudentSkillAssessment> findByUserIdOrderByAssessedAtDesc(UUID userId);

    Optional<StudentSkillAssessment> findFirstByUserIdAndLanguageOrderByAssessedAtDesc(UUID userId, String language);

    List<StudentSkillAssessment> findByUserIdAndLanguageOrderByAssessedAtDesc(UUID userId, String language);

    void deleteByUserId(UUID userId);
}
