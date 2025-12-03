package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.LevelCompetency;

/**
 * Repository para entidade LevelCompetency (associação nível-competência).
 */
@Repository
public interface LevelCompetencyRepository extends JpaRepository<LevelCompetency, UUID> {

    /**
     * Busca associação por nível e competência.
     */
    Optional<LevelCompetency> findByLevelIdAndCompetencyId(UUID levelId, UUID competencyId);

    /**
     * Busca todas as associações de um nível.
     */
    List<LevelCompetency> findByLevelId(UUID levelId);

    /**
     * Busca todas as associações de uma competência.
     */
    List<LevelCompetency> findByCompetencyId(UUID competencyId);

    /**
     * Busca associações de um nível com competências ordenadas por peso.
     */
    @Query("SELECT lc FROM LevelCompetency lc WHERE lc.level.id = :levelId ORDER BY lc.weight DESC")
    List<LevelCompetency> findByLevelIdOrderByWeightDesc(@Param("levelId") UUID levelId);

    /**
     * Busca associação por código do nível e código da competência.
     */
    @Query("SELECT lc FROM LevelCompetency lc WHERE lc.level.code = :levelCode AND lc.competency.code = :competencyCode")
    Optional<LevelCompetency> findByLevelCodeAndCompetencyCode(
            @Param("levelCode") String levelCode,
            @Param("competencyCode") String competencyCode);
}
