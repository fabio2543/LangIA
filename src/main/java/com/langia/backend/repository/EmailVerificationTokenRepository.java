package com.langia.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.EmailVerificationToken;

/**
 * Repository para operacoes com tokens de verificacao de e-mail.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    /**
     * Busca token pelo hash SHA-256.
     */
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    /**
     * Invalida todos os tokens ativos de um usuario.
     * Retorna a quantidade de tokens invalidados.
     */
    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.usedAt = :now WHERE t.user.id = :userId AND t.usedAt IS NULL")
    int invalidateAllUserTokens(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Conta quantos tokens ativos o usuario possui.
     */
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user.id = :userId AND t.usedAt IS NULL AND t.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Remove tokens expirados e ja utilizados (limpeza).
     * Retorna a quantidade de tokens removidos.
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :before AND t.usedAt IS NOT NULL")
    int deleteExpiredAndUsed(@Param("before") LocalDateTime before);
}
