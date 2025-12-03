package com.langia.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa uma trilha de aprendizado personalizada.
 * Cada estudante pode ter até 3 trilhas ativas (uma por idioma).
 */
@Entity
@Table(name = "trails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_code", referencedColumnName = "code", nullable = false)
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blueprint_id")
    private Blueprint blueprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TrailStatus status = TrailStatus.GENERATING;

    @Column(name = "content_hash", nullable = false, length = 40)
    private String contentHash;

    @Column(name = "curriculum_version", nullable = false, length = 20)
    private String curriculumVersion;

    @Column(name = "estimated_duration_hours", precision = 5, scale = 1)
    private BigDecimal estimatedDurationHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_trail_id")
    private Trail previousTrail;

    @Enumerated(EnumType.STRING)
    @Column(name = "refresh_reason")
    private RefreshReason refreshReason;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @OneToMany(mappedBy = "trail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrailModule> modules = new ArrayList<>();

    @OneToOne(mappedBy = "trail", cascade = CascadeType.ALL, orphanRemoval = true)
    private TrailProgress progress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Adiciona um módulo à trilha.
     */
    public void addModule(TrailModule module) {
        modules.add(module);
        module.setTrail(this);
    }

    /**
     * Verifica se a trilha está ativa (não arquivada).
     */
    public boolean isActive() {
        return status != TrailStatus.ARCHIVED;
    }

    /**
     * Verifica se a trilha está pronta para uso.
     */
    public boolean isReady() {
        return status == TrailStatus.READY;
    }
}
