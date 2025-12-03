package com.langia.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.langia.backend.dto.trail.CompetencyDTO;
import com.langia.backend.dto.trail.DescriptorDTO;
import com.langia.backend.dto.trail.LessonDTO;
import com.langia.backend.dto.trail.LevelDTO;
import com.langia.backend.dto.trail.ModuleDTO;
import com.langia.backend.dto.trail.TrailDTO;
import com.langia.backend.dto.trail.TrailProgressDTO;
import com.langia.backend.dto.trail.TrailSummaryDTO;
import com.langia.backend.model.Competency;
import com.langia.backend.model.Descriptor;
import com.langia.backend.model.Lesson;
import com.langia.backend.model.Level;
import com.langia.backend.model.Trail;
import com.langia.backend.model.TrailModule;
import com.langia.backend.model.TrailProgress;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper para conversão entre Entities e DTOs de trilhas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrailMapper {

    private final ObjectMapper objectMapper;

    /**
     * Converte Trail entity para TrailDTO completo.
     */
    public TrailDTO toTrailDTO(Trail trail) {
        if (trail == null) return null;

        return TrailDTO.builder()
                .id(trail.getId())
                .studentId(trail.getStudent() != null ? trail.getStudent().getId() : null)
                .languageCode(trail.getLanguage() != null ? trail.getLanguage().getCode() : null)
                .languageName(trail.getLanguage() != null ? trail.getLanguage().getNamePt() : null)
                .languageFlag(null) // TODO: Adicionar flagEmoji à tabela languages se necessário
                .levelCode(trail.getLevel() != null ? trail.getLevel().getCode() : null)
                .levelName(trail.getLevel() != null ? trail.getLevel().getName() : null)
                .status(trail.getStatus())
                .contentHash(trail.getContentHash())
                .curriculumVersion(trail.getCurriculumVersion())
                .estimatedDurationHours(trail.getEstimatedDurationHours())
                .blueprintId(trail.getBlueprint() != null ? trail.getBlueprint().getId() : null)
                .previousTrailId(trail.getPreviousTrail() != null ? trail.getPreviousTrail().getId() : null)
                .refreshReason(trail.getRefreshReason() != null ? trail.getRefreshReason().name() : null)
                .modules(toModuleDTOList(trail.getModules()))
                .progress(toTrailProgressDTO(trail.getProgress()))
                .archivedAt(trail.getArchivedAt())
                .createdAt(trail.getCreatedAt())
                .updatedAt(trail.getUpdatedAt())
                .build();
    }

    /**
     * Converte Trail entity para TrailSummaryDTO resumido.
     */
    public TrailSummaryDTO toTrailSummaryDTO(Trail trail) {
        if (trail == null) return null;

        TrailProgress progress = trail.getProgress();

        return TrailSummaryDTO.builder()
                .id(trail.getId())
                .languageCode(trail.getLanguage() != null ? trail.getLanguage().getCode() : null)
                .languageName(trail.getLanguage() != null ? trail.getLanguage().getNamePt() : null)
                .languageFlag(null) // TODO: Adicionar flagEmoji à tabela languages se necessário
                .levelCode(trail.getLevel() != null ? trail.getLevel().getCode() : null)
                .levelName(trail.getLevel() != null ? trail.getLevel().getName() : null)
                .status(trail.getStatus())
                .progressPercentage(progress != null ? progress.getProgressPercentage() : null)
                .lessonsCompleted(progress != null ? progress.getLessonsCompleted() : 0)
                .totalLessons(progress != null ? progress.getTotalLessons() : 0)
                .averageScore(progress != null ? progress.getAverageScore() : null)
                .timeSpentMinutes(progress != null ? progress.getTimeSpentMinutes() : 0)
                .lastActivityAt(progress != null ? progress.getLastActivityAt() : null)
                .createdAt(trail.getCreatedAt())
                .build();
    }

    /**
     * Converte lista de Trail entities para lista de TrailSummaryDTO.
     */
    public List<TrailSummaryDTO> toTrailSummaryDTOList(List<Trail> trails) {
        if (trails == null) return List.of();
        return trails.stream()
                .map(this::toTrailSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte TrailModule entity para ModuleDTO.
     */
    public ModuleDTO toModuleDTO(TrailModule module) {
        if (module == null) return null;

        return ModuleDTO.builder()
                .id(module.getId())
                .trailId(module.getTrail() != null ? module.getTrail().getId() : null)
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .status(module.getStatus())
                .competencyCode(module.getCompetency() != null ? module.getCompetency().getCode() : null)
                .competencyName(module.getCompetency() != null ? module.getCompetency().getName() : null)
                .lessons(toLessonDTOList(module.getLessons()))
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }

    /**
     * Converte lista de TrailModule entities para lista de ModuleDTO.
     */
    public List<ModuleDTO> toModuleDTOList(List<TrailModule> modules) {
        if (modules == null) return List.of();
        return modules.stream()
                .map(this::toModuleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte Lesson entity para LessonDTO.
     */
    public LessonDTO toLessonDTO(Lesson lesson) {
        if (lesson == null) return null;

        Object contentObj = null;
        if (lesson.getContent() != null) {
            try {
                contentObj = objectMapper.readValue(lesson.getContent(), Object.class);
            } catch (JsonProcessingException e) {
                log.warn("Erro ao converter conteúdo da lição para JSON: {}", e.getMessage());
                contentObj = lesson.getContent();
            }
        }

        return LessonDTO.builder()
                .id(lesson.getId())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .title(lesson.getTitle())
                .type(lesson.getType())
                .orderIndex(lesson.getOrderIndex())
                .durationMinutes(lesson.getDurationMinutes())
                .content(contentObj)
                .isPlaceholder(lesson.getIsPlaceholder())
                .completedAt(lesson.getCompletedAt())
                .score(lesson.getScore())
                .timeSpentSeconds(lesson.getTimeSpentSeconds())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    /**
     * Converte lista de Lesson entities para lista de LessonDTO.
     */
    public List<LessonDTO> toLessonDTOList(List<Lesson> lessons) {
        if (lessons == null) return List.of();
        return lessons.stream()
                .map(this::toLessonDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte TrailProgress entity para TrailProgressDTO.
     */
    public TrailProgressDTO toTrailProgressDTO(TrailProgress progress) {
        if (progress == null) return null;

        return TrailProgressDTO.builder()
                .id(progress.getId())
                .trailId(progress.getTrail() != null ? progress.getTrail().getId() : null)
                .totalLessons(progress.getTotalLessons())
                .lessonsCompleted(progress.getLessonsCompleted())
                .progressPercentage(progress.getProgressPercentage())
                .averageScore(progress.getAverageScore())
                .timeSpentMinutes(progress.getTimeSpentMinutes())
                .lastActivityAt(progress.getLastActivityAt())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    /**
     * Converte Level entity para LevelDTO.
     */
    public LevelDTO toLevelDTO(Level level) {
        if (level == null) return null;

        return LevelDTO.builder()
                .id(level.getId())
                .code(level.getCode())
                .name(level.getName())
                .description(level.getDescription())
                .orderIndex(level.getOrderIndex())
                .build();
    }

    /**
     * Converte lista de Level entities para lista de LevelDTO.
     */
    public List<LevelDTO> toLevelDTOList(List<Level> levels) {
        if (levels == null) return List.of();
        return levels.stream()
                .map(this::toLevelDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte Competency entity para CompetencyDTO.
     */
    public CompetencyDTO toCompetencyDTO(Competency competency) {
        if (competency == null) return null;

        return CompetencyDTO.builder()
                .id(competency.getId())
                .code(competency.getCode())
                .name(competency.getName())
                .description(competency.getDescription())
                .category(null) // Campo não existe ainda na entidade Competency
                .icon(competency.getIcon())
                .orderIndex(competency.getOrderIndex())
                .build();
    }

    /**
     * Converte lista de Competency entities para lista de CompetencyDTO.
     */
    public List<CompetencyDTO> toCompetencyDTOList(List<Competency> competencies) {
        if (competencies == null) return List.of();
        return competencies.stream()
                .map(this::toCompetencyDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte Descriptor entity para DescriptorDTO.
     */
    public DescriptorDTO toDescriptorDTO(Descriptor descriptor) {
        if (descriptor == null) return null;

        return DescriptorDTO.builder()
                .id(descriptor.getId())
                .code(descriptor.getCode())
                .description(descriptor.getDescription())
                .descriptionEn(descriptor.getDescriptionEn())
                .levelCode(descriptor.getLevelCompetency() != null && descriptor.getLevelCompetency().getLevel() != null
                        ? descriptor.getLevelCompetency().getLevel().getCode() : null)
                .competencyCode(descriptor.getLevelCompetency() != null && descriptor.getLevelCompetency().getCompetency() != null
                        ? descriptor.getLevelCompetency().getCompetency().getCode() : null)
                .orderIndex(descriptor.getOrderIndex())
                .isCore(descriptor.getIsCore())
                .estimatedHours(descriptor.getEstimatedHours())
                .build();
    }

    /**
     * Converte lista de Descriptor entities para lista de DescriptorDTO.
     */
    public List<DescriptorDTO> toDescriptorDTOList(List<Descriptor> descriptors) {
        if (descriptors == null) return List.of();
        return descriptors.stream()
                .map(this::toDescriptorDTO)
                .collect(Collectors.toList());
    }
}
