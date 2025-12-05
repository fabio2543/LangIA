import { useEffect, useState, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useTrail } from '../hooks/useTrail';
import { TrailCard, TrailGenerating } from '../components/trail';
import { Button } from '../components/common/Button';
import { languageService } from '../services/trailService';
import type { LanguageEnrollment } from '../types/trail';

/**
 * Página de listagem de trilhas ativas do estudante.
 */
export const TrailsPage = () => {
  const { user, isLoading: authLoading } = useAuth();
  const navigate = useNavigate();
  const {
    activeTrails,
    isLoading,
    error,
    generationStatus,
    loadActiveTrails,
    generateTrail,
    archiveTrail,
  } = useTrail();

  const [enrolledLanguages, setEnrolledLanguages] = useState<LanguageEnrollment[]>([]);
  const [selectedLanguage, setSelectedLanguage] = useState('');
  const [isGeneratingNew, setIsGeneratingNew] = useState(false);

  // Carrega idiomas inscritos do usuário (sem dependências que causem loop)
  const loadEnrolledLanguages = useCallback(async () => {
    try {
      const enrollments = await languageService.getEnrollments();
      setEnrolledLanguages(enrollments);
    } catch (err) {
      console.error('Erro ao carregar idiomas:', err);
    }
  }, []);

  // Define o idioma selecionado quando os dados carregam
  useEffect(() => {
    if (enrolledLanguages.length > 0 && !selectedLanguage) {
      const availableForTrail = enrolledLanguages.filter(
        (e) => !activeTrails.some((t) => t.languageCode === e.languageCode)
      );
      if (availableForTrail.length > 0) {
        setSelectedLanguage(availableForTrail[0].languageCode);
      }
    }
  }, [enrolledLanguages, activeTrails, selectedLanguage]);

  const handleRegenerate = async (_trailId: string, languageCode: string) => {
    await generateTrail({ languageCode, forceRegenerate: true });
  };

  const handleDelete = async (trailId: string) => {
    await archiveTrail(trailId);
  };

  const handleGenerateNew = async () => {
    setIsGeneratingNew(true);
    try {
      await generateTrail({ languageCode: selectedLanguage });
    } finally {
      setIsGeneratingNew(false);
    }
  };

  useEffect(() => {
    if (!user && !authLoading) {
      navigate('/login');
    }
  }, [user, authLoading, navigate]);

  useEffect(() => {
    if (user) {
      loadActiveTrails();
      loadEnrolledLanguages();
    }
  }, [user, loadActiveTrails, loadEnrolledLanguages]);

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

  const isGenerating = generationStatus !== null;

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
              <Link to="/dashboard">
                <Button variant="outline" size="sm">
                  Dashboard
                </Button>
              </Link>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Page Title + Create Button */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-serif italic text-text mb-2">
              Minhas Trilhas de Aprendizado
            </h1>
            <p className="text-text-light">
              Acompanhe seu progresso em cada idioma que você está aprendendo.
            </p>
          </div>

          {/* Create New Trail Button - shows when there are enrolled languages without trails */}
          {(() => {
            const availableLanguages = enrolledLanguages.filter(
              (lang) => !activeTrails.some((t) => t.languageCode === lang.languageCode)
            );
            return availableLanguages.length > 0 && activeTrails.length < 3 && (
              <div className="flex items-center gap-3">
                <select
                  value={selectedLanguage}
                  onChange={(e) => setSelectedLanguage(e.target.value)}
                  className="px-4 py-2 rounded-full border border-gray-200 bg-white text-text text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                >
                  {availableLanguages.map((lang) => (
                    <option key={lang.languageCode} value={lang.languageCode}>
                      {lang.languageNamePt}
                    </option>
                  ))}
                </select>
              <Button
                variant="primary"
                size="md"
                onClick={handleGenerateNew}
                disabled={isGeneratingNew || isGenerating}
              >
                {isGeneratingNew ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    Gerando...
                  </span>
                ) : (
                  '+ Nova Trilha'
                )}
              </Button>
            </div>
            );
          })()}
        </div>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-6 mb-8">
            <div className="flex items-center gap-3">
              <span className="text-2xl">⚠️</span>
              <div>
                <p className="font-medium text-red-800">Erro ao carregar trilhas</p>
                <p className="text-sm text-red-600">{error}</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => loadActiveTrails()}
              className="mt-4"
            >
              Tentar novamente
            </Button>
          </div>
        )}

        {/* Generating State */}
        {isGenerating && (
          <div className="mb-8">
            <TrailGenerating status={generationStatus} />
          </div>
        )}

        {/* Loading State */}
        {isLoading && !isGenerating && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="bg-white rounded-2xl shadow-card p-6 animate-pulse"
              >
                <div className="flex items-center gap-4 mb-4">
                  <div className="w-14 h-14 bg-gray-200 rounded-xl" />
                  <div className="flex-1">
                    <div className="h-5 bg-gray-200 rounded w-3/4 mb-2" />
                    <div className="h-4 bg-gray-100 rounded w-1/2" />
                  </div>
                </div>
                <div className="h-2 bg-gray-100 rounded-full mb-4" />
                <div className="h-4 bg-gray-100 rounded w-full" />
              </div>
            ))}
          </div>
        )}

        {/* Trails Grid */}
        {!isLoading && activeTrails.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {activeTrails.map((trail) => (
              <TrailCard
                key={trail.id}
                trail={trail}
                onClick={() => navigate(`/trails/${trail.id}`)}
                onRegenerate={handleRegenerate}
                onDelete={handleDelete}
              />
            ))}
          </div>
        )}

        {/* Empty State */}
        {!isLoading && !isGenerating && activeTrails.length === 0 && !error && (
          <div className="bg-white rounded-3xl shadow-card p-12 text-center">
            <div className="text-6xl mb-4">🎯</div>
            <h2 className="text-2xl font-serif italic text-text mb-2">
              Nenhuma trilha ativa
            </h2>
            <p className="text-text-light max-w-md mx-auto mb-6">
              {enrolledLanguages.length > 0
                ? 'Selecione um idioma e gere sua trilha de aprendizado personalizada com IA.'
                : 'Você ainda não está inscrito em nenhum idioma. Configure seus idiomas primeiro.'}
            </p>

            {enrolledLanguages.length > 0 ? (
              <>
                {/* Language Selector */}
                <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-6">
                  <select
                    value={selectedLanguage}
                    onChange={(e) => setSelectedLanguage(e.target.value)}
                    className="px-4 py-3 rounded-full border border-gray-200 bg-white text-text focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent min-w-[200px]"
                  >
                    {enrolledLanguages.map((lang) => (
                      <option key={lang.languageCode} value={lang.languageCode}>
                        {lang.languageNamePt}
                      </option>
                    ))}
                  </select>

                  <Button
                    variant="primary"
                    size="lg"
                    onClick={handleGenerateNew}
                    disabled={isGeneratingNew || !selectedLanguage}
                  >
                    {isGeneratingNew ? (
                      <span className="flex items-center gap-2">
                        <span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        Gerando...
                      </span>
                    ) : (
                      'Gerar Trilha'
                    )}
                  </Button>
                </div>

                <p className="text-sm text-text-light">
                  Ou{' '}
                  <Link to="/profile/learning" className="text-primary hover:underline">
                    configure suas preferências
                  </Link>{' '}
                  antes de gerar.
                </p>
              </>
            ) : (
              <Link to="/onboarding/language">
                <Button variant="primary" size="lg">
                  Escolher Idiomas
                </Button>
              </Link>
            )}
          </div>
        )}

        {/* Info Card */}
        {activeTrails.length > 0 && (
          <div className="mt-8 bg-gradient-to-br from-primary-light to-bg rounded-2xl p-6">
            <div className="flex items-start gap-4">
              <span className="text-3xl">💡</span>
              <div>
                <h3 className="font-semibold text-text mb-1">Dica</h3>
                <p className="text-sm text-text-light">
                  Você pode ter até 3 trilhas ativas simultaneamente. Complete lições regularmente
                  para manter seu progresso e desbloquear novos conteúdos.
                </p>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default TrailsPage;
