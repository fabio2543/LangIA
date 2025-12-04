import {
  createContext,
  useState,
  useCallback,
  useMemo,
  useRef,
  useEffect,
  type ReactNode,
} from 'react';
import { trailService, subscribeToGenerationStatus } from '../services/trailService';
import type {
  Trail,
  TrailSummary,
  TrailModule,
  Lesson,
  TrailProgress,
  TrailGenerationStatus,
  GenerateTrailRequest,
  RefreshTrailRequest,
  UpdateLessonProgressRequest,
} from '../types/trail';

// ============================================
// Context Types
// ============================================

interface TrailContextState {
  // Estado
  activeTrails: TrailSummary[];
  currentTrail: Trail | null;
  currentModule: TrailModule | null;
  currentLesson: Lesson | null;
  generationStatus: TrailGenerationStatus | null;
  isLoading: boolean;
  isGenerating: boolean;
  error: string | null;
}

interface TrailContextActions {
  // Ações de trilha
  loadActiveTrails: () => Promise<void>;
  loadTrailByLanguage: (languageCode: string) => Promise<Trail>;
  loadTrailById: (trailId: string) => Promise<Trail>;
  generateTrail: (request: GenerateTrailRequest) => Promise<Trail>;
  refreshTrail: (trailId: string, request: RefreshTrailRequest) => Promise<Trail>;
  archiveTrail: (trailId: string) => Promise<void>;

  // Ações de módulo
  loadModule: (trailId: string, moduleId: string) => Promise<TrailModule>;

  // Ações de lição
  loadLesson: (lessonId: string) => Promise<Lesson>;
  loadNextLesson: (trailId: string) => Promise<Lesson | null>;
  updateLessonProgress: (
    lessonId: string,
    request: UpdateLessonProgressRequest
  ) => Promise<Lesson>;
  completeLesson: (
    lessonId: string,
    score?: number,
    timeSpentSeconds?: number
  ) => Promise<Lesson>;

  // Ações de progresso
  loadProgress: (trailId: string) => Promise<TrailProgress>;

  // Utilitários
  clearError: () => void;
  setCurrentTrail: (trail: Trail | null) => void;
  setCurrentModule: (module: TrailModule | null) => void;
  setCurrentLesson: (lesson: Lesson | null) => void;
}

export type TrailContextValue = TrailContextState & TrailContextActions;

// ============================================
// Context Creation
// ============================================

const TrailContext = createContext<TrailContextValue | null>(null);

// ============================================
// Provider Component
// ============================================

interface TrailProviderProps {
  children: ReactNode;
}

