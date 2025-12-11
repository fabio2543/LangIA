package com.langia.backend.repository;

import com.langia.backend.model.LinguisticChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LinguisticChunkRepository extends JpaRepository<LinguisticChunk, UUID> {

    List<LinguisticChunk> findByLanguageCodeAndCefrLevelOrderByOrderIndex(String languageCode, String cefrLevel);

    List<LinguisticChunk> findByLanguageCodeAndCefrLevelAndIsCoreTrue(String languageCode, String cefrLevel);

    List<LinguisticChunk> findByLanguageCodeAndCategory(String languageCode, String category);

    List<LinguisticChunk> findByLanguageCodeAndCefrLevelAndCategory(String languageCode, String cefrLevel, String category);

    @Query("SELECT c FROM LinguisticChunk c WHERE c.languageCode = :languageCode AND c.cefrLevel = :cefrLevel AND c.isCore = true ORDER BY c.orderIndex")
    List<LinguisticChunk> findCoreChunks(@Param("languageCode") String languageCode, @Param("cefrLevel") String cefrLevel);

    @Query("SELECT DISTINCT c.category FROM LinguisticChunk c WHERE c.languageCode = :languageCode AND c.cefrLevel = :cefrLevel")
    List<String> findCategoriesByLanguageAndLevel(@Param("languageCode") String languageCode, @Param("cefrLevel") String cefrLevel);

    long countByLanguageCodeAndCefrLevel(String languageCode, String cefrLevel);

    long countByLanguageCodeAndCefrLevelAndIsCore(String languageCode, String cefrLevel, Boolean isCore);

    List<LinguisticChunk> findByLanguageCode(String languageCode);

    List<LinguisticChunk> findByLanguageCodeAndCefrLevel(String languageCode, String cefrLevel);
}
