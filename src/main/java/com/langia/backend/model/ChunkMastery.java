package com.langia.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chunk_mastery", indexes = {
    @Index(name = "idx_chunk_mastery_user", columnList = "user_id"),
    @Index(name = "idx_chunk_mastery_chunk", columnList = "chunk_id"),
    @Index(name = "idx_chunk_mastery_level", columnList = "user_id, mastery_level")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_chunk_mastery", columnNames = {"user_id", "chunk_id"})
})
public class ChunkMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", nullable = false)
    private LinguisticChunk chunk;

    @Column(name = "mastery_level")
    private Integer masteryLevel = 0;

    @Column(name = "times_practiced")
    private Integer timesPracticed = 0;

    @Column(name = "times_correct")
    private Integer timesCorrect = 0;

    @Column(name = "last_practiced_at")
    private LocalDateTime lastPracticedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contexts_used", columnDefinition = "jsonb")
    private List<String> contextsUsed;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public LinguisticChunk getChunk() {
        return chunk;
    }

    public void setChunk(LinguisticChunk chunk) {
        this.chunk = chunk;
    }

    public Integer getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(Integer masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public Integer getTimesPracticed() {
        return timesPracticed;
    }

    public void setTimesPracticed(Integer timesPracticed) {
        this.timesPracticed = timesPracticed;
    }

    public Integer getTimesCorrect() {
        return timesCorrect;
    }

    public void setTimesCorrect(Integer timesCorrect) {
        this.timesCorrect = timesCorrect;
    }

    public LocalDateTime getLastPracticedAt() {
        return lastPracticedAt;
    }

    public void setLastPracticedAt(LocalDateTime lastPracticedAt) {
        this.lastPracticedAt = lastPracticedAt;
    }

    public List<String> getContextsUsed() {
        return contextsUsed;
    }

    public void setContextsUsed(List<String> contextsUsed) {
        this.contextsUsed = contextsUsed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
    public void incrementPractice(boolean wasCorrect) {
        this.timesPracticed++;
        if (wasCorrect) {
            this.timesCorrect++;
        }
        this.lastPracticedAt = LocalDateTime.now();
    }

    public double getAccuracyRate() {
        if (timesPracticed == 0) return 0.0;
        return (double) timesCorrect / timesPracticed * 100;
    }
}
