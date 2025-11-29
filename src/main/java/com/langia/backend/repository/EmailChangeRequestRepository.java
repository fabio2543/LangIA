package com.langia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.EmailChangeRequest;

/**
 * Repository for EmailChangeRequest entity.
 */
@Repository
public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, UUID> {

    Optional<EmailChangeRequest> findByTokenHash(String tokenHash);

    /**
     * Find all active (not used, not expired) email change requests for a user.
     * This is an optimized query that only fetches relevant records.
     */
    @Query("SELECT e FROM EmailChangeRequest e WHERE e.user.id = :userId AND e.usedAt IS NULL AND e.expiresAt > :now")
    List<EmailChangeRequest> findActiveRequestsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM EmailChangeRequest e WHERE e.user.id = :userId AND e.usedAt IS NULL AND e.expiresAt > :now")
    long countActiveRequestsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE EmailChangeRequest e SET e.usedAt = :now WHERE e.user.id = :userId AND e.usedAt IS NULL")
    int invalidateAllUserRequests(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    void deleteByUserId(UUID userId);
}
