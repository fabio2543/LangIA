import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { trailService, subscribeToGenerationStatus } from '../services/trailService';
import { TrailProgress, ModuleCard, TrailGenerating } from '../components/trail';
import { Button } from '../components/common/Button';
import type { Trail, TrailGenerationStatus } from '../types/trail';

/**
 * Página de detalhes de uma trilha específica.
 */
export const TrailDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const { user, isLoading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [trail, setTrail] = useState<Trail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [generationStatus, setGenerationStatus] = useState<TrailGenerationStatus | null>(null);

  useEffect(() => {
    if (!user && !authLoading) {
      navigate('/login');
    }
  }, [user, authLoading, navigate]);

  useEffect(() => {
    if (!id || !user) return;

    const fetchTrail = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await trailService.getTrailById(id);
        setTrail(data);

        // If trail is generating, subscribe to status updates
        if (data.status === 'GENERATING' || data.status === 'PARTIAL') {
          const unsubscribe = subscribeToGenerationStatus(id, (status) => {
            setGenerationStatus(status as TrailGenerationStatus);
            if ((status as TrailGenerationStatus).progressPercentage === 100) {
              // Refetch trail when generation completes
              trailService.getTrailById(id).then(setTrail);
              setGenerationStatus(null);
            }
          });
          return () => unsubscribe();
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar trilha');
      } finally {
        setIsLoading(false);
      }
    };

    fetchTrail();
  }, [id, user]);

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

  const isGenerating = trail?.status === 'GENERATING' || trail?.status === 'PARTIAL';

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
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Back Button */}
        <button
          onClick={() => navigate('/trails')}
          className="flex items-center gap-2 text-text-light hover:text-text mb-6 transition-colors"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Voltar para Trilhas
        </button>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-6 mb-8">
            <div className="flex items-center gap-3">
              <span className="text-2xl">⚠️</span>
              <div>
                <p className="font-medium text-red-800">Erro ao carregar trilha</p>
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
              <div className="flex items-center gap-6 mb-6">
                <div className="w-20 h-20 bg-gray-200 rounded-2xl" />
                <div className="flex-1">
                  <div className="h-8 bg-gray-200 rounded w-1/3 mb-2" />
                  <div className="h-4 bg-gray-100 rounded w-1/4" />
                </div>
              </div>
              <div className="h-3 bg-gray-100 rounded-full" />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="bg-white rounded-2xl shadow-card p-6">
                  <div className="h-6 bg-gray-200 rounded w-3/4 mb-4" />
                  <div className="h-4 bg-gray-100 rounded w-full mb-2" />
                  <div className="h-2 bg-gray-100 rounded-full" />
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Trail Content */}
        {!isLoading && trail && (
          <>
            {/* Trail Header */}
            <div className="bg-white rounded-3xl shadow-card p-8 mb-8">
              <div className="flex flex-col md:flex-row md:items-center gap-6 mb-6">
                <div className="w-20 h-20 bg-primary-light rounded-2xl flex items-center justify-center text-4xl">
                  {trail.languageFlag || '🌍'}
                </div>
                <div className="flex-1">
                  <h1 className="text-3xl font-serif italic text-text mb-1">
                    {trail.languageName}
                  </h1>
                  <div className="flex items-center gap-3 text-text-light">
                    <span className="px-3 py-1 bg-primary-light text-primary rounded-full text-sm font-medium">
                      {trail.levelCode}
                    </span>
                    <span>{trail.levelName}</span>
                  </div>
                </div>
                {!isGenerating && trail.progress && (
                  <div className="text-right">
                    <p className="text-3xl font-bold text-primary">
                      {trail.progress.progressPercentage.toFixed(0)}%
                    </p>
                    <p className="text-sm text-text-light">Concluído</p>
                  </div>
                )}
              </div>

              {/* Progress */}
              {!isGenerating && trail.progress && <TrailProgress progress={trail.progress} />}
            </div>

            {/* Generating State */}
            {isGenerating && (
              <TrailGenerating status={generationStatus} className="mb-8" />
            )}

            {/* Modules */}
            {!isGenerating && trail.modules.length > 0 && (
              <div>
                <h2 className="text-xl font-semibold text-text mb-6">
                  Módulos ({trail.modules.length})
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {trail.modules.map((module) => (
                    <ModuleCard
                      key={module.id}
                      module={module}
                      onClick={() => navigate(`/trails/${trail.id}/modules/${module.id}`)}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* No Modules Yet */}
            {!isGenerating && trail.modules.length === 0 && (
              <div className="bg-white rounded-2xl shadow-card p-8 text-center">
                <div className="text-4xl mb-4">📚</div>
                <p className="text-text-light">
                  Os módulos desta trilha ainda estão sendo preparados.
                </p>
              </div>
            )}

            {/* Trail Stats */}
            {!isGenerating && (
              <div className="mt-8 grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="bg-white rounded-xl p-4 text-center">
                  <p className="text-2xl font-bold text-text">{trail.modules.length}</p>
                  <p className="text-sm text-text-light">Módulos</p>
                </div>
                <div className="bg-white rounded-xl p-4 text-center">
                  <p className="text-2xl font-bold text-text">{trail.progress?.totalLessons ?? 0}</p>
                  <p className="text-sm text-text-light">Lições</p>
                </div>
                <div className="bg-white rounded-xl p-4 text-center">
                  <p className="text-2xl font-bold text-text">{trail.progress?.lessonsCompleted ?? 0}</p>
                  <p className="text-sm text-text-light">Completadas</p>
                </div>
                <div className="bg-white rounded-xl p-4 text-center">
                  <p className="text-2xl font-bold text-text">
                    {trail.estimatedDurationHours ? `${trail.estimatedDurationHours}h` : '-'}
                  </p>
                  <p className="text-sm text-text-light">Duração Est.</p>
                </div>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default TrailDetailPage;
