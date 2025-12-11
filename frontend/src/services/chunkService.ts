import api, { handleApiError, type ApiError } from './api';
import type {
  LinguisticChunk,
  ChunkMastery,
  CefrLevel,
  ChunkCategory,
} from '../types';

export const chunkService = {
  /**
   * Busca chunks linguísticos por idioma e nível
   */
  getChunks: async (
    languageCode: string,
    cefrLevel?: CefrLevel,
    category?: ChunkCategory
  ): Promise<LinguisticChunk[]> => {
    const params = new URLSearchParams({ languageCode });
    if (cefrLevel) params.append('cefrLevel', cefrLevel);
    if (category) params.append('category', category);
    const response = await api.get<LinguisticChunk[]>(`/chunks?${params}`);
    return response.data;
  },

  /**
   * Busca um chunk específico por ID
   */
  getChunk: async (chunkId: string): Promise<LinguisticChunk> => {
    const response = await api.get<LinguisticChunk>(`/chunks/${chunkId}`);
    return response.data;
  },

  /**
   * Busca chunks essenciais (core) de um nível
   */
  getCoreChunks: async (languageCode: string, cefrLevel: CefrLevel): Promise<LinguisticChunk[]> => {
    const response = await api.get<LinguisticChunk[]>(
      `/chunks/core?languageCode=${languageCode}&cefrLevel=${cefrLevel}`
    );
    return response.data;
  },

  /**
   * Busca domínio do usuário sobre chunks
   */
  getMastery: async (languageCode: string): Promise<ChunkMastery[]> => {
    const response = await api.get<ChunkMastery[]>(`/chunks/mastery?languageCode=${languageCode}`);
    return response.data;
  },

  /**
   * Atualiza domínio de um chunk após prática
   */
  updateMastery: async (
    chunkId: string,
    quality: number,
    context?: string
  ): Promise<ChunkMastery> => {
    const response = await api.post<ChunkMastery>(`/chunks/${chunkId}/practice`, {
      quality,
      context,
    });
    return response.data;
  },

  /**
   * Busca chunks por categoria
   */
  getByCategory: async (
    languageCode: string,
    category: ChunkCategory
  ): Promise<LinguisticChunk[]> => {
    const response = await api.get<LinguisticChunk[]>(
      `/chunks/category/${category}?languageCode=${languageCode}`
    );
    return response.data;
  },

  /**
   * Busca chunks recomendados para praticar (baixo domínio)
   */
  getRecommended: async (languageCode: string, limit: number = 10): Promise<LinguisticChunk[]> => {
    const response = await api.get<LinguisticChunk[]>(
      `/chunks/recommended?languageCode=${languageCode}&limit=${limit}`
    );
    return response.data;
  },

  /**
   * Busca estatísticas de domínio por categoria
   */
  getMasteryStats: async (languageCode: string): Promise<{
    category: ChunkCategory;
    total: number;
    mastered: number;
    avgMasteryLevel: number;
  }[]> => {
    const response = await api.get(`/chunks/mastery/stats?languageCode=${languageCode}`);
    return response.data;
  },
};

export { handleApiError, type ApiError };
