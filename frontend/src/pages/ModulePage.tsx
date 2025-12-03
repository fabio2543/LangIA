import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { trailService } from '../services/trailService';
import { LessonCard } from '../components/trail';
import { Button } from '../components/common/Button';
import type { TrailModule } from '../types/trail';

/**
 * Página de detalhes de um módulo com suas lições.
 */
export const ModulePage = () => {
  const { id: trailId, moduleId } = useParams<{ id: string; moduleId: string }>();
  const { user, isLoading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [module, setModule] = useState<TrailModule | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user && !authLoading) {
      navigate('/login');
    }
  }, [user, authLoading, navigate]);

  useEffect(() => {
    if (!trailId || !moduleId || !user) return;

    const fetchModule = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await trailService.getModuleById(trailId, moduleId);
        setModule(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar módulo');
      } finally {
        setIsLoading(false);
      }
    };

    fetchModule();
  }, [trailId, moduleId, user]);

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

  const competencyIcons: Record<string, string> = {
    speaking: '🗣️',
    listening: '👂',
    reading: '📖',
    writing: '✍️',
    grammar: '📝',
    vocabulary: '📚',
    pronunciation: '🎤',
  };

  const completedLessons = module?.lessons.filter((l) => l.completedAt).length ?? 0;
  const totalLessons = module?.lessons.length ?? 0;
  const progressPercentage = totalLessons > 0 ? (completedLessons / totalLessons) * 100 : 0;

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
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Back Button */}
        <button
          onClick={() => navigate(`/trails/${trailId}`)}
          className="flex items-center gap-2 text-text-light hover:text-text mb-6 transition-colors"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Voltar para Trilha
        </button>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-6 mb-8">
            <div className="flex items-center gap-3">
              <span className="text-2xl">⚠️</span>
              <div>
                <p className="font-medium text-red-800">Erro ao carregar módulo</p>
                <p className="text-sm text-red-600">{error}</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => window.location.reload()}
              className="mt-4"
            >
              Tentar novamente
            </Button>
          </div>
        )}

        {/* Loading State */}
        {isLoading && (
          <div className="animate-pulse">
            <div className="bg-white rounded-3xl shadow-card p-8 mb-8">
              <div className="flex items-center gap-4 mb-4">
                <div className="w-16 h-16 bg-gray-200 rounded-xl" />
                <div className="flex-1">
                  <div className="h-6 bg-gray-200 rounded w-1/2 mb-2" />
                  <div className="h-4 bg-gray-100 rounded w-1/3" />
                </div>
              </div>
              <div className="h-2 bg-gray-100 rounded-full" />
            </div>
            <div className="space-y-4">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="bg-white rounded-xl p-4">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 bg-gray-200 rounded-lg" />
                    <div className="flex-1">
                      <div className="h-5 bg-gray-200 rounded w-3/4 mb-2" />
                      <div className="h-4 bg-gray-100 rounded w-1/4" />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Module Content */}
        {!isLoading && module && (
          <>
            {/* Module Header */}
            <div className="bg-white rounded-3xl shadow-card p-8 mb-8">
              <div className="flex items-start gap-4 mb-6">
                <div className="w-16 h-16 bg-primary-light rounded-xl flex items-center justify-center text-3xl">
                  {competencyIcons[module.competencyCode] ?? '📘'}
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-1">
                    <h1 className="text-2xl font-serif italic text-text">
                      {module.title}
                    </h1>
                    <span className="text-sm text-text-light">
                      Módulo {module.orderIndex + 1}
                    </span>
                  </div>
                  <p className="text-text-light">{module.competencyName}</p>
                </div>
              </div>

              {module.description && (
                <p className="text-text-light mb-6">{module.description}</p>
              )}

              {/* Progress Bar */}
              <div>
                <div className="flex justify-between text-sm mb-2">
                  <span className="text-text-light">
                    {completedLessons} de {totalLessons} lições completadas
                  </span>
                  <span className="font-medium text-primary">
                    {progressPercentage.toFixed(0)}%
                  </span>
                </div>
                <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-primary rounded-full transition-all duration-300"
                    style={{ width: `${progressPercentage}%` }}
                  />
                </div>
              </div>
            </div>

            {/* Lessons List */}
            <div>
              <h2 className="text-lg font-semibold text-text mb-4">
                Lições
              </h2>
              <div className="space-y-3">
                {module.lessons.map((lesson, index) => (
                  <div key={lesson.id} className="flex items-center gap-3">
                    <span className="w-8 h-8 flex items-center justify-center text-sm font-medium text-text-light bg-white rounded-full shadow-sm">
                      {index + 1}
                    </span>
                    <div className="flex-1">
                      <LessonCard
                        lesson={lesson}
                        onClick={() => navigate(`/lessons/${lesson.id}`)}
                        showDetails={!!lesson.completedAt}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Continue Button */}
            {module.lessons.length > 0 && (
              <div className="mt-8 text-center">
                {(() => {
                  const nextLesson = module.lessons.find((l) => !l.completedAt && !l.isPlaceholder);
                  if (nextLesson) {
                    return (
                      <Button
                        variant="primary"
                        size="lg"
                        onClick={() => navigate(`/lessons/${nextLesson.id}`)}
                      >
                        Continuar Aprendendo
                      </Button>
                    );
                  }
                  if (completedLessons === totalLessons) {
                    return (
                      <div className="bg-green-50 rounded-2xl p-6">
                        <div className="text-4xl mb-2">🎉</div>
                        <p className="font-semibold text-green-800">Módulo Concluído!</p>
                        <p className="text-sm text-green-600">
                          Parabéns! Você completou todas as lições deste módulo.
                        </p>
                      </div>
                    );
                  }
                  return null;
                })()}
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default ModulePage;
