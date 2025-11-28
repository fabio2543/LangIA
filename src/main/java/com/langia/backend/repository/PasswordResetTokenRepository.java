package com.langia.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.PasswordResetToken;

/**
 * Repository para operações com tokens de recuperação de senha.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Busca token pelo hash SHA-256.
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Busca tokens ativos (não usados e não expirados) de um usuário.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId AND t.usedAt IS NULL AND t.expiresAt > :now")
    List<PasswordResetToken> findActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Invalida todos os tokens ativos de um usuário.
     * Retorna a quantidade de tokens invalidados.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.user.id = :userId AND t.usedAt IS NULL")
    int invalidateAllUserTokens(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Remove tokens expirados e já utilizados (limpeza).
     * Retorna a quantidade de tokens removidos.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :before AND t.usedAt IS NOT NULL")
    int deleteExpiredAndUsed(@Param("before") LocalDateTime before);

    /**
     * Conta quantos tokens ativos o usuário possui.
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user.id = :userId AND t.usedAt IS NULL AND t.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
