package com.langia.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "skill_metrics", indexes = {
    @Index(name = "idx_metrics_user_date", columnList = "user_id, metric_date DESC"),
    @Index(name = "idx_metrics_user_skill", columnList = "user_id, skill_type")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_skill_metrics", columnNames = {"user_id", "language_code", "skill_type", "metric_date"})
})
public class SkillMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "skill_type", nullable = false, length = 20)
    private String skillType;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "exercises_completed")
    private Integer exercisesCompleted = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "accuracy_percentage", precision = 5, scale = 2)
    private BigDecimal accuracyPercentage;

    @Column(name = "avg_response_time_ms")
    private Integer avgResponseTimeMs;

    @Column(name = "min_response_time_ms")
    private Integer minResponseTimeMs;

    @Column(name = "max_response_time_ms")
    private Integer maxResponseTimeMs;

    @Column(name = "total_practice_time_minutes")
    private Integer totalPracticeTimeMinutes = 0;

    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    @Column(name = "difficulty_avg", precision = 3, scale = 2)
    private BigDecimal difficultyAvg;

    @Column(name = "improvement_score", precision = 5, scale = 2)
    private BigDecimal improvementScore;

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
        updateAccuracy();
    }

    private void updateAccuracy() {
        if (exercisesCompleted != null && exercisesCompleted > 0) {
            double accuracy = (double) correctAnswers / exercisesCompleted * 100;
            this.accuracyPercentage = BigDecimal.valueOf(accuracy);
        }
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

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public LocalDate getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(LocalDate metricDate) {
        this.metricDate = metricDate;
    }

    public Integer getExercisesCompleted() {
        return exercisesCompleted;
    }

    public void setExercisesCompleted(Integer exercisesCompleted) {
        this.exercisesCompleted = exercisesCompleted;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public BigDecimal getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(BigDecimal accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public Integer getAvgResponseTimeMs() {
        return avgResponseTimeMs;
    }

    public void setAvgResponseTimeMs(Integer avgResponseTimeMs) {
        this.avgResponseTimeMs = avgResponseTimeMs;
    }

    public Integer getMinResponseTimeMs() {
        return minResponseTimeMs;
    }

    public void setMinResponseTimeMs(Integer minResponseTimeMs) {
        this.minResponseTimeMs = minResponseTimeMs;
    }

    public Integer getMaxResponseTimeMs() {
        return maxResponseTimeMs;
    }

    public void setMaxResponseTimeMs(Integer maxResponseTimeMs) {
        this.maxResponseTimeMs = maxResponseTimeMs;
    }

    public Integer getTotalPracticeTimeMinutes() {
        return totalPracticeTimeMinutes;
    }

    public void setTotalPracticeTimeMinutes(Integer totalPracticeTimeMinutes) {
        this.totalPracticeTimeMinutes = totalPracticeTimeMinutes;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public BigDecimal getDifficultyAvg() {
        return difficultyAvg;
    }

    public void setDifficultyAvg(BigDecimal difficultyAvg) {
        this.difficultyAvg = difficultyAvg;
    }

    public BigDecimal getImprovementScore() {
        return improvementScore;
    }

    public void setImprovementScore(BigDecimal improvementScore) {
        this.improvementScore = improvementScore;
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
    public void addExercise(boolean correct, Integer responseTimeMs) {
        this.exercisesCompleted++;
        if (correct) {
            this.correctAnswers++;
        }

        if (responseTimeMs != null) {
            if (this.avgResponseTimeMs == null) {
                this.avgResponseTimeMs = responseTimeMs;
                this.minResponseTimeMs = responseTimeMs;
                this.maxResponseTimeMs = responseTimeMs;
            } else {
                // Update average
                this.avgResponseTimeMs = (this.avgResponseTimeMs * (exercisesCompleted - 1) + responseTimeMs) / exercisesCompleted;
                this.minResponseTimeMs = Math.min(this.minResponseTimeMs, responseTimeMs);
                this.maxResponseTimeMs = Math.max(this.maxResponseTimeMs, responseTimeMs);
            }
        }

        updateAccuracy();
    }
}
