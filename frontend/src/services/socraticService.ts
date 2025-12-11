import api, { handleApiError, type ApiError } from './api';
import type {
  SocraticInteraction,
  SocraticFeedbackRequest,
  SocraticFeedbackResponse,
  SocraticReflectionRequest,
  SocraticReflectionResponse,
} from '../types';

export const socraticService = {
  /**
   * Solicita feedback socrático da IA para uma resposta do aluno
   * A IA responde com uma pergunta guiada em vez de correção direta
   */
  requestFeedback: async (data: SocraticFeedbackRequest): Promise<SocraticFeedbackResponse> => {
    const response = await api.post<SocraticFeedbackResponse>('/socratic/feedback', data);
    return response.data;
  },

  /**
   * Envia a reflexão do aluno após a pergunta socrática
   * Retorna se precisa de mais perguntas ou a correção final
   */
  submitReflection: async (
    data: SocraticReflectionRequest
  ): Promise<SocraticReflectionResponse> => {
    const response = await api.post<SocraticReflectionResponse>('/socratic/reflection', data);
    return response.data;
  },

  /**
   * Avalia a interação socrática (1-5 estrelas)
   */
  rateInteraction: async (interactionId: string, rating: number): Promise<void> => {
    await api.post(`/socratic/${interactionId}/rate`, { rating });
  },

  /**
   * Busca histórico de interações socráticas
   */
  getHistory: async (languageCode: string, limit: number = 20): Promise<SocraticInteraction[]> => {
    const response = await api.get<SocraticInteraction[]>(
      `/socratic/history?languageCode=${languageCode}&limit=${limit}`
    );
    return response.data;
  },

  /**
   * Busca uma interação específica por ID
   */
  getInteraction: async (interactionId: string): Promise<SocraticInteraction> => {
    const response = await api.get<SocraticInteraction>(`/socratic/${interactionId}`);
    return response.data;
  },

  /**
   * Busca estatísticas de interações socráticas
   */
  getStats: async (languageCode: string): Promise<{
    totalInteractions: number;
    selfCorrectionRate: number;
    avgInteractionRounds: number;
    avgRating: number;
    tokensUsedTotal: number;
  }> => {
    const response = await api.get(`/socratic/stats?languageCode=${languageCode}`);
    return response.data;
  },

  /**
   * Busca interações bem-sucedidas (onde o aluno se autocorrigiu)
   */
  getSuccessfulInteractions: async (
    languageCode: string,
    limit: number = 10
  ): Promise<SocraticInteraction[]> => {
    const response = await api.get<SocraticInteraction[]>(
      `/socratic/successful?languageCode=${languageCode}&limit=${limit}`
    );
    return response.data;
  },
};

export { handleApiError, type ApiError };
