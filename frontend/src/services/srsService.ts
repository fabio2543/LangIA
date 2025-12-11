import api, { handleApiError, type ApiError } from './api';
import type {
  SrsReviewRequest,
  SrsReviewResponse,
  SrsDueCardsResponse,
  SrsProgress,
  SrsStats,
  CefrLevel,
} from '../types';

export const srsService = {
  /**
   * Busca cards pendentes de revisão para hoje
   */
  getDueCards: async (languageCode: string, limit?: number): Promise<SrsDueCardsResponse> => {
    const params = new URLSearchParams({ languageCode });
    if (limit) params.append('limit', limit.toString());
    const response = await api.get<SrsDueCardsResponse>(`/srs/due?${params}`);
    return response.data;
  },

  /**
   * Registra revisão de um card com qualidade (0-5, algoritmo SM-2)
   * 0 = Total blackout
   * 1 = Errou, mas lembrou ao ver resposta
   * 2 = Errou, mas era quase
   * 3 = Correto, com dificuldade
   * 4 = Correto, com hesitação
   * 5 = Perfeito, resposta imediata
   */
  reviewCard: async (data: SrsReviewRequest): Promise<SrsReviewResponse> => {
    const response = await api.post<SrsReviewResponse>('/srs/review', data);
    return response.data;
  },

  /**
   * Busca progresso SRS de todos os cards do usuário
   */
  getProgress: async (languageCode: string): Promise<SrsProgress[]> => {
    const response = await api.get<SrsProgress[]>(`/srs/progress?languageCode=${languageCode}`);
    return response.data;
  },

  /**
   * Busca estatísticas gerais do SRS
   */
  getStats: async (languageCode: string): Promise<SrsStats> => {
    const response = await api.get<SrsStats>(`/srs/stats?languageCode=${languageCode}`);
    return response.data;
  },

  /**
   * Adiciona um novo card ao sistema SRS
   */
  addCard: async (data: {
    languageCode: string;
    cefrLevel: CefrLevel;
    cardType: 'word' | 'chunk' | 'phrase' | 'grammar';
    front: string;
    back: string;
    context?: string;
    exampleSentence?: string;
    tags?: string[];
  }): Promise<{ cardId: string; message: string }> => {
    const response = await api.post('/srs/cards', data);
    return response.data;
  },

  /**
   * Remove um card do sistema SRS
   */
  removeCard: async (cardId: string): Promise<void> => {
    await api.delete(`/srs/cards/${cardId}`);
  },
};

export { handleApiError, type ApiError };
