package com.langia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.UserProfileDetails;

/**
 * Repository for UserProfileDetails entity.
 */
@Repository
public interface UserProfileDetailsRepository extends JpaRepository<UserProfileDetails, UUID> {

    Optional<UserProfileDetails> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
