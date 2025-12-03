import { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { trailService } from '../services/trailService';
import { Button } from '../components/common/Button';
import type { Lesson } from '../types/trail';

/**
 * Página de conteúdo de uma lição.
 */
export const LessonPage = () => {
  const { lessonId } = useParams<{ lessonId: string }>();
  const { user, isLoading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCompleting, setIsCompleting] = useState(false);
  const [startTime] = useState(Date.now());

  useEffect(() => {
    if (!user && !authLoading) {
      navigate('/login');
    }
  }, [user, authLoading, navigate]);

  useEffect(() => {
    if (!lessonId || !user) return;

    const fetchLesson = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await trailService.getLessonById(lessonId);
        setLesson(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar lição');
      } finally {
        setIsLoading(false);
      }
    };

    fetchLesson();
  }, [lessonId, user]);

  const handleComplete = useCallback(async () => {
    if (!lessonId || !lesson) return;

    setIsCompleting(true);
    try {
      const timeSpentSeconds = Math.floor((Date.now() - startTime) / 1000);
      await trailService.updateLessonProgress(lessonId, {
        completed: true,
        score: 85, // Mock score - in production this would be calculated
        timeSpentSeconds,
      });
      setLesson((prev) =>
        prev
          ? {
              ...prev,
              completedAt: new Date().toISOString(),
              score: 85,
              timeSpentSeconds,
            }
          : null
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao completar lição');
    } finally {
      setIsCompleting(false);
    }
  }, [lessonId, lesson, startTime]);

  if (authLoading || !user) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center">
          <div className="text-4xl mb-4">⏳</div>
          <p className="text-text-light">Carregando...</p>
        </div>
      </div>
    );
  }

  const lessonTypeConfig: Record<
    Lesson['type'],
    { icon: string; label: string; color: string }
  > = {
    interactive: { icon: '🎮', label: 'Interativo', color: 'bg-purple-100 text-purple-600' },
    video: { icon: '🎬', label: 'Vídeo', color: 'bg-red-100 text-red-600' },
    reading: { icon: '📖', label: 'Leitura', color: 'bg-blue-100 text-blue-600' },
    exercise: { icon: '✏️', label: 'Exercício', color: 'bg-yellow-100 text-yellow-600' },
    conversation: { icon: '💬', label: 'Conversação', color: 'bg-green-100 text-green-600' },
    flashcard: { icon: '🃏', label: 'Flashcard', color: 'bg-orange-100 text-orange-600' },
    game: { icon: '🎯', label: 'Jogo', color: 'bg-pink-100 text-pink-600' },
  };

  const typeConfig = lesson ? lessonTypeConfig[lesson.type] : null;
  const isCompleted = !!lesson?.completedAt;

  return (
    <div className="min-h-screen bg-bg-warm">
      {/* Header */}
      <header className="bg-text shadow-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <Link to="/dashboard" className="flex items-center gap-1 text-2xl font-bold text-white">
              Lang<span className="text-accent">IA</span>
            </Link>
            <nav className="flex items-center gap-4">
              <Link to="/trails">
                <Button variant="outline" size="sm">
                  Minhas Trilhas
                </Button>
              </Link>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Back Button */}
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-text-light hover:text-text mb-6 transition-colors"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Voltar
        </button>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-6 mb-8">
            <div className="flex items-center gap-3">
              <span className="text-2xl">⚠️</span>
              <div>
                <p className="font-medium text-red-800">Erro</p>
                <p className="text-sm text-red-600">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* Loading State */}
        {isLoading && (
          <div className="animate-pulse">
            <div className="bg-white rounded-3xl shadow-card p-8 mb-8">
              <div className="h-8 bg-gray-200 rounded w-2/3 mb-4" />
              <div className="h-4 bg-gray-100 rounded w-1/4 mb-8" />
              <div className="space-y-4">
                <div className="h-4 bg-gray-100 rounded w-full" />
                <div className="h-4 bg-gray-100 rounded w-full" />
                <div className="h-4 bg-gray-100 rounded w-3/4" />
              </div>
            </div>
          </div>
        )}

        {/* Lesson Content */}
        {!isLoading && lesson && (
          <>
            {/* Lesson Header */}
            <div className="bg-white rounded-3xl shadow-card p-8 mb-6">
              <div className="flex items-start justify-between mb-6">
                <div>
                  <h1 className="text-2xl font-serif italic text-text mb-2">
                    {lesson.title}
                  </h1>
                  <div className="flex items-center gap-3">
                    {typeConfig && (
                      <span className={`px-3 py-1 rounded-full text-sm font-medium ${typeConfig.color}`}>
                        {typeConfig.icon} {typeConfig.label}
                      </span>
                    )}
                    <span className="text-sm text-text-light">
                      {lesson.durationMinutes} min
                    </span>
                  </div>
                </div>
                {isCompleted && (
                  <div className="flex items-center gap-2 bg-green-100 text-green-600 px-4 py-2 rounded-full">
                    <span>✓</span>
                    <span className="font-medium">Concluída</span>
                  </div>
                )}
              </div>

              {/* Completed Stats */}
              {isCompleted && lesson.score !== null && (
                <div className="bg-gray-50 rounded-xl p-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm text-text-light">Sua pontuação</p>
                    <p className="text-2xl font-bold text-primary">{lesson.score.toFixed(0)}</p>
                  </div>
                  {lesson.timeSpentSeconds && (
                    <div className="text-right">
                      <p className="text-sm text-text-light">Tempo</p>
                      <p className="text-lg font-medium text-text">
                        {formatTimeSpent(lesson.timeSpentSeconds)}
                      </p>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Lesson Body */}
            <div className="bg-white rounded-2xl shadow-card p-8 mb-6">
              {(lesson.content as { introduction?: string })?.introduction && (
                <p className="text-lg text-text mb-6">
                  {(lesson.content as { introduction: string }).introduction}
                </p>
              )}

              {Array.isArray((lesson.content as { sections?: unknown[] })?.sections) ? (
                ((lesson.content as { sections: Array<{ title: string; content: string }> }).sections).map(
                  (section, index) => (
                    <div key={index} className="mb-8 last:mb-0">
                      <h2 className="text-lg font-semibold text-text mb-3">{section.title}</h2>
                      <p className="text-text-light">{section.content}</p>
                    </div>
                  )
                )
              ) : (
                <div className="text-center py-8">
                  <div className="text-5xl mb-4">{typeConfig?.icon || '📚'}</div>
                  <p className="text-text-light">
                    O conteúdo desta lição será carregado aqui.
                  </p>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="flex justify-center gap-4">
              {!isCompleted ? (
                <Button
                  variant="primary"
                  size="lg"
                  onClick={handleComplete}
                  disabled={isCompleting}
                >
                  {isCompleting ? 'Salvando...' : 'Marcar como Concluída'}
                </Button>
              ) : (
                <Button
                  variant="secondary"
                  size="lg"
                  onClick={() => navigate(-1)}
                >
                  Voltar ao Módulo
                </Button>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
};

/**
 * Formata tempo em segundos para exibição.
 */
const formatTimeSpent = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60);
  if (minutes < 1) return '<1min';
  if (minutes < 60) return `${minutes}min`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}h ${mins}min`;
};

export default LessonPage;
