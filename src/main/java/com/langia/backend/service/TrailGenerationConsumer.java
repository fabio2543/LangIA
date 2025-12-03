package com.langia.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.config.RabbitMQConfig;
import com.langia.backend.dto.message.TrailGenerationMessage;
import com.langia.backend.model.Lesson;
import com.langia.backend.model.ModuleStatus;
import com.langia.backend.model.Trail;
import com.langia.backend.model.TrailModule;
import com.langia.backend.model.TrailStatus;
import com.langia.backend.repository.LessonRepository;
import com.langia.backend.repository.TrailModuleRepository;
import com.langia.backend.repository.TrailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumer RabbitMQ para processamento assíncrono de geração de trilhas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrailGenerationConsumer {

    private final TrailRepository trailRepository;
    private final TrailModuleRepository trailModuleRepository;
    private final LessonRepository lessonRepository;
    private final TrailGenerationJobService jobService;
    private final TrailProgressService progressService;
    // TODO: Adicionar ContentGenerationService quando implementar LLM

    /**
     * Processa mensagem de geração de trilha.
     */
    @RabbitListener(queues = RabbitMQConfig.TRAIL_GENERATION_QUEUE)
    @Transactional
    public void processGeneration(TrailGenerationMessage message) {
        log.info("Processando geração da trilha: {} (tentativa {})",
                message.getTrailId(), message.getAttemptNumber());

        try {
            // Buscar trilha
            Trail trail = trailRepository.findById(message.getTrailId())
                    .orElseThrow(() -> new RuntimeException("Trilha não encontrada: " + message.getTrailId()));

            // Verificar se já foi processada
            if (trail.getStatus() == TrailStatus.READY) {
                log.info("Trilha {} já está pronta, ignorando mensagem", message.getTrailId());
                return;
            }

            // Notificar início
            jobService.sendStartNotification(message.getTrailId(), message.getStudentId());

            // Gerar conteúdo dos módulos
            List<TrailModule> modules = trailModuleRepository.findByTrailIdOrderByOrderIndexAsc(message.getTrailId());
            int totalModules = modules.size();
            int processedModules = 0;

            for (TrailModule module : modules) {
                // Gerar conteúdo das lições do módulo
                generateModuleContent(module, message);

                // Atualizar status do módulo
                module.setStatus(ModuleStatus.READY);
                trailModuleRepository.save(module);

                processedModules++;

                // Notificar progresso
                jobService.sendModuleGeneratedNotification(
                        message.getTrailId(),
                        message.getStudentId(),
                        processedModules,
                        totalModules
                );
            }

            // Calcular duração total
            List<Lesson> allLessons = lessonRepository.findByTrailIdOrdered(message.getTrailId());
            int totalMinutes = allLessons.stream()
                    .mapToInt(Lesson::getDurationMinutes)
                    .sum();
            BigDecimal hours = BigDecimal.valueOf(totalMinutes)
                    .divide(BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP);

            // Atualizar trilha para READY
            trail.setEstimatedDurationHours(hours);
            trail.setStatus(TrailStatus.READY);
            trailRepository.save(trail);

            // Atualizar progresso
            progressService.updateTotalLessons(message.getTrailId());

            // Notificar conclusão
            jobService.sendCompletionNotification(
                    message.getTrailId(),
                    message.getStudentId(),
                    totalModules,
                    allLessons.size()
            );

            log.info("Trilha {} gerada com sucesso - {} módulos, {} lições",
                    message.getTrailId(), totalModules, allLessons.size());

        } catch (Exception e) {
            log.error("Erro ao gerar trilha {}: {}", message.getTrailId(), e.getMessage(), e);

            // Tentar retry ou notificar falha
            if (message.canRetry()) {
                log.info("Tentando retry para trilha: {}", message.getTrailId());
                jobService.retryGeneration(message);
            } else {
                // Atualizar status da trilha para falha
                trailRepository.findById(message.getTrailId())
                        .ifPresent(trail -> {
                            trail.setStatus(TrailStatus.PARTIAL);
                            trailRepository.save(trail);
                        });

                jobService.sendFailureNotification(
                        message.getTrailId(),
                        message.getStudentId(),
                        e.getMessage()
                );
            }
        }
    }

    /**
     * Gera conteúdo para um módulo.
     * Por enquanto usa conteúdo placeholder.
     * TODO: Integrar com ContentGenerationService (LLM) quando disponível.
     */
    private void generateModuleContent(TrailModule module, TrailGenerationMessage message) {
        log.debug("Gerando conteúdo para módulo: {}", module.getId());

        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());

        for (Lesson lesson : lessons) {
            // Por enquanto, apenas marca como não-placeholder e adiciona conteúdo básico
            // TODO: Chamar ContentGenerationService para gerar conteúdo real via LLM
            lesson.setIsPlaceholder(false);
            lesson.setContent(generatePlaceholderContent(lesson, message));
            lessonRepository.save(lesson);
        }
    }

    /**
     * Gera conteúdo placeholder para uma lição.
     * Será substituído pela geração via LLM.
     */
    private String generatePlaceholderContent(Lesson lesson, TrailGenerationMessage message) {
        return String.format("""
            {
                "type": "%s",
                "title": "%s",
                "language": "%s",
                "level": "%s",
                "generated": true,
                "placeholder": false,
                "content": {
                    "instructions": "Complete esta atividade de %s",
                    "exercises": []
                }
            }
            """,
                lesson.getType().name(),
                lesson.getTitle(),
                message.getLanguageCode(),
                message.getLevelCode(),
                lesson.getType().name()
        );
    }
}
