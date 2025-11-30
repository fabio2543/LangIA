package com.langia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.StudentLearningPreferences;

/**
 * Repository for StudentLearningPreferences entity.
 */
@Repository
public interface StudentLearningPreferencesRepository extends JpaRepository<StudentLearningPreferences, UUID> {

    Optional<StudentLearningPreferences> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
