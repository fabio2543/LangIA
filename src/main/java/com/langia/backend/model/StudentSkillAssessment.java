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

    @Column(name = "idioma", nullable = false, length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "dificuldade_escuta", nullable = false)
    @Builder.Default
    private DifficultyLevel listeningDifficulty = DifficultyLevel.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "dificuldade_fala", nullable = false)
    @Builder.Default
    private DifficultyLevel speakingDifficulty = DifficultyLevel.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "dificuldade_leitura", nullable = false)
    @Builder.Default
    private DifficultyLevel readingDifficulty = DifficultyLevel.MODERATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "dificuldade_escrita", nullable = false)
    @Builder.Default
    private DifficultyLevel writingDifficulty = DifficultyLevel.MODERATE;

    @Column(name = "detalhes_escuta", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> listeningDetails = new ArrayList<>();

    @Column(name = "detalhes_fala", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> speakingDetails = new ArrayList<>();

    @Column(name = "detalhes_leitura", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> readingDetails = new ArrayList<>();

    @Column(name = "detalhes_escrita", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> writingDetails = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_cefr_auto")
    private CefrLevel selfCefrLevel;

    @CreationTimestamp
    @Column(name = "assessed_at", nullable = false, updatable = false)
    private LocalDateTime assessedAt;
}
