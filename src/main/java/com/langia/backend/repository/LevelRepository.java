package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Level;

/**
 * Repository para entidade Level (níveis CEFR).
 */
@Repository
public interface LevelRepository extends JpaRepository<Level, UUID> {

    /**
     * Busca nível pelo código CEFR (ex: A1, B2).
     */
    Optional<Level> findByCode(String code);

    /**
     * Verifica se existe nível com o código especificado.
     */
    boolean existsByCode(String code);

    /**
     * Busca todos os níveis ordenados por ordem.
     */
    List<Level> findAllByOrderByOrderIndexAsc();

    /**
     * Busca nível pelo order_index.
     */
    Optional<Level> findByOrderIndex(Integer orderIndex);

    /**
     * Busca o próximo nível após o atual.
     */
    @Query("SELECT l FROM Level l WHERE l.orderIndex > :currentOrder ORDER BY l.orderIndex ASC LIMIT 1")
    Optional<Level> findNextLevel(@Param("currentOrder") Integer currentOrder);

    /**
     * Busca o nível anterior ao atual.
     */
    @Query("SELECT l FROM Level l WHERE l.orderIndex < :currentOrder ORDER BY l.orderIndex DESC LIMIT 1")
    Optional<Level> findPreviousLevel(@Param("currentOrder") Integer currentOrder);
}
