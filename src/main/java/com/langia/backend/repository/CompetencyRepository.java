package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Competency;

/**
 * Repository para entidade Competency (competências linguísticas).
 */
@Repository
public interface CompetencyRepository extends JpaRepository<Competency, UUID> {

    /**
     * Busca competência pelo código (ex: speaking, listening).
     */
    Optional<Competency> findByCode(String code);

    /**
     * Verifica se existe competência com o código especificado.
     */
    boolean existsByCode(String code);

    /**
     * Busca todas as competências ordenadas por ordem.
     */
    List<Competency> findAllByOrderByOrderIndexAsc();

    /**
     * Busca competências por categoria.
     */
    List<Competency> findByCategoryOrderByOrderIndexAsc(String category);
}
