package com.langia.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile_functionalities", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_functionality", columnNames = { "profile_id", "functionality_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ProfileFunctionality {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "functionality_id", nullable = false)
    private Functionality functionality;

    @CreationTimestamp
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @Builder.Default
    @Column(name = "granted_by_inheritance", nullable = false)
    private boolean grantedByInheritance = false;

    @PrePersist
    void ensureGrantedAt() {
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
    }
}


