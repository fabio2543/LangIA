package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
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
    Optional<StudentLanguageEnrollment> findByUserIdAndLanguageCode(UUID userId, String languageCode);

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
    boolean existsByUserIdAndLanguageCode(UUID userId, String languageCode);

    /**
     * Remove enrollment de um usuário e idioma específico.
     */
    void deleteByUserIdAndLanguageCode(UUID userId, String languageCode);
}
