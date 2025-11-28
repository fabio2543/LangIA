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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa um token de recuperação de senha.
 * O token é armazenado como hash SHA-256 para segurança.
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_prt_token_hash", columnList = "token_hash"),
    @Index(name = "idx_prt_user_used", columnList = "user_id, used_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Hash SHA-256 do token (64 caracteres hexadecimais).
     * O token em texto plano é enviado por email e nunca armazenado.
     */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    /**
     * Data/hora de expiração do token.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Data/hora em que o token foi utilizado.
     * Null se ainda não foi usado.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Verifica se o token está expirado.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Verifica se o token já foi utilizado.
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Verifica se o token é válido (não expirado e não usado).
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * Marca o token como utilizado.
     */
    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }
}
