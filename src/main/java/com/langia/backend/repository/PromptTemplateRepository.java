package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.PromptTemplate;

/**
 * Repositório para templates de prompts usados na geração de conteúdo via LLM.
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {

    /**
     * Busca template ativo por código da competência e nome.
     */
    @Query("SELECT pt FROM PromptTemplate pt " +
           "WHERE pt.competency.code = :competencyCode " +
           "AND pt.name = :name " +
           "AND pt.isActive = true " +
           "ORDER BY pt.version DESC")
    Optional<PromptTemplate> findActiveByCompetencyCodeAndName(
            @Param("competencyCode") String competencyCode,
            @Param("name") String name);

    /**
     * Busca todos os templates ativos para uma competência.
     */
    @Query("SELECT pt FROM PromptTemplate pt " +
           "WHERE pt.competency.code = :competencyCode " +
           "AND pt.isActive = true " +
           "ORDER BY pt.name")
    List<PromptTemplate> findActiveByCompetencyCode(@Param("competencyCode") String competencyCode);

    /**
     * Busca template ativo mais recente para uma competência (qualquer nome).
     */
    @Query("SELECT pt FROM PromptTemplate pt " +
           "WHERE pt.competency.code = :competencyCode " +
           "AND pt.isActive = true " +
           "ORDER BY pt.version DESC, pt.createdAt DESC")
    List<PromptTemplate> findLatestActiveByCompetencyCode(@Param("competencyCode") String competencyCode);

    /**
     * Busca todos os templates ativos.
     */
    List<PromptTemplate> findByIsActiveTrue();
}
