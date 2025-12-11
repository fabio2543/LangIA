package com.langia.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "srs_progress", indexes = {
    @Index(name = "idx_srs_user", columnList = "user_id"),
    @Index(name = "idx_srs_next_review", columnList = "user_id, next_review_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_srs_user_card", columnNames = {"user_id", "card_id"})
})
public class SrsProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private VocabularyCard card;

    @Column(name = "easiness_factor", precision = 4, scale = 2)
    private BigDecimal easinessFactor = BigDecimal.valueOf(2.50);

    @Column(name = "interval_days")
    private Integer intervalDays = 1;

    @Column
    private Integer repetitions = 0;

    @Column(name = "next_review_date", nullable = false)
    private LocalDate nextReviewDate;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "last_quality")
    private Integer lastQuality;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "correct_reviews")
    private Integer correctReviews = 0;

    @Column
    private Integer streak = 0;

    @Column
    private Integer lapses = 0;

    @Column(length = 20)
    private String status = "new";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nextReviewDate == null) {
            nextReviewDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // SM-2 Algorithm implementation
    public void review(int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }

        this.lastQuality = quality;
        this.lastReviewedAt = LocalDateTime.now();
        this.totalReviews++;

        if (quality >= 3) {
            this.correctReviews++;
            this.streak++;

            // Calculate new easiness factor
            double ef = easinessFactor.doubleValue();
            ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            if (ef < 1.3) ef = 1.3;
            this.easinessFactor = BigDecimal.valueOf(ef);

            // Calculate new interval
            if (repetitions == 0) {
                intervalDays = 1;
            } else if (repetitions == 1) {
                intervalDays = 6;
            } else {
                intervalDays = (int) Math.round(intervalDays * ef);
            }

            repetitions++;
            status = "review";
        } else {
            // Failed review - reset
            this.streak = 0;
            this.lapses++;
            this.intervalDays = 1;
            this.repetitions = 0;
            status = "relearning";
        }

        this.nextReviewDate = LocalDate.now().plusDays(intervalDays);
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

    public VocabularyCard getCard() {
        return card;
    }

    public void setCard(VocabularyCard card) {
        this.card = card;
    }

    public BigDecimal getEasinessFactor() {
        return easinessFactor;
    }

    public void setEasinessFactor(BigDecimal easinessFactor) {
        this.easinessFactor = easinessFactor;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(LocalDateTime lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public Integer getLastQuality() {
        return lastQuality;
    }

    public void setLastQuality(Integer lastQuality) {
        this.lastQuality = lastQuality;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Integer getCorrectReviews() {
        return correctReviews;
    }

    public void setCorrectReviews(Integer correctReviews) {
        this.correctReviews = correctReviews;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public Integer getLapses() {
        return lapses;
    }

    public void setLapses(Integer lapses) {
        this.lapses = lapses;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
    public boolean isDueToday() {
        return !nextReviewDate.isAfter(LocalDate.now());
    }

    public double getRetentionRate() {
        if (totalReviews == 0) return 0.0;
        return (double) correctReviews / totalReviews * 100;
    }
}
