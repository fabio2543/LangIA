package com.langia.backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_streaks", indexes = {
    @Index(name = "idx_streaks_user", columnList = "user_id"),
    @Index(name = "idx_streaks_current", columnList = "current_streak DESC")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_streaks", columnNames = {"user_id", "language_code"})
})
public class DailyStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    private Integer longestStreak = 0;

    @Column(name = "last_study_date")
    private LocalDate lastStudyDate;

    @Column(name = "streak_started_at")
    private LocalDate streakStartedAt;

    @Column(name = "streak_frozen_until")
    private LocalDate streakFrozenUntil;

    @Column(name = "freeze_count_used")
    private Integer freezeCountUsed = 0;

    @Column(name = "total_study_days")
    private Integer totalStudyDays = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastStudyDate() {
        return lastStudyDate;
    }

    public void setLastStudyDate(LocalDate lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }

    public LocalDate getStreakStartedAt() {
        return streakStartedAt;
    }

    public void setStreakStartedAt(LocalDate streakStartedAt) {
        this.streakStartedAt = streakStartedAt;
    }

    public LocalDate getStreakFrozenUntil() {
        return streakFrozenUntil;
    }

    public void setStreakFrozenUntil(LocalDate streakFrozenUntil) {
        this.streakFrozenUntil = streakFrozenUntil;
    }

    public Integer getFreezeCountUsed() {
        return freezeCountUsed;
    }

    public void setFreezeCountUsed(Integer freezeCountUsed) {
        this.freezeCountUsed = freezeCountUsed;
    }

    public Integer getTotalStudyDays() {
        return totalStudyDays;
    }

    public void setTotalStudyDays(Integer totalStudyDays) {
        this.totalStudyDays = totalStudyDays;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public void recordStudyDay(LocalDate studyDate) {
        if (studyDate.equals(lastStudyDate)) {
            return; // Already studied today
        }

        totalStudyDays++;

        if (lastStudyDate == null || studyDate.equals(lastStudyDate.plusDays(1))) {
            // Consecutive day
            currentStreak++;
            if (lastStudyDate == null) {
                streakStartedAt = studyDate;
            }
        } else if (streakFrozenUntil != null && !studyDate.isAfter(streakFrozenUntil)) {
            // Protected by freeze
            currentStreak++;
            streakFrozenUntil = null; // Use the freeze
        } else {
            // Streak broken
            currentStreak = 1;
            streakStartedAt = studyDate;
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }

        lastStudyDate = studyDate;
    }

    public void freezeStreak() {
        if (freezeCountUsed < 2) { // Max 2 freezes per month
            streakFrozenUntil = LocalDate.now().plusDays(1);
            freezeCountUsed++;
        }
    }

    public boolean isStreakAtRisk() {
        if (lastStudyDate == null) return false;
        return LocalDate.now().isAfter(lastStudyDate) && streakFrozenUntil == null;
    }
}
