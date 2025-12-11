package com.langia.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "error_patterns", indexes = {
    @Index(name = "idx_errors_user_lang", columnList = "user_id, language_code"),
    @Index(name = "idx_errors_count", columnList = "occurrence_count DESC")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_error_pattern", columnNames = {"user_id", "language_code", "error_category"})
})
public class ErrorPattern {

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

    @Column(name = "error_category", nullable = false, length = 100)
    private String errorCategory;

    @Column(name = "error_description", nullable = false, columnDefinition = "TEXT")
    private String errorDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "example_errors", columnDefinition = "jsonb")
    private List<Map<String, Object>> exampleErrors;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount = 1;

    @Column(name = "first_occurred_at")
    private LocalDateTime firstOccurredAt;

    @Column(name = "last_occurred_at")
    private LocalDateTime lastOccurredAt;

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column
    private Integer priority = 5;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        firstOccurredAt = LocalDateTime.now();
        lastOccurredAt = LocalDateTime.now();
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

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public String getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(String errorCategory) {
        this.errorCategory = errorCategory;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public List<Map<String, Object>> getExampleErrors() {
        return exampleErrors;
    }

    public void setExampleErrors(List<Map<String, Object>> exampleErrors) {
        this.exampleErrors = exampleErrors;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public LocalDateTime getFirstOccurredAt() {
        return firstOccurredAt;
    }

    public void setFirstOccurredAt(LocalDateTime firstOccurredAt) {
        this.firstOccurredAt = firstOccurredAt;
    }

    public LocalDateTime getLastOccurredAt() {
        return lastOccurredAt;
    }

    public void setLastOccurredAt(LocalDateTime lastOccurredAt) {
        this.lastOccurredAt = lastOccurredAt;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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
    public void incrementOccurrence() {
        this.occurrenceCount++;
        this.lastOccurredAt = LocalDateTime.now();
        this.isResolved = false;
        this.resolvedAt = null;
    }

    public void markAsResolved(String notes) {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = notes;
    }
}
