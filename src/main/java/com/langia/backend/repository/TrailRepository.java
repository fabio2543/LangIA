package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Trail;
import com.langia.backend.model.TrailStatus;

/**
 * Repository para entidade Trail (trilha de aprendizado do estudante).
 */
@Repository
public interface TrailRepository extends JpaRepository<Trail, UUID> {

    /**
     * Busca trilha ativa de um estudante para um idioma (excluindo arquivadas).
     */
    @Query("SELECT t FROM Trail t " +
           "WHERE t.student.id = :studentId " +
           "AND t.language.code = :languageCode " +
           "AND t.status != 'ARCHIVED' " +
           "ORDER BY t.createdAt DESC")
    Optional<Trail> findActiveByStudentAndLanguage(
            @Param("studentId") UUID studentId,
            @Param("languageCode") String languageCode);

    /**
     * Busca todas as trilhas não arquivadas de um estudante.
     */
    @Query("SELECT t FROM Trail t " +
           "WHERE t.student.id = :studentId " +
           "AND t.status != 'ARCHIVED' " +
           "ORDER BY t.updatedAt DESC")
    List<Trail> findActiveByStudentId(@Param("studentId") UUID studentId);

    /**
     * Busca trilha pelo hash de conteúdo.
     */
    Optional<Trail> findByContentHash(String contentHash);

    /**
     * Busca trilhas por status.
     */
    List<Trail> findByStatusOrderByCreatedAtDesc(TrailStatus status);

    /**
     * Conta trilhas ativas de um estudante (não arquivadas).
     */
    @Query("SELECT COUNT(t) FROM Trail t WHERE t.student.id = :studentId AND t.status != 'ARCHIVED'")
    long countActiveByStudentId(@Param("studentId") UUID studentId);

    /**
     * Verifica se estudante já tem trilha para o idioma (não arquivada).
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Trail t " +
           "WHERE t.student.id = :studentId " +
           "AND t.language.code = :languageCode " +
           "AND t.status != 'ARCHIVED'")
    boolean existsActiveByStudentAndLanguage(
            @Param("studentId") UUID studentId,
            @Param("languageCode") String languageCode);

    /**
     * Busca trilhas em geração (para processamento de jobs).
     */
    List<Trail> findByStatusInOrderByCreatedAtAsc(List<TrailStatus> statuses);

    /**
     * Arquiva trilha (soft delete).
     */
    @Modifying
    @Query("UPDATE Trail t SET t.status = 'ARCHIVED', t.archivedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void archiveById(@Param("id") UUID id);

    /**
     * Atualiza status da trilha.
     */
    @Modifying
    @Query("UPDATE Trail t SET t.status = :status, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") TrailStatus status);

    /**
     * Busca trilha anterior (para histórico de refresh).
     */
    Optional<Trail> findByPreviousTrailId(UUID previousTrailId);

    /**
     * Busca trilhas com blueprint específico.
     */
    List<Trail> findByBlueprintIdOrderByCreatedAtDesc(UUID blueprintId);

    /**
     * Busca trilhas de um estudante para um idioma específico (incluindo arquivadas).
     */
    @Query("SELECT t FROM Trail t " +
           "WHERE t.student.id = :studentId " +
           "AND t.language.code = :languageCode " +
           "ORDER BY t.createdAt DESC")
    List<Trail> findAllByStudentAndLanguage(
            @Param("studentId") UUID studentId,
            @Param("languageCode") String languageCode);

    /**
     * Busca trilhas prontas de um estudante.
     */
    @Query("SELECT t FROM Trail t " +
           "WHERE t.student.id = :studentId " +
           "AND t.status = 'READY' " +
           "ORDER BY t.updatedAt DESC")
    List<Trail> findReadyByStudentId(@Param("studentId") UUID studentId);

    /**
     * Busca trilha por ID com módulos e lições (evita N+1 queries).
     */
    @Query("SELECT DISTINCT t FROM Trail t " +
           "LEFT JOIN FETCH t.modules m " +
           "LEFT JOIN FETCH m.lessons " +
           "LEFT JOIN FETCH m.competency " +
           "LEFT JOIN FETCH t.progress " +
           "LEFT JOIN FETCH t.language " +
           "LEFT JOIN FETCH t.level " +
           "WHERE t.id = :id")
    Optional<Trail> findByIdWithModulesAndLessons(@Param("id") UUID id);
}
