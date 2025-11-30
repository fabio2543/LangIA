package com.langia.backend.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Notification settings for users.
 */
@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "active_channels", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private Map<String, Boolean> activeChannels = getDefaultChannels();

    @Column(name = "category_preferences", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private Map<String, Object> categoryPreferences = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_frequency")
    @Builder.Default
    private ReminderFrequency reminderFrequency = ReminderFrequency.DAILY;

    @Column(name = "preferred_time_start")
    private LocalTime preferredTimeStart;

    @Column(name = "preferred_time_end")
    private LocalTime preferredTimeEnd;

    @Column(name = "quiet_mode_start")
    private LocalTime quietModeStart;

    @Column(name = "quiet_mode_end")
    private LocalTime quietModeEnd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private static Map<String, Boolean> getDefaultChannels() {
        Map<String, Boolean> defaults = new HashMap<>();
        defaults.put("PUSH", true);
        defaults.put("EMAIL", true);
        defaults.put("WHATSAPP", false);
        return defaults;
    }
}
