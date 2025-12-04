package com.langia.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.trail.GenerateTrailRequestDTO;
import com.langia.backend.dto.trail.LessonDTO;
import com.langia.backend.dto.trail.ModuleDTO;
import com.langia.backend.dto.trail.RefreshTrailRequestDTO;
import com.langia.backend.dto.trail.TrailDTO;
import com.langia.backend.dto.trail.TrailProgressDTO;
import com.langia.backend.dto.trail.TrailSummaryDTO;
import com.langia.backend.dto.trail.UpdateLessonProgressDTO;
import com.langia.backend.exception.LessonNotFoundException;
import com.langia.backend.exception.ModuleNotFoundException;
import com.langia.backend.exception.TrailGenerationException;
import com.langia.backend.exception.TrailLimitExceededException;
import com.langia.backend.exception.TrailNotFoundException;
import com.langia.backend.mapper.TrailMapper;
import com.langia.backend.model.Blueprint;
import com.langia.backend.model.Competency;
import com.langia.backend.model.Descriptor;
import com.langia.backend.model.Language;
import com.langia.backend.model.Lesson;
import com.langia.backend.model.LessonType;
import com.langia.backend.model.Level;
import com.langia.backend.model.LevelCompetency;
import com.langia.backend.model.ModuleStatus;
import com.langia.backend.model.RefreshReason;
import com.langia.backend.model.Trail;
import com.langia.backend.model.TrailModule;
import com.langia.backend.model.TrailStatus;
import com.langia.backend.model.User;
import com.langia.backend.repository.LanguageRepository;
import com.langia.backend.repository.LessonRepository;
import com.langia.backend.repository.TrailModuleRepository;
import com.langia.backend.repository.TrailRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço principal para gerenciamento de trilhas de aprendizado.
 * Orquestra a criação, geração e manutenção de trilhas on-demand.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrailService {

    private static final int MAX_ACTIVE_TRAILS = 3;
    private static final int DEFAULT_LESSON_DURATION = 15;

    private final TrailRepository trailRepository;
    private final TrailModuleRepository trailModuleRepository;
    private final LessonRepository lessonRepository;
    private final LanguageRepository languageRepository;
    private final UserRepository userRepository;

    private final TrailMapper trailMapper;
    private final TrailHashService trailHashService;
    private final TrailProgressService trailProgressService;
    private final CurriculumService curriculumService;
    private final BlueprintService blueprintService;
    private final TrailGenerationAIService trailGenerationAIService;

    // ========== BUSCA DE TRILHAS ==========

    /**
     * Busca trilha ativa de um estudante para um idioma.
     * Se não existir, inicia geração on-demand.
     *
     * @param studentId ID do estudante
     * @param languageCode Código do idioma
     * @return Trilha existente ou nova em geração
     */
    @Transactional
    public TrailDTO getOrCreateTrail(UUID studentId, String languageCode) {
        log.info("Buscando trilha para estudante: {}, idioma: {}", studentId, languageCode);

        // Verificar se já existe trilha ativa
        Optional<Trail> existingTrail = trailRepository.findActiveByStudentAndLanguage(studentId, languageCode);

        if (existingTrail.isPresent()) {
            log.info("Trilha existente encontrada: {}", existingTrail.get().getId());
            return trailMapper.toTrailDTO(existingTrail.get());
        }

        // Criar nova trilha
        log.info("Criando nova trilha para estudante: {}, idioma: {}", studentId, languageCode);
        return createTrail(studentId, languageCode, null);
    }

    /**
     * Busca trilha por ID.
     */
    @Transactional(readOnly = true)
    public TrailDTO getTrailById(UUID trailId) {
        Trail trail = trailRepository.findById(trailId)
                .orElseThrow(() -> new TrailNotFoundException(trailId));
        return trailMapper.toTrailDTO(trail);
    }

    /**
     * Busca todas as trilhas ativas de um estudante.
     */
    @Transactional(readOnly = true)
    public List<TrailSummaryDTO> getActiveTrails(UUID studentId) {
        List<Trail> trails = trailRepository.findActiveByStudentId(studentId);
        return trailMapper.toTrailSummaryDTOList(trails);
    }

    /**
     * Busca trilhas prontas de um estudante.
     */
    @Transactional(readOnly = true)
    public List<TrailSummaryDTO> getReadyTrails(UUID studentId) {
        List<Trail> trails = trailRepository.findReadyByStudentId(studentId);
        return trailMapper.toTrailSummaryDTOList(trails);
    }

    // ========== CRIAÇÃO DE TRILHAS ==========

    /**
     * Cria uma nova trilha para o estudante.
     */
    @Transactional
    public TrailDTO createTrail(UUID studentId, String languageCode, String preferencesJson) {
        log.info("Criando trilha - Estudante: {}, Idioma: {}", studentId, languageCode);

        // Validar limite de trilhas ativas
        long activeCount = trailRepository.countActiveByStudentId(studentId);
        if (activeCount >= MAX_ACTIVE_TRAILS) {
            throw new TrailLimitExceededException(MAX_ACTIVE_TRAILS);
        }

        // Buscar entidades necessárias
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado: " + studentId));

        Language language = languageRepository.findById(languageCode)
                .orElseThrow(() -> new RuntimeException("Idioma não encontrado: " + languageCode));

        // Determinar nível inicial (A1 por padrão, ou baseado em assessment)
        Level level = curriculumService.getLevelEntityByCode("A1")
                .orElseThrow(() -> new RuntimeException("Nível A1 não encontrado"));

        // Calcular hash para cache
        String curriculumVersion = curriculumService.getCurrentCurriculumVersion();
        String contentHash = trailHashService.calculateTrailHash(
                studentId, languageCode, level.getCode(), preferencesJson, curriculumVersion);

        // Verificar cache por hash
        Optional<Trail> cachedTrail = trailRepository.findByContentHash(contentHash);
        if (cachedTrail.isPresent() && cachedTrail.get().getStatus() == TrailStatus.READY) {
            log.info("Trilha encontrada em cache pelo hash: {}", contentHash);
            // Criar cópia da trilha cacheada para o estudante
            return cloneTrailForStudent(cachedTrail.get(), student);
        }

        // Buscar blueprint adequado
        Optional<Blueprint> blueprint = blueprintService.findMatchingBlueprint(
                languageCode, level.getCode(), preferencesJson);

        // Criar trilha
        Trail trail = Trail.builder()
                .student(student)
                .language(language)
                .level(level)
                .blueprint(blueprint.orElse(null))
                .status(TrailStatus.GENERATING)
                .contentHash(contentHash)
                .curriculumVersion(curriculumVersion)
                .build();

        trail = trailRepository.save(trail);
        log.info("Trilha criada com ID: {}", trail.getId());

        // Se tiver blueprint, criar estrutura base
        if (blueprint.isPresent()) {
            blueprintService.incrementUsageCount(blueprint.get().getId());
            createModulesFromBlueprint(trail, blueprint.get());
        } else {
            // Criar estrutura padrão baseada nas competências do nível
            createDefaultModules(trail);
        }

        // Atualizar status para PARTIAL (estrutura criada, conteúdo pendente)
        trail.setStatus(TrailStatus.PARTIAL);
        trail = trailRepository.save(trail);

        // Criar registro de progresso
        int totalLessons = (int) lessonRepository.countByTrailId(trail.getId());
        trailProgressService.createProgress(trail, totalLessons);

        // Em produção, aqui enfileiraria job de geração de conteúdo via RabbitMQ
        // Por enquanto, simula geração completa
        completeTrailGeneration(trail.getId());

        return trailMapper.toTrailDTO(trail);
    }

    /**
     * Força geração de trilha (mesmo se já existir).
     */
    @Transactional
    public TrailDTO generateTrail(UUID studentId, GenerateTrailRequestDTO request) {
        log.info("Gerando trilha forçada - Estudante: {}, Idioma: {}", studentId, request.getLanguageCode());

        if (Boolean.TRUE.equals(request.getForceRegenerate())) {
            // Arquivar trilha existente
            trailRepository.findActiveByStudentAndLanguage(studentId, request.getLanguageCode())
                    .ifPresent(existing -> {
                        log.info("Arquivando trilha existente: {}", existing.getId());
                        existing.setStatus(TrailStatus.ARCHIVED);
                        existing.setArchivedAt(LocalDateTime.now());
                        trailRepository.save(existing);
                    });
        }

        return createTrail(studentId, request.getLanguageCode(), null);
    }

    // ========== REFRESH DE TRILHAS ==========

    /**
     * Regenera uma trilha existente.
     */
    @Transactional
    public TrailDTO refreshTrail(UUID trailId, RefreshTrailRequestDTO request) {
        log.info("Refresh de trilha: {}, motivo: {}", trailId, request.getReason());

        Trail oldTrail = trailRepository.findById(trailId)
                .orElseThrow(() -> new TrailNotFoundException(trailId));

        // Arquivar trilha antiga
        oldTrail.setStatus(TrailStatus.ARCHIVED);
        oldTrail.setArchivedAt(LocalDateTime.now());
        trailRepository.save(oldTrail);

        // Determinar novo nível
        Level newLevel = oldTrail.getLevel();
        if (request.getReason() == RefreshReason.level_change && request.getNewLevelCode() != null) {
            newLevel = curriculumService.getLevelEntityByCode(request.getNewLevelCode())
                    .orElseThrow(() -> new RuntimeException("Nível não encontrado: " + request.getNewLevelCode()));
        }

        // Criar nova trilha
        Trail newTrail = Trail.builder()
                .student(oldTrail.getStudent())
                .language(oldTrail.getLanguage())
                .level(newLevel)
                .status(TrailStatus.GENERATING)
                .contentHash(trailHashService.calculateTrailHash(
                        oldTrail.getStudent().getId(),
                        oldTrail.getLanguage().getCode(),
                        newLevel.getCode(),
                        null,
                        curriculumService.getCurrentCurriculumVersion()))
                .curriculumVersion(curriculumService.getCurrentCurriculumVersion())
                .previousTrail(oldTrail)
                .refreshReason(request.getReason())
                .build();

        newTrail = trailRepository.save(newTrail);
        log.info("Nova trilha criada após refresh: {}", newTrail.getId());

        // Criar estrutura e simular geração
        createDefaultModules(newTrail);
        newTrail.setStatus(TrailStatus.PARTIAL);
        trailRepository.save(newTrail);

        int totalLessons = (int) lessonRepository.countByTrailId(newTrail.getId());
        trailProgressService.createProgress(newTrail, totalLessons);

        completeTrailGeneration(newTrail.getId());

        return trailMapper.toTrailDTO(newTrail);
    }

    // ========== MÓDULOS ==========

    /**
     * Busca módulo por ID.
     */
    @Transactional(readOnly = true)
    public ModuleDTO getModuleById(UUID moduleId) {
        TrailModule module = trailModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundException(moduleId));
        return trailMapper.toModuleDTO(module);
    }

    /**
     * Busca módulos de uma trilha.
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> getModulesByTrailId(UUID trailId) {
        List<TrailModule> modules = trailModuleRepository.findByTrailIdWithLessons(trailId);
        return trailMapper.toModuleDTOList(modules);
    }

    // ========== LIÇÕES ==========

    /**
     * Busca lição por ID.
     */
    @Transactional(readOnly = true)
    public LessonDTO getLessonById(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));
        return trailMapper.toLessonDTO(lesson);
    }

    /**
     * Busca próxima lição não completada de uma trilha.
     */
    @Transactional(readOnly = true)
    public Optional<LessonDTO> getNextLesson(UUID trailId) {
        return lessonRepository.findNextIncompleteByTrailId(trailId)
                .map(trailMapper::toLessonDTO);
    }

    /**
     * Atualiza progresso de uma lição.
     */
    @Transactional
    public LessonDTO updateLessonProgress(UUID lessonId, UpdateLessonProgressDTO request) {
        log.info("Atualizando progresso da lição: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));

        if (Boolean.TRUE.equals(request.getCompleted())) {
            lesson.setCompletedAt(LocalDateTime.now());
        }

        if (request.getScore() != null) {
            lesson.setScore(request.getScore());
        }

        if (request.getTimeSpentSeconds() != null) {
            // Acumular tempo gasto
            int currentTime = lesson.getTimeSpentSeconds() != null ? lesson.getTimeSpentSeconds() : 0;
            lesson.setTimeSpentSeconds(currentTime + request.getTimeSpentSeconds());
        }

        lesson = lessonRepository.save(lesson);

        // Recalcular progresso da trilha
        UUID trailId = lesson.getModule().getTrail().getId();
        trailProgressService.recalculateProgress(trailId);

        log.info("Progresso da lição atualizado: {}", lessonId);
        return trailMapper.toLessonDTO(lesson);
    }

    // ========== PROGRESSO ==========

    /**
     * Busca progresso de uma trilha.
     */
    @Transactional(readOnly = true)
    public TrailProgressDTO getProgress(UUID trailId) {
        return trailProgressService.getProgress(trailId)
                .orElseThrow(() -> new TrailNotFoundException(trailId));
    }

    // ========== ARQUIVAMENTO ==========

    /**
     * Arquiva uma trilha.
     */
    @Transactional
    public void archiveTrail(UUID trailId) {
        log.info("Arquivando trilha: {}", trailId);
        trailRepository.archiveById(trailId);
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    /**
     * Cria módulos baseados em blueprint.
     */
    private void createModulesFromBlueprint(Trail trail, Blueprint blueprint) {
        log.debug("Criando módulos a partir do blueprint: {}", blueprint.getId());

        // Por enquanto, cria estrutura padrão
        // Em produção, parsearia blueprint.getStructure() (JSON)
        createDefaultModules(trail);
    }

    /**
     * Cria módulos padrão baseados nas competências do nível.
     */
    private void createDefaultModules(Trail trail) {
        log.debug("Criando módulos padrão para trilha: {}", trail.getId());

        List<LevelCompetency> levelCompetencies = curriculumService.getLevelCompetencies(trail.getLevel().getCode());

        int orderIndex = 0;
        for (LevelCompetency lc : levelCompetencies) {
            Competency competency = lc.getCompetency();

            TrailModule module = TrailModule.builder()
                    .trail(trail)
                    .competency(competency)
                    .title(competency.getName())
                    .description("Módulo de " + competency.getName())
                    .orderIndex(orderIndex++)
                    .status(ModuleStatus.PENDING)
                    .lessons(new ArrayList<>())
                    .build();

            module = trailModuleRepository.save(module);

            // Criar lições placeholder baseadas nos descritores
            createPlaceholderLessons(module, lc);
        }
    }

    /**
     * Cria lições placeholder para um módulo.
     */
    private void createPlaceholderLessons(TrailModule module, LevelCompetency levelCompetency) {
        List<Descriptor> descriptors = curriculumService.getCoreDescriptors(levelCompetency.getId());

        int lessonOrder = 0;
        for (Descriptor descriptor : descriptors) {
            // Criar uma lição por descritor
            Lesson lesson = Lesson.builder()
                    .module(module)
                    .title(descriptor.getDescription())
                    .type(getDefaultLessonType(module.getCompetency().getCode()))
                    .orderIndex(lessonOrder++)
                    .durationMinutes(DEFAULT_LESSON_DURATION)
                    .content("{}")
                    .isPlaceholder(true)
                    .build();

            lessonRepository.save(lesson);
        }

        // Se não houver descritores, criar pelo menos 3 lições genéricas
        if (descriptors.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                Lesson lesson = Lesson.builder()
                        .module(module)
                        .title("Lição " + (i + 1) + " - " + module.getTitle())
                        .type(getDefaultLessonType(module.getCompetency().getCode()))
                        .orderIndex(lessonOrder++)
                        .durationMinutes(DEFAULT_LESSON_DURATION)
                        .content("{}")
                        .isPlaceholder(true)
                        .build();

                lessonRepository.save(lesson);
            }
        }
    }

    /**
     * Determina tipo de lição padrão baseado na competência.
     */
    private LessonType getDefaultLessonType(String competencyCode) {
        return switch (competencyCode) {
            case "speaking" -> LessonType.conversation;
            case "listening" -> LessonType.video;
            case "reading" -> LessonType.reading;
            case "writing" -> LessonType.exercise;
            case "vocabulary" -> LessonType.flashcard;
            case "grammar" -> LessonType.interactive;
            default -> LessonType.interactive;
        };
    }

    /**
     * Clona trilha existente para outro estudante.
     */
    private TrailDTO cloneTrailForStudent(Trail sourceTrail, User student) {
        log.info("Clonando trilha {} para estudante {}", sourceTrail.getId(), student.getId());

        Trail clone = Trail.builder()
                .student(student)
                .language(sourceTrail.getLanguage())
                .level(sourceTrail.getLevel())
                .blueprint(sourceTrail.getBlueprint())
                .status(TrailStatus.READY)
                .contentHash(sourceTrail.getContentHash())
                .curriculumVersion(sourceTrail.getCurriculumVersion())
                .estimatedDurationHours(sourceTrail.getEstimatedDurationHours())
                .build();

        clone = trailRepository.save(clone);

        // Clonar módulos e lições
        for (TrailModule sourceModule : sourceTrail.getModules()) {
            TrailModule cloneModule = TrailModule.builder()
                    .trail(clone)
                    .competency(sourceModule.getCompetency())
                    .title(sourceModule.getTitle())
                    .description(sourceModule.getDescription())
                    .orderIndex(sourceModule.getOrderIndex())
                    .status(ModuleStatus.READY)
                    .lessons(new ArrayList<>())
                    .build();

            cloneModule = trailModuleRepository.save(cloneModule);

            for (Lesson sourceLesson : sourceModule.getLessons()) {
                Lesson cloneLesson = Lesson.builder()
                        .module(cloneModule)
                        .contentBlock(sourceLesson.getContentBlock())
                        .title(sourceLesson.getTitle())
                        .type(sourceLesson.getType())
                        .orderIndex(sourceLesson.getOrderIndex())
                        .durationMinutes(sourceLesson.getDurationMinutes())
                        .content(sourceLesson.getContent())
                        .isPlaceholder(false)
                        .build();

                lessonRepository.save(cloneLesson);
            }
        }

        int totalLessons = (int) lessonRepository.countByTrailId(clone.getId());
        trailProgressService.createProgress(clone, totalLessons);

        return trailMapper.toTrailDTO(clone);
    }

    /**
     * Completa a geração de trilha usando IA para personalizar o conteúdo.
     * Utiliza as preferências e assessment do estudante para gerar conteúdo relevante.
     */
    private void completeTrailGeneration(UUID trailId) {
        log.info("Gerando trilha personalizada com IA: {}", trailId);

        Trail trail = trailRepository.findById(trailId)
                .orElseThrow(() -> new TrailGenerationException("Trilha não encontrada: " + trailId));

        UUID studentId = trail.getStudent().getId();
        String languageCode = trail.getLanguage().getCode();
        String levelCode = trail.getLevel().getCode();

        // Gerar conteúdo personalizado para cada lição usando IA
        List<Lesson> lessons = lessonRepository.findByTrailIdOrdered(trailId);
        for (Lesson lesson : lessons) {
            lesson.setIsPlaceholder(false);

            try {
                // Gerar conteúdo da lição com IA
                String generatedContent = trailGenerationAIService.generateLessonContent(
                        lesson.getTitle(),
                        lesson.getType().name(),
                        studentId,
                        languageCode,
                        levelCode
                );
                lesson.setContent(generatedContent);
                log.debug("Conteúdo gerado para lição: {}", lesson.getTitle());
            } catch (Exception e) {
                log.warn("Erro ao gerar conteúdo da lição {}: {}. Usando conteúdo padrão.",
                        lesson.getTitle(), e.getMessage());
                lesson.setContent("{\"type\":\"" + lesson.getType().name() + "\",\"generated\":false}");
            }
        }
        lessonRepository.saveAll(lessons);

        // Atualizar módulos para READY
        List<TrailModule> modules = trailModuleRepository.findByTrailIdOrderByOrderIndexAsc(trailId);
        for (TrailModule module : modules) {
            module.setStatus(ModuleStatus.READY);
        }
        trailModuleRepository.saveAll(modules);

        // Calcular duração estimada
        int totalMinutes = lessons.stream()
                .mapToInt(Lesson::getDurationMinutes)
                .sum();
        BigDecimal hours = BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP);
        trail.setEstimatedDurationHours(hours);

        // Atualizar status final
        trail.setStatus(TrailStatus.READY);
        trailRepository.save(trail);

        // Atualizar progresso
        trailProgressService.updateTotalLessons(trailId);

        log.info("Trilha {} gerada com IA - {} módulos, {} lições, {}h estimadas",
                trailId, modules.size(), lessons.size(), hours);
    }
}
