package com.langia.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa uma lição dentro de um módulo.
 */
@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private TrailModule module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_block_id")
    private ContentBlock contentBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descriptor_id")
    private Descriptor descriptor;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LessonType type;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 15;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private String content = "{}";

    @Column(name = "is_placeholder", nullable = false)
    @Builder.Default
    private Boolean isPlaceholder = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Verifica se a lição foi completada.
     */
    public boolean isCompleted() {
        return completedAt != null;
    }

    /**
     * Marca a lição como completada.
     */
    public void complete(BigDecimal score, Integer timeSpentSeconds) {
        this.completedAt = LocalDateTime.now();
        this.score = score;
        if (timeSpentSeconds != null) {
            this.timeSpentSeconds = timeSpentSeconds;
        }
    }
}
