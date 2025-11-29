package com.langia.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // Languages (stored as JSONB for simplicity)
    @Column(name = "idiomas_estudo", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> studyLanguages = new ArrayList<>();

    @Column(name = "idioma_principal", length = 50)
    private String primaryLanguage;

    @Column(name = "nivel_auto_por_idioma", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private Map<String, String> selfLevelByLanguage = new HashMap<>();

    // Availability
    @Enumerated(EnumType.STRING)
    @Column(name = "tempo_diario_disponivel")
    @Builder.Default
    private TimeAvailable dailyTimeAvailable = TimeAvailable.MIN_30;

    @Column(name = "dias_semana_preferidos", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> preferredDays = new ArrayList<>();

    @Column(name = "horarios_preferidos", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> preferredTimes = new ArrayList<>();

    @Column(name = "meta_horas_semana")
    private Integer weeklyHoursGoal;

    // Interests and formats
    @Column(name = "topicos_interesse", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> topicsOfInterest = new ArrayList<>();

    @Column(name = "topicos_customizados", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> customTopics = new ArrayList<>();

    @Column(name = "formatos_preferidos", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> preferredFormats = new ArrayList<>();

    @Column(name = "ranking_formatos", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> formatRanking = new ArrayList<>();

    // Objectives
    @Enumerated(EnumType.STRING)
    @Column(name = "objetivo_principal")
    private LearningObjective primaryObjective;

    @Column(name = "objetivo_descricao", length = 500)
    private String objectiveDescription;

    @Column(name = "prazo_objetivo", length = 30)
    private String objectiveDeadline;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
