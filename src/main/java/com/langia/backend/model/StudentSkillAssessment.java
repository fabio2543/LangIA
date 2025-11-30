package com.langia.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Skill assessment history for students by language.
 */
@Entity
@Table(name = "student_skill_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSkillAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "language", nullable = false, length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "listening_difficulty", nullable = false)
    @Builder.Default
    private DifficultyLevel listeningDifficulty = DifficultyLevel.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "speaking_difficulty", nullable = false)
    @Builder.Default
    private DifficultyLevel speakingDifficulty = DifficultyLevel.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_difficulty", nullable = false)
    @Builder.Default
    private DifficultyLevel readingDifficulty = DifficultyLevel.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "writing_difficulty", nullable = false)
    @Builder.Default
    private DifficultyLevel writingDifficulty = DifficultyLevel.MODERATE;

    @Column(name = "listening_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> listeningDetails = new ArrayList<>();

    @Column(name = "speaking_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> speakingDetails = new ArrayList<>();

    @Column(name = "reading_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> readingDetails = new ArrayList<>();

    @Column(name = "writing_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> writingDetails = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "self_cefr_level")
    private CefrLevel selfCefrLevel;

    @CreationTimestamp
    @Column(name = "assessed_at", nullable = false, updatable = false)
    private LocalDateTime assessedAt;
}
