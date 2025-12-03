package com.langia.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.trail.CompetencyDTO;
import com.langia.backend.dto.trail.DescriptorDTO;
import com.langia.backend.dto.trail.LevelDTO;
import com.langia.backend.mapper.TrailMapper;
import com.langia.backend.model.Competency;
import com.langia.backend.model.Descriptor;
import com.langia.backend.model.Level;
import com.langia.backend.model.LevelCompetency;
import com.langia.backend.repository.CompetencyRepository;
import com.langia.backend.repository.DescriptorRepository;
import com.langia.backend.repository.LevelCompetencyRepository;
import com.langia.backend.repository.LevelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerenciamento do currículo (níveis CEFR, competências, descritores).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CurriculumService {

    private final LevelRepository levelRepository;
    private final CompetencyRepository competencyRepository;
    private final LevelCompetencyRepository levelCompetencyRepository;
    private final DescriptorRepository descriptorRepository;
    private final TrailMapper trailMapper;

    // Versão atual do currículo - em produção seria buscado do banco
    private static final String CURRENT_CURRICULUM_VERSION = "1.0.0";

    /**
     * Retorna a versão atual do currículo.
     */
    public String getCurrentCurriculumVersion() {
        return CURRENT_CURRICULUM_VERSION;
    }

    // ========== NÍVEIS ==========

    /**
     * Busca todos os níveis CEFR ordenados.
     */
    public List<LevelDTO> getAllLevels() {
        List<Level> levels = levelRepository.findAllByOrderByOrderIndexAsc();
        return trailMapper.toLevelDTOList(levels);
    }

    /**
     * Busca nível pelo código (A1, A2, B1, B2, C1, C2).
     */
    public Optional<LevelDTO> getLevelByCode(String code) {
        return levelRepository.findByCode(code)
                .map(trailMapper::toLevelDTO);
    }

    /**
     * Busca entidade Level pelo código.
     */
    public Optional<Level> getLevelEntityByCode(String code) {
        return levelRepository.findByCode(code);
    }

    /**
     * Busca o próximo nível após o atual.
     */
    public Optional<LevelDTO> getNextLevel(String currentLevelCode) {
        return levelRepository.findByCode(currentLevelCode)
                .flatMap(level -> levelRepository.findNextLevel(level.getOrderIndex()))
                .map(trailMapper::toLevelDTO);
    }

    /**
     * Busca o nível anterior ao atual.
     */
    public Optional<LevelDTO> getPreviousLevel(String currentLevelCode) {
        return levelRepository.findByCode(currentLevelCode)
                .flatMap(level -> levelRepository.findPreviousLevel(level.getOrderIndex()))
                .map(trailMapper::toLevelDTO);
    }

    // ========== COMPETÊNCIAS ==========

    /**
     * Busca todas as competências ordenadas.
     */
    public List<CompetencyDTO> getAllCompetencies() {
        List<Competency> competencies = competencyRepository.findAllByOrderByOrderIndexAsc();
        return trailMapper.toCompetencyDTOList(competencies);
    }

    /**
     * Busca competência pelo código.
     */
    public Optional<CompetencyDTO> getCompetencyByCode(String code) {
        return competencyRepository.findByCode(code)
                .map(trailMapper::toCompetencyDTO);
    }

    /**
     * Busca entidade Competency pelo código.
     */
    public Optional<Competency> getCompetencyEntityByCode(String code) {
        return competencyRepository.findByCode(code);
    }

    // ========== LEVEL-COMPETENCIES ==========

    /**
     * Busca competências de um nível com seus pesos.
     */
    public List<LevelCompetency> getLevelCompetencies(String levelCode) {
        return levelRepository.findByCode(levelCode)
                .map(level -> levelCompetencyRepository.findByLevelIdOrderByWeightDesc(level.getId()))
                .orElse(List.of());
    }

    /**
     * Busca associação específica nível-competência.
     */
    public Optional<LevelCompetency> getLevelCompetency(String levelCode, String competencyCode) {
        return levelCompetencyRepository.findByLevelCodeAndCompetencyCode(levelCode, competencyCode);
    }

    // ========== DESCRITORES ==========

    /**
     * Busca descritores de um nível.
     */
    public List<DescriptorDTO> getDescriptorsByLevel(String levelCode) {
        List<Descriptor> descriptors = descriptorRepository.findByLevelCode(levelCode);
        return trailMapper.toDescriptorDTOList(descriptors);
    }

    /**
     * Busca descritores de um nível e competência.
     */
    public List<DescriptorDTO> getDescriptorsByLevelAndCompetency(String levelCode, String competencyCode) {
        List<Descriptor> descriptors = descriptorRepository.findByLevelCodeAndCompetencyCode(levelCode, competencyCode);
        return trailMapper.toDescriptorDTOList(descriptors);
    }

    /**
     * Busca descritores core de uma associação nível-competência.
     */
    public List<Descriptor> getCoreDescriptors(UUID levelCompetencyId) {
        return descriptorRepository.findByLevelCompetencyIdAndIsCoreTrue(levelCompetencyId);
    }

    /**
     * Busca descritores por idioma (ou genéricos).
     */
    public List<DescriptorDTO> getDescriptorsByLanguage(String languageCode) {
        List<Descriptor> descriptors = descriptorRepository.findByLanguageCodeOrGeneric(languageCode);
        return trailMapper.toDescriptorDTOList(descriptors);
    }

    /**
     * Busca descritor pelo código.
     */
    public Optional<Descriptor> getDescriptorByCode(String code) {
        return descriptorRepository.findByCode(code);
    }

    /**
     * Conta descritores de um nível.
     */
    public long countDescriptorsByLevel(String levelCode) {
        return descriptorRepository.countByLevelCode(levelCode);
    }

    /**
     * Verifica se um código de nível é válido.
     */
    public boolean isValidLevelCode(String levelCode) {
        return levelRepository.existsByCode(levelCode);
    }

    /**
     * Verifica se um código de competência é válido.
     */
    public boolean isValidCompetencyCode(String competencyCode) {
        return competencyRepository.existsByCode(competencyCode);
    }
}
