package com.langia.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.config.RabbitMQConfig;
import com.langia.backend.dto.message.TrailGenerationMessage;
import com.langia.backend.dto.message.TrailNotificationMessage;
import com.langia.backend.model.GenerationJobStatus;
import com.langia.backend.model.JobType;
import com.langia.backend.model.Trail;
import com.langia.backend.model.TrailGenerationJob;
import com.langia.backend.repository.TrailGenerationJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para enfileiramento e gerenciamento de jobs de geração de trilhas.
 * Persiste jobs no banco de dados para auditoria e rastreamento.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrailGenerationJobService {

    private final RabbitTemplate rabbitTemplate;
    private final TrailGenerationJobRepository jobRepository;

    /**
     * Enfileira job de geração de trilha.
     * Persiste o job no banco e envia mensagem para RabbitMQ.
     *
     * @param trail Trilha a ser gerada
     * @return Job criado
     */
    @Transactional
    public TrailGenerationJob enqueueGeneration(Trail trail) {
        log.info("Enfileirando geração da trilha: {}", trail.getId());

        // Verifica se já existe job ativo para esta trilha
        if (jobRepository.existsActiveByTrailId(trail.getId())) {
            log.warn("Já existe job ativo para a trilha: {}", trail.getId());
            return jobRepository.findActiveByTrailId(trail.getId()).orElse(null);
        }

        // Cria job no banco de dados
        TrailGenerationJob job = TrailGenerationJob.builder()
                .trail(trail)
                .student(trail.getStudent())
                .status(GenerationJobStatus.QUEUED)
                .jobType(JobType.full_generation)
                .priority(5)
                .build();
        job = jobRepository.save(job);

        // Cria mensagem para RabbitMQ
        TrailGenerationMessage message = TrailGenerationMessage.create(
                trail.getId(),
                trail.getStudent().getId(),
                trail.getLanguage().getCode(),
                trail.getLevel().getCode(),
                trail.getBlueprint() != null ? trail.getBlueprint().getId() : null,
                trail.getCurriculumVersion()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRAIL_EXCHANGE,
                RabbitMQConfig.TRAIL_GENERATION_ROUTING_KEY,
                message
        );

        log.info("Job de geração {} enfileirado para trilha: {}", job.getId(), trail.getId());
        return job;
    }

    /**
     * Reenfileira job para retry.
     *
     * @param message Mensagem original
     */
    public void retryGeneration(TrailGenerationMessage message) {
        if (!message.canRetry()) {
            log.warn("Máximo de tentativas atingido para trilha: {}", message.getTrailId());
            sendFailureNotification(message.getTrailId(), message.getStudentId(),
                    "Máximo de tentativas de geração atingido");
            return;
        }

        TrailGenerationMessage retryMessage = message.retry();
        log.info("Reenfileirando geração da trilha: {} (tentativa {})",
                retryMessage.getTrailId(), retryMessage.getAttemptNumber());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRAIL_EXCHANGE,
                RabbitMQConfig.TRAIL_GENERATION_ROUTING_KEY,
                retryMessage
        );
    }

    /**
     * Envia notificação de progresso.
     */
    public void sendProgressNotification(TrailNotificationMessage notification) {
        log.debug("Enviando notificação de progresso para trilha: {}", notification.getTrailId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRAIL_EXCHANGE,
                RabbitMQConfig.TRAIL_NOTIFICATION_ROUTING_KEY,
                notification
        );
    }

    /**
     * Envia notificação de início de geração.
     */
    public void sendStartNotification(UUID trailId, UUID studentId) {
        sendProgressNotification(TrailNotificationMessage.started(trailId, studentId));
    }

    /**
     * Envia notificação de módulo gerado.
     */
    public void sendModuleGeneratedNotification(UUID trailId, UUID studentId,
                                                 int modulesGenerated, int totalModules) {
        sendProgressNotification(TrailNotificationMessage.moduleGenerated(
                trailId, studentId, modulesGenerated, totalModules));
    }

    /**
     * Envia notificação de conclusão.
     */
    public void sendCompletionNotification(UUID trailId, UUID studentId,
                                           int totalModules, int totalLessons) {
        sendProgressNotification(TrailNotificationMessage.completed(
                trailId, studentId, totalModules, totalLessons));
    }

    /**
     * Envia notificação de falha.
     */
    public void sendFailureNotification(UUID trailId, UUID studentId, String errorMessage) {
        sendProgressNotification(TrailNotificationMessage.failed(trailId, studentId, errorMessage));
    }

    // ========== Métodos de consulta e atualização de jobs ==========

    /**
     * Busca job ativo para uma trilha.
     */
    public Optional<TrailGenerationJob> findActiveJobByTrailId(UUID trailId) {
        return jobRepository.findActiveByTrailId(trailId);
    }

    /**
     * Busca último job de uma trilha.
     */
    public Optional<TrailGenerationJob> findLastJobByTrailId(UUID trailId) {
        return jobRepository.findFirstByTrailIdOrderByCreatedAtDesc(trailId);
    }

    /**
     * Busca jobs de um estudante.
     */
    public List<TrailGenerationJob> findJobsByStudentId(UUID studentId) {
        return jobRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    /**
     * Marca job como em processamento.
     */
    @Transactional
    public void markJobAsProcessing(UUID trailId, String workerId) {
        jobRepository.findActiveByTrailId(trailId).ifPresent(job -> {
            job.markAsProcessing(workerId);
            job.incrementAttempt();
            jobRepository.save(job);
            log.info("Job {} marcado como PROCESSING pelo worker {}", job.getId(), workerId);
        });
    }

    /**
     * Marca job como concluído.
     */
    @Transactional
    public void markJobAsCompleted(UUID trailId, int tokensUsed, int processingTimeMs) {
        jobRepository.findActiveByTrailId(trailId).ifPresent(job -> {
            job.markAsCompleted(tokensUsed, processingTimeMs);
            jobRepository.save(job);
            log.info("Job {} marcado como COMPLETED. Tokens: {}, Tempo: {}ms",
                    job.getId(), tokensUsed, processingTimeMs);
        });
    }

    /**
     * Marca job como falho.
     */
    @Transactional
    public void markJobAsFailed(UUID trailId, String error, String errorDetails) {
        jobRepository.findActiveByTrailId(trailId).ifPresent(job -> {
            job.markAsFailed(error, errorDetails);
            jobRepository.save(job);
            log.warn("Job {} marcado como FAILED: {}", job.getId(), error);
        });
    }

    /**
     * Cancela jobs ativos de uma trilha.
     */
    @Transactional
    public int cancelJobsByTrailId(UUID trailId) {
        int cancelled = jobRepository.cancelByTrailId(trailId);
        if (cancelled > 0) {
            log.info("Cancelados {} jobs para trilha: {}", cancelled, trailId);
        }
        return cancelled;
    }

    /**
     * Conta jobs na fila.
     */
    public long countQueuedJobs() {
        return jobRepository.countByStatus(GenerationJobStatus.QUEUED);
    }

    /**
     * Conta jobs em processamento.
     */
    public long countProcessingJobs() {
        return jobRepository.countByStatus(GenerationJobStatus.PROCESSING);
    }

    /**
     * Busca jobs travados (em processamento há muito tempo).
     */
    public List<TrailGenerationJob> findStaleJobs(int timeoutMinutes) {
        OffsetDateTime timeout = OffsetDateTime.now().minusMinutes(timeoutMinutes);
        return jobRepository.findStaleProcessingJobs(timeout);
    }

    /**
     * Soma total de tokens usados por um estudante.
     */
    public long getTotalTokensUsedByStudent(UUID studentId) {
        return jobRepository.sumTokensUsedByStudentId(studentId);
    }
}
