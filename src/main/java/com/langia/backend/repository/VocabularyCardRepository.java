package com.langia.backend.repository;

import com.langia.backend.model.VocabularyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VocabularyCardRepository extends JpaRepository<VocabularyCard, UUID> {

    List<VocabularyCard> findByUserIdAndLanguageCode(UUID userId, String languageCode);

    List<VocabularyCard> findByUserIdAndLanguageCodeAndCefrLevel(UUID userId, String languageCode, String cefrLevel);

    List<VocabularyCard> findByUserIdAndCardType(UUID userId, String cardType);

    List<VocabularyCard> findByIsSystemCardTrueAndLanguageCodeAndCefrLevel(String languageCode, String cefrLevel);

    List<VocabularyCard> findByIsSystemCardTrueAndLanguageCode(String languageCode);

    @Query("SELECT vc FROM VocabularyCard vc WHERE vc.isSystemCard = true AND vc.languageCode = :languageCode AND vc.cefrLevel = :cefrLevel AND vc.isActive = true")
    List<VocabularyCard> findActiveSystemCards(@Param("languageCode") String languageCode, @Param("cefrLevel") String cefrLevel);

    @Query("SELECT vc FROM VocabularyCard vc WHERE (vc.user.id = :userId OR vc.isSystemCard = true) AND vc.languageCode = :languageCode AND vc.isActive = true")
    List<VocabularyCard> findAllAvailableCards(@Param("userId") UUID userId, @Param("languageCode") String languageCode);

    @Query("SELECT vc FROM VocabularyCard vc WHERE vc.user.id = :userId AND vc.sourceChunk.id IS NOT NULL")
    List<VocabularyCard> findUserCardsFromChunks(@Param("userId") UUID userId);

    long countByUserIdAndLanguageCode(UUID userId, String languageCode);

    long countByIsSystemCardTrueAndLanguageCodeAndCefrLevel(String languageCode, String cefrLevel);

    @Query("SELECT COUNT(vc) FROM VocabularyCard vc WHERE (vc.user.id = :userId OR vc.isSystemCard = true) AND vc.languageCode = :languageCode AND vc.isActive = true")
    int countByUserIdOrSystemCard(@Param("userId") UUID userId, @Param("languageCode") String languageCode);
}
