package com.langia.backend.dto.onboarding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa o status atual do onboarding do usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingStatusDTO {

    /**
     * Indica se o onboarding foi completado.
     */
    private boolean completed;

    /**
     * Status de cada etapa do onboarding.
     */
    private OnboardingStepsStatus steps;

    /**
     * Próxima etapa a ser completada (null se todas completas).
     */
    private String nextStep;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OnboardingStepsStatus {
        /**
         * Se o perfil básico está completo (nome, etc).
         */
        private boolean profileComplete;

        /**
         * Se há pelo menos um idioma inscrito.
         */
        private boolean languageEnrolled;

        /**
         * Se as preferências de aprendizado foram configuradas.
         */
        private boolean preferencesSet;

        /**
         * Se há pelo menos uma autoavaliação completa.
         */
        private boolean assessmentDone;
    }
}
