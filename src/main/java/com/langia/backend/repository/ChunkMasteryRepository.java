package com.langia.backend.repository;

import com.langia.backend.model.ChunkMastery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChunkMasteryRepository extends JpaRepository<ChunkMastery, UUID> {

    List<ChunkMastery> findByUserId(UUID userId);

    Optional<ChunkMastery> findByUserIdAndChunkId(UUID userId, UUID chunkId);

    List<ChunkMastery> findByUserIdAndMasteryLevelGreaterThanEqual(UUID userId, Integer masteryLevel);

    List<ChunkMastery> findByUserIdOrderByLastPracticedAtDesc(UUID userId);

    @Query("SELECT cm FROM ChunkMastery cm WHERE cm.user.id = :userId AND cm.chunk.languageCode = :languageCode ORDER BY cm.masteryLevel DESC")
    List<ChunkMastery> findByUserAndLanguage(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT cm FROM ChunkMastery cm WHERE cm.user.id = :userId AND cm.chunk.languageCode = :languageCode AND cm.chunk.cefrLevel = :cefrLevel ORDER BY cm.masteryLevel DESC")
    List<ChunkMastery> findByUserLanguageAndLevel(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("cefrLevel") String cefrLevel);

    @Query("SELECT COUNT(cm) FROM ChunkMastery cm WHERE cm.user.id = :userId AND cm.chunk.languageCode = :languageCode AND cm.masteryLevel >= :minLevel")
    long countMasteredChunks(@Param("userId") UUID userId, @Param("languageCode") String languageCode, @Param("minLevel") Integer minLevel);

    @Query("SELECT AVG(cm.masteryLevel) FROM ChunkMastery cm WHERE cm.user.id = :userId AND cm.chunk.languageCode = :languageCode")
    Double getAverageMasteryLevel(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    void deleteByUserId(UUID userId);

    @Query("SELECT cm FROM ChunkMastery cm WHERE cm.user.id = :userId AND cm.chunk.languageCode = :languageCode ORDER BY cm.masteryLevel ASC, cm.lastPracticedAt ASC NULLS FIRST")
    List<ChunkMastery> findChunksNeedingPractice(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    List<ChunkMastery> findByUserIdAndChunkLanguageCode(UUID userId, String languageCode);
}
