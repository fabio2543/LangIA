import { useState, useEffect } from 'react';
import { useOnboarding } from '../../../context/OnboardingContext';
import { languageService } from '../../../services/profileService';
import { Button } from '../../common/Button';
import type { Language, LanguageEnrollment, CefrLevel } from '../../../types';

const CEFR_LEVELS: { value: CefrLevel; label: string; description: string }[] = [
  { value: 'A1', label: 'A1 - Iniciante', description: 'Expressoes basicas e frases simples' },
  { value: 'A2', label: 'A2 - Basico', description: 'Situacoes do dia a dia' },
  { value: 'B1', label: 'B1 - Intermediario', description: 'Conversas sobre assuntos familiares' },
  { value: 'B2', label: 'B2 - Intermediario Avancado', description: 'Discussoes tecnicas e abstratas' },
  { value: 'C1', label: 'C1 - Avancado', description: 'Comunicacao fluente e espontanea' },
  { value: 'C2', label: 'C2 - Proficiente', description: 'Dominio completo do idioma' },
];

export const LanguageStep = () => {
  const { nextStep, prevStep, isLoading: contextLoading } = useOnboarding();
  const [availableLanguages, setAvailableLanguages] = useState<Language[]>([]);
  const [enrolledLanguages, setEnrolledLanguages] = useState<LanguageEnrollment[]>([]);
  const [selectedLanguage, setSelectedLanguage] = useState<string>('');
  const [selectedLevel, setSelectedLevel] = useState<CefrLevel>('A1');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [languages, enrolled] = await Promise.all([
          languageService.getAvailableLanguages(),
          languageService.getEnrollments(),
        ]);
        setAvailableLanguages(languages);
        setEnrolledLanguages(enrolled);

        // Se ja tem idioma inscrito, nao pre-seleciona nada
        if (enrolled.length === 0 && languages.length > 0) {
          setSelectedLanguage(languages[0].code);
        }
      } catch (err) {
        console.error('Erro ao carregar idiomas:', err);
        setError('Erro ao carregar idiomas disponiveis');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  const handleEnrollLanguage = async () => {
    if (!selectedLanguage) {
      setError('Selecione um idioma');
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      await languageService.enroll({
        languageCode: selectedLanguage,
        cefrLevel: selectedLevel,
        isPrimary: enrolledLanguages.length === 0,
      });

      // Recarrega lista
      const enrolled = await languageService.getEnrollments();
      setEnrolledLanguages(enrolled);
      setSelectedLanguage('');
    } catch (err) {
      setError('Erro ao adicionar idioma. Tente novamente.');
      console.error('Erro ao adicionar idioma:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleRemoveLanguage = async (languageCode: string) => {
    try {
      setIsSaving(true);
      await languageService.unenroll(languageCode);
      const enrolled = await languageService.getEnrollments();
      setEnrolledLanguages(enrolled);
    } catch (err) {
      setError('Erro ao remover idioma');
      console.error('Erro ao remover idioma:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleContinue = () => {
    if (enrolledLanguages.length === 0) {
      setError('Adicione pelo menos um idioma para continuar');
      return;
    }
    nextStep();
  };

  const getLanguageName = (code: string) => {
    const lang = availableLanguages.find((l) => l.code === code);
    return lang?.namePt || code;
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="text-4xl mb-4 animate-bounce">...</div>
          <p className="text-text-light">Carregando idiomas...</p>
        </div>
      </div>
    );
  }

  const availableToEnroll = availableLanguages.filter(
    (lang) => !enrolledLanguages.some((e) => e.languageCode === lang.code)
  );

  return (
    <div className="max-w-xl mx-auto">
      <div className="text-center mb-8">
        <div className="text-6xl mb-4">🌍</div>
        <h1 className="text-3xl font-serif italic text-text mb-2">
          Qual idioma voce quer aprender?
        </h1>
        <p className="text-text-light">
          Escolha ate 3 idiomas para estudar. Voce pode adicionar mais depois.
        </p>
      </div>

      {/* Idiomas Inscritos */}
      {enrolledLanguages.length > 0 && (
        <div className="bg-white rounded-2xl shadow-card p-6 mb-6">
          <h2 className="text-lg font-semibold text-text mb-4">Seus idiomas</h2>
          <div className="space-y-3">
            {enrolledLanguages.map((enrollment) => (
              <div
                key={enrollment.id}
                className="flex items-center justify-between p-4 bg-bg rounded-xl"
              >
                <div>
                  <span className="font-medium text-text">
                    {enrollment.languageNamePt}
                  </span>
                  {enrollment.isPrimary && (
                    <span className="ml-2 text-xs bg-accent/20 text-accent px-2 py-1 rounded-full">
                      Principal
                    </span>
                  )}
                  {enrollment.cefrLevel && (
                    <span className="ml-2 text-xs text-text-light">
                      Nivel {enrollment.cefrLevel}
                    </span>
                  )}
                </div>
                <button
                  type="button"
                  onClick={() => handleRemoveLanguage(enrollment.languageCode)}
                  className="text-red-500 hover:text-red-700 p-2"
                  disabled={isSaving}
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Adicionar Idioma */}
      {enrolledLanguages.length < 3 && availableToEnroll.length > 0 && (
        <div className="bg-white rounded-2xl shadow-card p-6 mb-6">
          <h2 className="text-lg font-semibold text-text mb-4">
            {enrolledLanguages.length === 0 ? 'Escolha um idioma' : 'Adicionar outro idioma'}
          </h2>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm mb-4">
              {error}
            </div>
          )}

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-text mb-2">Idioma</label>
              <select
                value={selectedLanguage}
                onChange={(e) => setSelectedLanguage(e.target.value)}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all"
              >
                <option value="">Selecione um idioma</option>
                {availableToEnroll.map((lang) => (
                  <option key={lang.code} value={lang.code}>
                    {lang.namePt}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-text mb-2">
                Seu nivel atual (aproximado)
              </label>
              <select
                value={selectedLevel}
                onChange={(e) => setSelectedLevel(e.target.value as CefrLevel)}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all"
              >
                {CEFR_LEVELS.map((level) => (
                  <option key={level.value} value={level.value}>
                    {level.label}
                  </option>
                ))}
              </select>
              <p className="text-xs text-text-light mt-1">
                {CEFR_LEVELS.find((l) => l.value === selectedLevel)?.description}
              </p>
            </div>

            <Button
              type="button"
              variant="outline"
              onClick={handleEnrollLanguage}
              disabled={!selectedLanguage || isSaving}
              className="w-full"
            >
              {isSaving ? 'Adicionando...' : 'Adicionar idioma'}
            </Button>
          </div>
        </div>
      )}

      {/* Navegacao */}
      <div className="flex gap-4">
        <Button
          type="button"
          variant="outline"
          onClick={prevStep}
          className="flex-1"
          disabled={contextLoading}
        >
          Voltar
        </Button>
        <Button
          type="button"
          variant="primary"
          onClick={handleContinue}
          className="flex-1"
          disabled={enrolledLanguages.length === 0 || contextLoading}
        >
          Continuar
        </Button>
      </div>
    </div>
  );
};

export default LanguageStep;
