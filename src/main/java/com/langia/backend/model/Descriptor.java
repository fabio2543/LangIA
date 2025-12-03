package com.langia.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidade que representa um descritor de aprendizagem (Can-Do Statement).
 * Baseado no framework CEFR.
 */
@Entity
@Table(name = "descriptors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Descriptor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_competency_id", nullable = false)
    private LevelCompetency levelCompetency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_code", referencedColumnName = "code")
    private Language language;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_core", nullable = false)
    @Builder.Default
    private Boolean isCore = true;

    @Column(name = "estimated_hours", precision = 4, scale = 1)
    private BigDecimal estimatedHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
