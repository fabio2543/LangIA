import api, { handleApiError, type ApiError } from './api';
import type {
  ExerciseResponse,
  SubmitExerciseRequest,
  ErrorPattern,
} from '../types';

export const exerciseService = {
  /**
   * Submete resposta de exercício para tracking
   */
  submitResponse: async (data: SubmitExerciseRequest): Promise<ExerciseResponse> => {
    const response = await api.post<ExerciseResponse>('/exercises/responses', data);
    return response.data;
  },

  /**
   * Busca histórico de respostas de exercícios
   */
  getHistory: async (languageCode: string, limit: number = 50): Promise<ExerciseResponse[]> => {
    const response = await api.get<ExerciseResponse[]>(
      `/exercises/history?languageCode=${languageCode}&limit=${limit}`
    );
    return response.data;
  },

  /**
   * Busca histórico de respostas por lição
   */
  getHistoryByLesson: async (lessonId: string): Promise<ExerciseResponse[]> => {
    const response = await api.get<ExerciseResponse[]>(`/exercises/history/lesson/${lessonId}`);
    return response.data;
  },

  /**
   * Busca padrões de erros do usuário
   */
  getErrorPatterns: async (languageCode: string): Promise<ErrorPattern[]> => {
    const response = await api.get<ErrorPattern[]>(
      `/exercises/errors?languageCode=${languageCode}`
    );
    return response.data;
  },

  /**
   * Busca top N erros mais frequentes
   */
  getTopErrors: async (languageCode: string, limit: number = 5): Promise<ErrorPattern[]> => {
    const response = await api.get<ErrorPattern[]>(
      `/exercises/errors/top?languageCode=${languageCode}&limit=${limit}`
    );
    return response.data;
  },

  /**
   * Marca um padrão de erro como resolvido
   */
  markErrorResolved: async (errorId: string): Promise<void> => {
    await api.patch(`/exercises/errors/${errorId}/resolve`);
  },

  /**
   * Busca estatísticas de exercícios por tipo
   */
  getStatsByType: async (languageCode: string): Promise<{
    exerciseType: string;
    total: number;
    correct: number;
    accuracy: number;
  }[]> => {
    const response = await api.get(`/exercises/stats/by-type?languageCode=${languageCode}`);
    return response.data;
  },
};

export { handleApiError, type ApiError };
