package com.langia.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 * Learning preferences for students.
 */
@Entity
@Table(name = "student_learning_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentLearningPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Availability
    @Column(name = "daily_time_available", length = 20)
    @Builder.Default
    private String dailyTimeAvailable = "MIN_30";

    @Column(name = "preferred_days", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> preferredDays = new ArrayList<>();

    @Column(name = "preferred_times", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> preferredTimes = new ArrayList<>();

    @Column(name = "weekly_hours_goal")
    private Integer weeklyHoursGoal;

    // Interests and formats
    @Column(name = "topics_of_interest", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> topicsOfInterest = new ArrayList<>();

    @Column(name = "custom_topics", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> customTopics = new ArrayList<>();

    @Column(name = "preferred_formats", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> preferredFormats = new ArrayList<>();

    @Column(name = "format_ranking", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> formatRanking = new ArrayList<>();

    // Objectives
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_objective")
    private LearningObjective primaryObjective;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_objective")
    private LearningObjective secondaryObjective;

    @Column(name = "objective_description", length = 500)
    private String objectiveDescription;

    @Column(name = "objective_deadline", length = 30)
    private String objectiveDeadline;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