export const TrailProvider = ({ children }: TrailProviderProps) => {
  // Estado
  const [activeTrails, setActiveTrails] = useState<TrailSummary[]>([]);
  const [currentTrail, setCurrentTrail] = useState<Trail | null>(null);
  const [currentModule, setCurrentModule] = useState<TrailModule | null>(null);
  const [currentLesson, setCurrentLesson] = useState<Lesson | null>(null);
  const [generationStatus, setGenerationStatus] = useState<TrailGenerationStatus | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Ref para armazenar função de cleanup da assinatura SSE
  const unsubscribeRef = useRef<(() => void) | null>(null);

  // Cleanup SSE ao desmontar ou trocar de trilha
  const cleanupSubscription = useCallback(() => {
    if (unsubscribeRef.current) {
      unsubscribeRef.current();
      unsubscribeRef.current = null;
    }
  }, []);

  // Cleanup ao desmontar o provider
  useEffect(() => {
    return () => {
      cleanupSubscription();
    };
  }, [cleanupSubscription]);

  // Utilitários
  const clearError = useCallback(() => setError(null), []);

  // Carregar trilhas ativas
  const loadActiveTrails = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const trails = await trailService.getActiveTrails();
      setActiveTrails(trails);
    } catch (err) {
      setError('Erro ao carregar trilhas');
      console.error('Error loading active trails:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Carregar trilha por ID (definido antes de loadTrailByLanguage para ser usado como dependência)
  const loadTrailById = useCallback(async (trailId: string): Promise<Trail> => {
    setIsLoading(true);
    setError(null);
    try {
      const trail = await trailService.getTrailById(trailId);
      setCurrentTrail(trail);
      return trail;
    } catch (err) {
      setError('Erro ao carregar trilha');
      console.error('Error loading trail by id:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Carregar trilha por idioma
  const loadTrailByLanguage = useCallback(async (languageCode: string): Promise<Trail> => {
    setIsLoading(true);
    setError(null);

    // Limpa assinatura anterior antes de iniciar nova
    cleanupSubscription();

    try {
      const trail = await trailService.getTrailByLanguage(languageCode);
      setCurrentTrail(trail);

      // Se a trilha está em geração, iniciar monitoramento
      if (trail.status === 'GENERATING' || trail.status === 'PARTIAL') {
        setIsGenerating(true);
        unsubscribeRef.current = subscribeToGenerationStatus(
          trail.id,
          (status) => {
            setGenerationStatus(status as TrailGenerationStatus);
            if ((status as TrailGenerationStatus).trailStatus === 'READY') {
              setIsGenerating(false);
              cleanupSubscription();
              // Recarregar trilha completa
              loadTrailById(trail.id);
            }
          },
          () => {
            setIsGenerating(false);
            cleanupSubscription();
          }
        );
      }

      return trail;
    } catch (err) {
      setError('Erro ao carregar trilha');
      console.error('Error loading trail by language:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [cleanupSubscription, loadTrailById]);

  // Gerar trilha
  const generateTrail = useCallback(async (request: GenerateTrailRequest): Promise<Trail> => {
    setIsLoading(true);
    setIsGenerating(true);
    setError(null);

    // Limpa assinatura anterior antes de iniciar nova
    cleanupSubscription();

    try {
      const trail = await trailService.generateTrail(request);
      setCurrentTrail(trail);

      // Monitorar geração
      if (trail.status === 'GENERATING' || trail.status === 'PARTIAL') {
        unsubscribeRef.current = subscribeToGenerationStatus(
          trail.id,
          (status) => {
            setGenerationStatus(status as TrailGenerationStatus);
            if ((status as TrailGenerationStatus).trailStatus === 'READY') {
              setIsGenerating(false);
              cleanupSubscription();
              loadTrailById(trail.id);
            }
          },
          () => {
            setIsGenerating(false);
            cleanupSubscription();
          }
        );
      } else {
        setIsGenerating(false);
      }

      // Recarregar lista de trilhas
      loadActiveTrails();

      return trail;
    } catch (err) {
      setIsGenerating(false);
      setError('Erro ao gerar trilha');
      console.error('Error generating trail:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [loadActiveTrails, loadTrailById, cleanupSubscription]);

  // Refresh trilha
  const refreshTrail = useCallback(async (
    trailId: string,
    request: RefreshTrailRequest
  ): Promise<Trail> => {
    setIsLoading(true);
    setIsGenerating(true);
    setError(null);
    try {
      const trail = await trailService.refreshTrail(trailId, request);
      setCurrentTrail(trail);
      loadActiveTrails();
      return trail;
    } catch (err) {
      setError('Erro ao regenerar trilha');
      console.error('Error refreshing trail:', err);
      throw err;
    } finally {
      setIsLoading(false);
      setIsGenerating(false);
    }
  }, [loadActiveTrails]);

  // Arquivar trilha
  const archiveTrail = useCallback(async (trailId: string): Promise<void> => {
    setIsLoading(true);
    setError(null);
    try {
      await trailService.archiveTrail(trailId);
      if (currentTrail?.id === trailId) {
        setCurrentTrail(null);
      }
      loadActiveTrails();
    } catch (err) {
      setError('Erro ao arquivar trilha');
      console.error('Error archiving trail:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [currentTrail, loadActiveTrails]);

  // Carregar módulo
  const loadModule = useCallback(async (
    trailId: string,
    moduleId: string
  ): Promise<TrailModule> => {
    setIsLoading(true);
    setError(null);
    try {
      const module = await trailService.getModuleById(trailId, moduleId);
      setCurrentModule(module);
      return module;
    } catch (err) {
      setError('Erro ao carregar módulo');
      console.error('Error loading module:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Carregar lição
  const loadLesson = useCallback(async (lessonId: string): Promise<Lesson> => {
    setIsLoading(true);
    setError(null);
    try {
      const lesson = await trailService.getLessonById(lessonId);
      setCurrentLesson(lesson);
      return lesson;
    } catch (err) {
      setError('Erro ao carregar lição');
      console.error('Error loading lesson:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Carregar próxima lição
  const loadNextLesson = useCallback(async (trailId: string): Promise<Lesson | null> => {
    setIsLoading(true);
    setError(null);
    try {
      const lesson = await trailService.getNextLesson(trailId);
      if (lesson) {
        setCurrentLesson(lesson);
      }
      return lesson;
    } catch (err) {
      setError('Erro ao carregar próxima lição');
      console.error('Error loading next lesson:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Atualizar progresso da lição
  const updateLessonProgress = useCallback(async (
    lessonId: string,
    request: UpdateLessonProgressRequest
  ): Promise<Lesson> => {
    setError(null);
    try {
      const lesson = await trailService.updateLessonProgress(lessonId, request);
      setCurrentLesson(lesson);

      // Atualizar trilha atual se necessário
      if (currentTrail) {
        loadTrailById(currentTrail.id);
      }

      return lesson;
    } catch (err) {
      setError('Erro ao atualizar progresso');
      console.error('Error updating lesson progress:', err);
      throw err;
    }
  }, [currentTrail, loadTrailById]);

  // Completar lição
  const completeLesson = useCallback(async (
    lessonId: string,
    score?: number,
    timeSpentSeconds?: number
  ): Promise<Lesson> => {
    return updateLessonProgress(lessonId, {
      completed: true,
      score,
      timeSpentSeconds,
    });
  }, [updateLessonProgress]);

  // Carregar progresso
  const loadProgress = useCallback(async (trailId: string): Promise<TrailProgress> => {
    setError(null);
    try {
      return await trailService.getProgress(trailId);
    } catch (err) {
      setError('Erro ao carregar progresso');
      console.error('Error loading progress:', err);
      throw err;
    }
  }, []);

  // Valor do contexto
  const value = useMemo<TrailContextValue>(() => ({
    // Estado
    activeTrails,
    currentTrail,
    currentModule,
    currentLesson,
    generationStatus,
    isLoading,
    isGenerating,
    error,

    // Ações
    loadActiveTrails,
    loadTrailByLanguage,
    loadTrailById,
    generateTrail,
    refreshTrail,
    archiveTrail,
    loadModule,
    loadLesson,
    loadNextLesson,
    updateLessonProgress,
    completeLesson,
    loadProgress,
    clearError,
    setCurrentTrail,
    setCurrentModule,
    setCurrentLesson,
  }), [
    activeTrails,
    currentTrail,
    currentModule,
    currentLesson,
    generationStatus,
    isLoading,
    isGenerating,
    error,
    loadActiveTrails,
    loadTrailByLanguage,
    loadTrailById,
    generateTrail,
    refreshTrail,
    archiveTrail,
    loadModule,
    loadLesson,
    loadNextLesson,
    updateLessonProgress,
    completeLesson,
    loadProgress,
    clearError,
  ]);

  return (
    <TrailContext.Provider value={value}>
      {children}
    </TrailContext.Provider>
  );
};

export default TrailContext;
