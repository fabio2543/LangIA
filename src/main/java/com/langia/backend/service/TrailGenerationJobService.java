package com.langia.backend.service;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.langia.backend.config.RabbitMQConfig;
import com.langia.backend.dto.message.TrailGenerationMessage;
import com.langia.backend.dto.message.TrailNotificationMessage;
import com.langia.backend.model.Trail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para enfileiramento e gerenciamento de jobs de geração de trilhas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrailGenerationJobService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Enfileira job de geração de trilha.
     *
     * @param trail Trilha a ser gerada
     */
    public void enqueueGeneration(Trail trail) {
        log.info("Enfileirando geração da trilha: {}", trail.getId());

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

        log.info("Job de geração enfileirado para trilha: {}", trail.getId());
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
}
