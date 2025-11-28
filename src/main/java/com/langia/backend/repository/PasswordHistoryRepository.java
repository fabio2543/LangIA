package com.langia.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.PasswordHistory;

/**
 * Repository para operações com histórico de senhas.
 */
@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    /**
     * Busca histórico de senhas do usuário ordenado por data (mais recente primeiro).
     */
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user.id = :userId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Busca as últimas N senhas do usuário.
     */
    default List<PasswordHistory> findLastPasswords(UUID userId, int count) {
        return findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, count));
    }

    /**
     * Conta quantas senhas estão no histórico do usuário.
     */
    @Query("SELECT COUNT(ph) FROM PasswordHistory ph WHERE ph.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * Remove as senhas mais antigas, mantendo apenas as N mais recentes.
     * Nota: Esta query usa subquery para compatibilidade com PostgreSQL.
     */
    @Modifying
    @Query(value = "DELETE FROM password_history ph WHERE ph.user_id = :userId AND ph.id NOT IN " +
           "(SELECT id FROM (SELECT id FROM password_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :keep) AS subquery)",
           nativeQuery = true)
    int deleteOldestKeeping(@Param("userId") UUID userId, @Param("keep") int keep);

    /**
     * Busca todo o histórico de senhas de um usuário.
     */
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
