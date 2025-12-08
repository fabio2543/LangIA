package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.StudentLanguageEnrollment;

/**
 * Repository para entidade StudentLanguageEnrollment.
 */
@Repository
public interface StudentLanguageEnrollmentRepository extends JpaRepository<StudentLanguageEnrollment, UUID> {

    /**
     * Busca todos os enrollments de um usuário.
     */
    List<StudentLanguageEnrollment> findByUserId(UUID userId);

    /**
     * Busca enrollments de um usuário ordenados por idioma primário primeiro.
     */
    @Query("SELECT e FROM StudentLanguageEnrollment e WHERE e.user.id = :userId ORDER BY e.isPrimary DESC, e.enrolledAt ASC")
    List<StudentLanguageEnrollment> findByUserIdOrderByPrimaryFirst(@Param("userId") UUID userId);

    /**
     * Busca enrollment específico de usuário e idioma.
     */
    @Query("SELECT e FROM StudentLanguageEnrollment e WHERE e.user.id = :userId AND e.language.code = :languageCode")
    Optional<StudentLanguageEnrollment> findByUserIdAndLanguageCode(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    /**
     * Busca o idioma primário de um usuário.
     */
    Optional<StudentLanguageEnrollment> findByUserIdAndIsPrimaryTrue(UUID userId);

    /**
     * Conta quantos idiomas um usuário tem cadastrados.
     */
    long countByUserId(UUID userId);

    /**
     * Verifica se um usuário já tem um idioma específico cadastrado.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM StudentLanguageEnrollment e WHERE e.user.id = :userId AND e.language.code = :languageCode")
    boolean existsByUserIdAndLanguageCode(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    /**
     * Remove enrollment de um usuário e idioma específico.
     */
    @Modifying
    @Query("DELETE FROM StudentLanguageEnrollment e WHERE e.user.id = :userId AND e.language.code = :languageCode")
    void deleteByUserIdAndLanguageCode(@Param("userId") UUID userId, @Param("languageCode") String languageCode);
}
