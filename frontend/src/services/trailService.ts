import api from './api';
import { logger } from '../utils/logger';
import type {
  Trail,
  TrailSummary,
  TrailModule,
  Lesson,
  TrailProgress,
  GenerateTrailRequest,
  RefreshTrailRequest,
  UpdateLessonProgressRequest,
  LanguageEnrollment,
} from '../types/trail';

// ============================================
// Language Enrollment Endpoints
// ============================================

export const languageService = {
  /**
   * Busca os idiomas em que o usuário está inscrito.
   */
  getEnrollments: async (): Promise<LanguageEnrollment[]> => {
    const response = await api.get<LanguageEnrollment[]>('/profile/languages');
    return response.data;
  },
};

// ============================================
// Trail Endpoints
// ============================================

export const trailService = {
  /**
   * Busca trilha ativa para um idioma.
   * Se não existir, inicia geração on-demand.
   */
  getTrailByLanguage: async (languageCode: string): Promise<Trail> => {
    const response = await api.get<Trail>('/trails', {
      params: { lang: languageCode },
    });
    return response.data;
  },

  /**
   * Busca trilha por ID.
   */
  getTrailById: async (trailId: string): Promise<Trail> => {
    const response = await api.get<Trail>(`/trails/${trailId}`);
    return response.data;
  },

  /**
   * Lista todas as trilhas ativas do estudante.
   */
  getActiveTrails: async (): Promise<TrailSummary[]> => {
    const response = await api.get<TrailSummary[]>('/trails/active');
    return response.data;
  },

  /**
   * Força geração de nova trilha.
   */
  generateTrail: async (request: GenerateTrailRequest): Promise<Trail> => {
    const response = await api.post<Trail>('/trails/generate', request);
    return response.data;
  },

  /**
   * Regenera trilha existente.
   */
  refreshTrail: async (trailId: string, request: RefreshTrailRequest): Promise<Trail> => {
    const response = await api.post<Trail>(`/trails/${trailId}/refresh`, request);
    return response.data;
  },

  /**
   * Arquiva uma trilha.
   */
  archiveTrail: async (trailId: string): Promise<{ message: string }> => {
    const response = await api.delete<{ message: string }>(`/trails/${trailId}`);
    return response.data;
  },

  // ============================================
  // Module Endpoints
  // ============================================

  /**
   * Lista módulos de uma trilha.
   */
  getModules: async (trailId: string): Promise<TrailModule[]> => {
    const response = await api.get<TrailModule[]>(`/trails/${trailId}/modules`);
    return response.data;
  },

  /**
   * Busca módulo por ID.
   */
  getModuleById: async (trailId: string, moduleId: string): Promise<TrailModule> => {
    const response = await api.get<TrailModule>(`/trails/${trailId}/modules/${moduleId}`);
    return response.data;
  },

  // ============================================
  // Lesson Endpoints
  // ============================================

  /**
   * Busca lição por ID.
   */
  getLessonById: async (lessonId: string): Promise<Lesson> => {
    const response = await api.get<Lesson>(`/trails/lessons/${lessonId}`);
    return response.data;
  },

  /**
   * Busca próxima lição não completada.
   * Retorna null apenas se 204 No Content (todas completadas).
   * Propaga outros erros para tratamento adequado.
   */
  getNextLesson: async (trailId: string): Promise<Lesson | null> => {
    try {
      const response = await api.get<Lesson>(`/trails/${trailId}/next-lesson`);
      return response.data;
    } catch (error) {
      // 204 No Content = todas as lições completadas
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } };
        if (axiosError.response?.status === 204) {
          return null;
        }
      }
      // Propaga outros erros (rede, 500, etc.)
      throw error;
    }
  },

  /**
   * Atualiza progresso de uma lição.
   */
  updateLessonProgress: async (
    lessonId: string,
    request: UpdateLessonProgressRequest
  ): Promise<Lesson> => {
    const response = await api.patch<Lesson>(`/trails/lessons/${lessonId}/progress`, request);
    return response.data;
  },

  /**
   * Marca lição como completa.
   */
  completeLesson: async (
    lessonId: string,
    score?: number,
    timeSpentSeconds?: number
  ): Promise<Lesson> => {
    return trailService.updateLessonProgress(lessonId, {
      completed: true,
      score,
      timeSpentSeconds,
    });
  },

  // ============================================
  // Progress Endpoints
  // ============================================

  /**
   * Busca progresso consolidado de uma trilha.
   */
  getProgress: async (trailId: string): Promise<TrailProgress> => {
    const response = await api.get<TrailProgress>(`/trails/${trailId}/progress`);
    return response.data;
  },
};

// ============================================
// SSE for Generation Status
// ============================================

/**
 * Inscreve-se no stream de status de geração de trilha.
 * Retorna função para cancelar a inscrição.
 */
export const subscribeToGenerationStatus = (
  trailId: string,
  onMessage: (status: unknown) => void,
  onError?: (error: Event) => void
): (() => void) => {
  const eventSource = new EventSource(`/api/trails/${trailId}/status/stream`, {
    withCredentials: true,
  });

  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      onMessage(data);
    } catch {
      logger.error('Error parsing SSE message:', event.data);
    }
  };

  eventSource.onerror = (error) => {
    if (onError) {
      onError(error);
    }
    eventSource.close();
  };

  // Retorna função de cleanup
  return () => {
    eventSource.close();
  };
};

export default trailService;
