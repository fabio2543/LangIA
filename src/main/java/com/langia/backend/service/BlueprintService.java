package com.langia.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.model.Blueprint;
import com.langia.backend.model.Level;
import com.langia.backend.repository.BlueprintRepository;
import com.langia.backend.repository.LevelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerenciamento de blueprints (templates de trilha).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlueprintService {

    private final BlueprintRepository blueprintRepository;
    private final LevelRepository levelRepository;

    /**
     * Busca blueprint mais adequado para idioma, nível e preferências do estudante.
     *
     * @param languageCode Código do idioma
     * @param levelCode Código do nível CEFR
     * @param preferencesJson JSON das preferências do estudante
     * @return Blueprint mais adequado ou vazio se não encontrar
     */
    @Transactional(readOnly = true)
    public Optional<Blueprint> findMatchingBlueprint(String languageCode, String levelCode, String preferencesJson) {
        log.info("Buscando blueprint para idioma: {}, nível: {}", languageCode, levelCode);

        // Primeiro, tenta buscar blueprint que corresponda às preferências
        Optional<Level> levelOpt = levelRepository.findByCode(levelCode);
        if (levelOpt.isEmpty()) {
            log.warn("Nível não encontrado: {}", levelCode);
            return Optional.empty();
        }

        UUID levelId = levelOpt.get().getId();
        String safePreferences = preferencesJson != null ? preferencesJson : "{}";

        Optional<Blueprint> matching = blueprintRepository.findMatchingBlueprint(
                languageCode, levelId, safePreferences);

        if (matching.isPresent()) {
            log.info("Blueprint encontrado com preferências correspondentes: {}", matching.get().getId());
            return matching;
        }

        // Se não encontrar com preferências, busca qualquer blueprint aprovado para idioma/nível
        List<Blueprint> approvedBlueprints = blueprintRepository.findApprovedByLanguageAndLevel(
                languageCode, levelCode);

        if (!approvedBlueprints.isEmpty()) {
            Blueprint best = approvedBlueprints.get(0);
            log.info("Usando blueprint aprovado mais popular: {}", best.getId());
            return Optional.of(best);
        }

        log.info("Nenhum blueprint encontrado para idioma: {}, nível: {}", languageCode, levelCode);
        return Optional.empty();
    }

    /**
     * Busca blueprint por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Blueprint> findById(UUID blueprintId) {
        return blueprintRepository.findById(blueprintId);
    }

    /**
     * Lista blueprints aprovados para um idioma e nível.
     */
    @Transactional(readOnly = true)
    public List<Blueprint> findApprovedByLanguageAndLevel(String languageCode, String levelCode) {
        return blueprintRepository.findApprovedByLanguageAndLevel(languageCode, levelCode);
    }

    /**
     * Lista blueprints pendentes de aprovação.
     */
    @Transactional(readOnly = true)
    public List<Blueprint> findPendingApproval() {
        return blueprintRepository.findByIsApprovedFalseOrderByCreatedAtDesc();
    }

    /**
     * Lista blueprints mais usados.
     */
    @Transactional(readOnly = true)
    public List<Blueprint> findMostUsed() {
        return blueprintRepository.findMostUsed();
    }

    /**
     * Incrementa contador de uso de um blueprint.
     */
    @Transactional
    public void incrementUsageCount(UUID blueprintId) {
        log.debug("Incrementando contador de uso do blueprint: {}", blueprintId);
        blueprintRepository.incrementUsageCount(blueprintId);
    }

    /**
     * Salva um novo blueprint.
     */
    @Transactional
    public Blueprint save(Blueprint blueprint) {
        log.info("Salvando blueprint: {}", blueprint.getName());
        return blueprintRepository.save(blueprint);
    }

    /**
     * Aprova um blueprint.
     */
    @Transactional
    public Blueprint approve(UUID blueprintId, UUID approvedBy) {
        log.info("Aprovando blueprint: {} por usuário: {}", blueprintId, approvedBy);

        Blueprint blueprint = blueprintRepository.findById(blueprintId)
                .orElseThrow(() -> new RuntimeException("Blueprint não encontrado: " + blueprintId));

        blueprint.setIsApproved(true);
        blueprint.setApprovedBy(approvedBy);
        blueprint.setApprovedAt(java.time.LocalDateTime.now());

        return blueprintRepository.save(blueprint);
    }
}
