package com.langia.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.NotificationSettingsEntity;

/**
 * Repository for NotificationSettingsEntity.
 */
@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettingsEntity, UUID> {

    Optional<NotificationSettingsEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
