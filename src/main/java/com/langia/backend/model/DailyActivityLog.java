package com.langia.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "daily_activity_log", indexes = {
    @Index(name = "idx_activity_user_date", columnList = "user_id, activity_date DESC"),
    @Index(name = "idx_activity_date", columnList = "activity_date DESC")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_activity", columnNames = {"user_id", "language_code", "activity_date"})
})
public class DailyActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "lessons_started")
    private Integer lessonsStarted = 0;

    @Column(name = "lessons_completed")
    private Integer lessonsCompleted = 0;

    @Column(name = "exercises_completed")
    private Integer exercisesCompleted = 0;

    @Column(name = "cards_reviewed")
    private Integer cardsReviewed = 0;

    @Column(name = "chunks_practiced")
    private Integer chunksPracticed = 0;

    @Column(name = "minutes_studied")
    private Integer minutesStudied = 0;

    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills_practiced", columnDefinition = "jsonb")
    private List<String> skillsPracticed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "achievements_unlocked", columnDefinition = "jsonb")
    private List<String> achievementsUnlocked;

    @Column(name = "session_count")
    private Integer sessionCount = 0;

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

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public Integer getLessonsStarted() {
        return lessonsStarted;
    }

    public void setLessonsStarted(Integer lessonsStarted) {
        this.lessonsStarted = lessonsStarted;
    }

    public Integer getLessonsCompleted() {
        return lessonsCompleted;
    }

    public void setLessonsCompleted(Integer lessonsCompleted) {
        this.lessonsCompleted = lessonsCompleted;
    }

    public Integer getExercisesCompleted() {
        return exercisesCompleted;
    }

    public void setExercisesCompleted(Integer exercisesCompleted) {
        this.exercisesCompleted = exercisesCompleted;
    }

    public Integer getCardsReviewed() {
        return cardsReviewed;
    }

    public void setCardsReviewed(Integer cardsReviewed) {
        this.cardsReviewed = cardsReviewed;
    }

    public Integer getChunksPracticed() {
        return chunksPracticed;
    }

    public void setChunksPracticed(Integer chunksPracticed) {
        this.chunksPracticed = chunksPracticed;
    }

    public Integer getMinutesStudied() {
        return minutesStudied;
    }

    public void setMinutesStudied(Integer minutesStudied) {
        this.minutesStudied = minutesStudied;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public List<String> getSkillsPracticed() {
        return skillsPracticed;
    }

    public void setSkillsPracticed(List<String> skillsPracticed) {
        this.skillsPracticed = skillsPracticed;
    }

    public List<String> getAchievementsUnlocked() {
        return achievementsUnlocked;
    }

    public void setAchievementsUnlocked(List<String> achievementsUnlocked) {
        this.achievementsUnlocked = achievementsUnlocked;
    }

    public Integer getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(Integer sessionCount) {
        this.sessionCount = sessionCount;
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
    public void addLesson(boolean completed) {
        this.lessonsStarted++;
        if (completed) {
            this.lessonsCompleted++;
        }
    }

    public void addExercise() {
        this.exercisesCompleted++;
    }

    public void addCardReview() {
        this.cardsReviewed++;
    }

    public void addChunkPractice() {
        this.chunksPracticed++;
    }

    public void addStudyTime(int minutes) {
        this.minutesStudied += minutes;
    }

    public void addXp(int xp) {
        this.xpEarned += xp;
    }

    public void startNewSession() {
        this.sessionCount++;
    }
}
