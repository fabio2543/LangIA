package com.langia.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Language;

/**
 * Repository para entidade Language.
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, String> {

    /**
     * Busca todos os idiomas ativos.
     */
    List<Language> findByActiveTrue();

    /**
     * Busca todos os idiomas ordenados por nome em português.
     */
    List<Language> findAllByOrderByNamePtAsc();

    /**
     * Busca idiomas ativos ordenados por nome em português.
     */
    List<Language> findByActiveTrueOrderByNamePtAsc();
}
