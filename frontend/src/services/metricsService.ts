import api, { handleApiError, type ApiError } from './api';
import type {
  SkillMetric,
  SkillMetricsSummary,
  DailyProgressPoint,
  SkillType,
} from '../types';

export const metricsService = {
  /**
   * Busca métricas por habilidade desde uma data
   */
  getSkillMetrics: async (languageCode: string, since?: string): Promise<SkillMetric[]> => {
    const params = new URLSearchParams({ languageCode });
    if (since) params.append('since', since);
    const response = await api.get<SkillMetric[]>(`/metrics/skills?${params}`);
    return response.data;
  },

  /**
   * Busca resumo de métricas por habilidade (com tendência)
   */
  getSummary: async (languageCode: string): Promise<SkillMetricsSummary[]> => {
    const response = await api.get<SkillMetricsSummary[]>(
      `/metrics/summary?languageCode=${languageCode}`
    );
    return response.data;
  },

  /**
   * Busca progresso diário para gráfico
   */
  getDailyProgress: async (
    languageCode: string,
    days: number = 30
  ): Promise<DailyProgressPoint[]> => {
    const response = await api.get<DailyProgressPoint[]>(
      `/metrics/daily-progress?languageCode=${languageCode}&days=${days}`
    );
    return response.data;
  },

  /**
   * Busca métricas de uma habilidade específica
   */
  getSkillDetail: async (
    languageCode: string,
    skillType: SkillType,
    days: number = 30
  ): Promise<SkillMetric[]> => {
    const response = await api.get<SkillMetric[]>(
      `/metrics/skills/${skillType}?languageCode=${languageCode}&days=${days}`
    );
    return response.data;
  },

  /**
   * Busca estatísticas gerais de desempenho
   */
  getOverallStats: async (languageCode: string): Promise<{
    totalExercises: number;
    avgAccuracy: number;
    avgResponseTimeMs: number;
    totalPracticeMinutes: number;
    totalXp: number;
    strongestSkill: SkillType;
    weakestSkill: SkillType;
  }> => {
    const response = await api.get(`/metrics/overall?languageCode=${languageCode}`);
    return response.data;
  },
};

export { handleApiError, type ApiError };
