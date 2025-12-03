package com.langia.backend.dto.message;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.langia.backend.model.GenerationJobStatus;
import com.langia.backend.model.TrailStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mensagem de notificação de progresso de geração de trilha.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrailNotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum NotificationType {
        GENERATION_STARTED,
        MODULE_GENERATED,
        LESSON_GENERATED,
        GENERATION_COMPLETED,
        GENERATION_FAILED
    }

    private UUID trailId;
    private UUID studentId;
    private NotificationType type;
    private TrailStatus trailStatus;
    private GenerationJobStatus jobStatus;
    private Integer progressPercentage;
    private String currentStep;
    private String message;
    private Integer modulesGenerated;
    private Integer totalModules;
    private Integer lessonsGenerated;
    private Integer totalLessons;
    private String errorMessage;
    private LocalDateTime timestamp;

    /**
     * Cria notificação de início de geração.
     */
    public static TrailNotificationMessage started(UUID trailId, UUID studentId) {
        return TrailNotificationMessage.builder()
                .trailId(trailId)
                .studentId(studentId)
                .type(NotificationType.GENERATION_STARTED)
                .trailStatus(TrailStatus.GENERATING)
                .jobStatus(GenerationJobStatus.PROCESSING)
                .progressPercentage(0)
                .currentStep("Iniciando geração da trilha")
                .message("Sua trilha está sendo gerada...")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Cria notificação de módulo gerado.
     */
    public static TrailNotificationMessage moduleGenerated(UUID trailId, UUID studentId,
                                                           int modulesGenerated, int totalModules) {
        int progress = (modulesGenerated * 100) / Math.max(totalModules, 1);
        return TrailNotificationMessage.builder()
                .trailId(trailId)
                .studentId(studentId)
                .type(NotificationType.MODULE_GENERATED)
                .trailStatus(TrailStatus.PARTIAL)
                .jobStatus(GenerationJobStatus.PROCESSING)
                .progressPercentage(progress)
                .currentStep("Gerando módulos")
                .message("Módulo " + modulesGenerated + " de " + totalModules + " gerado")
                .modulesGenerated(modulesGenerated)
                .totalModules(totalModules)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Cria notificação de conclusão.
     */
    public static TrailNotificationMessage completed(UUID trailId, UUID studentId,
                                                      int totalModules, int totalLessons) {
        return TrailNotificationMessage.builder()
                .trailId(trailId)
                .studentId(studentId)
                .type(NotificationType.GENERATION_COMPLETED)
                .trailStatus(TrailStatus.READY)
                .jobStatus(GenerationJobStatus.COMPLETED)
                .progressPercentage(100)
                .currentStep("Concluído")
                .message("Sua trilha está pronta!")
                .modulesGenerated(totalModules)
                .totalModules(totalModules)
                .lessonsGenerated(totalLessons)
                .totalLessons(totalLessons)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Cria notificação de falha.
     */
    public static TrailNotificationMessage failed(UUID trailId, UUID studentId, String errorMessage) {
        return TrailNotificationMessage.builder()
                .trailId(trailId)
                .studentId(studentId)
                .type(NotificationType.GENERATION_FAILED)
                .trailStatus(TrailStatus.GENERATING)
                .jobStatus(GenerationJobStatus.FAILED)
                .progressPercentage(0)
                .currentStep("Erro")
                .message("Erro ao gerar trilha. Tente novamente.")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
