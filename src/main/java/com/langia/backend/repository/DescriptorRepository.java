package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Descriptor;

/**
 * Repository para entidade Descriptor (Can-Do Statements CEFR).
 */
@Repository
public interface DescriptorRepository extends JpaRepository<Descriptor, UUID> {

    /**
     * Busca descritor pelo código único.
     */
    Optional<Descriptor> findByCode(String code);

    /**
     * Verifica se existe descritor com o código especificado.
     */
    boolean existsByCode(String code);

    /**
     * Busca descritores de uma associação nível-competência ordenados.
     */
    List<Descriptor> findByLevelCompetencyIdOrderByOrderIndexAsc(UUID levelCompetencyId);

    /**
     * Busca descritores core de uma associação nível-competência.
     */
    List<Descriptor> findByLevelCompetencyIdAndIsCoreTrue(UUID levelCompetencyId);

    /**
     * Busca descritores por nível e competência.
     */
    @Query("SELECT d FROM Descriptor d " +
           "WHERE d.levelCompetency.level.code = :levelCode " +
           "AND d.levelCompetency.competency.code = :competencyCode " +
           "ORDER BY d.orderIndex ASC")
    List<Descriptor> findByLevelCodeAndCompetencyCode(
            @Param("levelCode") String levelCode,
            @Param("competencyCode") String competencyCode);

    /**
     * Busca todos os descritores de um nível.
     */
    @Query("SELECT d FROM Descriptor d " +
           "WHERE d.levelCompetency.level.code = :levelCode " +
           "ORDER BY d.levelCompetency.competency.orderIndex ASC, d.orderIndex ASC")
    List<Descriptor> findByLevelCode(@Param("levelCode") String levelCode);

    /**
     * Busca descritores por idioma específico.
     */
    @Query("SELECT d FROM Descriptor d " +
           "WHERE d.language.code = :languageCode OR d.language IS NULL " +
           "ORDER BY d.orderIndex ASC")
    List<Descriptor> findByLanguageCodeOrGeneric(@Param("languageCode") String languageCode);

    /**
     * Conta descritores de um nível.
     */
    @Query("SELECT COUNT(d) FROM Descriptor d WHERE d.levelCompetency.level.code = :levelCode")
    long countByLevelCode(@Param("levelCode") String levelCode);
}
