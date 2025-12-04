import api from './api';
import type { OnboardingStatus, OnboardingCompleteResponse } from '../types';

/**
 * Serviço para gerenciamento do processo de onboarding.
 */
export const onboardingService = {
  /**
   * Obtém o status atual do onboarding do usuário.
   */
  getStatus: async (): Promise<OnboardingStatus> => {
    const response = await api.get<OnboardingStatus>('/onboarding/status');
    return response.data;
  },

  /**
   * Completa o processo de onboarding.
   * Gera automaticamente a trilha para o idioma primário.
   */
  complete: async (): Promise<OnboardingCompleteResponse> => {
    console.log('[onboardingService] complete() called');
    try {
      const response = await api.post<OnboardingCompleteResponse>('/onboarding/complete');
      console.log('[onboardingService] complete() response:', response.data);
      return response.data;
    } catch (error) {
      console.error('[onboardingService] complete() error:', error);
      throw error;
    }
  },

  /**
   * Verifica se o usuário precisa completar o onboarding.
   */
  needsOnboarding: async (): Promise<boolean> => {
    const response = await api.get<boolean>('/onboarding/needs');
    return response.data;
  },
};
