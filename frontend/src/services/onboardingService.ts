import { api } from './api';
import type { OnboardingStatus, OnboardingCompleteResponse } from '../types';

/**
 * Serviço para gerenciamento do processo de onboarding.
 */
export const onboardingService = {
  /**
   * Obtém o status atual do onboarding do usuário.
   */
  getStatus: async (): Promise<OnboardingStatus> => {
    const response = await api.get<OnboardingStatus>('/api/onboarding/status');
    return response.data;
  },

  /**
   * Completa o processo de onboarding.
   * Gera automaticamente a trilha para o idioma primário.
   */
  complete: async (): Promise<OnboardingCompleteResponse> => {
    const response = await api.post<OnboardingCompleteResponse>('/api/onboarding/complete');
    return response.data;
  },

  /**
   * Verifica se o usuário precisa completar o onboarding.
   */
  needsOnboarding: async (): Promise<boolean> => {
    const response = await api.get<boolean>('/api/onboarding/needs');
    return response.data;
  },
};
