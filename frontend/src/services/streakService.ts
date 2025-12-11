import api, { handleApiError, type ApiError } from './api';
import type {
  DailyStreak,
  DailyActivityLog,
  ActivitySummary,
} from '../types';

export const streakService = {
  /**
   * Busca streak atual do usuário para um idioma
   */
  getStreak: async (languageCode: string): Promise<DailyStreak> => {
    const response = await api.get<DailyStreak>(`/streaks/${languageCode}`);
    return response.data;
  },

  /**
   * Busca todas as streaks do usuário (todos os idiomas)
   */
  getAllStreaks: async (): Promise<DailyStreak[]> => {
    const response = await api.get<DailyStreak[]>('/streaks');
    return response.data;
  },

  /**
   * Congela a streak por um dia (streak freeze)
   */
  freezeStreak: async (languageCode: string): Promise<DailyStreak> => {
    const response = await api.post<DailyStreak>(`/streaks/${languageCode}/freeze`);
    return response.data;
  },

  /**
   * Busca log de atividade diária
   */
  getActivityLog: async (languageCode: string, days: number = 30): Promise<DailyActivityLog[]> => {
    const response = await api.get<DailyActivityLog[]>(
      `/activity/log?languageCode=${languageCode}&days=${days}`
    );
    return response.data;
  },

  /**
   * Busca resumo de atividade desde uma data
   */
  getActivitySummary: async (languageCode: string, since?: string): Promise<ActivitySummary> => {
    const params = new URLSearchParams({ languageCode });
    if (since) params.append('since', since);
    const response = await api.get<ActivitySummary>(`/activity/summary?${params}`);
    return response.data;
  },

  /**
   * Registra atividade de estudo (chamado automaticamente ao completar lições)
   */
  logActivity: async (data: {
    languageCode: string;
    lessonsCompleted?: number;
    exercisesCompleted?: number;
    cardsReviewed?: number;
    minutesStudied: number;
    xpEarned?: number;
    skillsPracticed?: string[];
  }): Promise<DailyActivityLog> => {
    const response = await api.post<DailyActivityLog>('/activity/log', data);
    return response.data;
  },
};

export { handleApiError, type ApiError };
