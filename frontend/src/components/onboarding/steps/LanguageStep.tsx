import { useState, useEffect } from 'react';
import { useOnboarding } from '../../../hooks/useOnboarding';
import { languageService } from '../../../services/profileService';
import { Button } from '../../common/Button';
import { logger } from '../../../utils/logger';
import type { Language, CefrLevel } from '../../../types';

// Idiomas disponíveis por enquanto (Inglês e Espanhol)
const ALLOWED_LANGUAGES = ['en', 'es'];

const LANGUAGE_FLAGS: Record<string, string> = {
  en: '🇺🇸',
  es: '🇪🇸',
};

const CEFR_LEVELS: { value: CefrLevel; label: string }[] = [
  { value: 'A1', label: 'A1 - Iniciante' },
  { value: 'A2', label: 'A2 - Básico' },
  { value: 'B1', label: 'B1 - Intermediário' },
  { value: 'B2', label: 'B2 - Intermediário Avançado' },
  { value: 'C1', label: 'C1 - Avançado' },
  { value: 'C2', label: 'C2 - Proficiente' },
];

interface SelectedLanguage {
  code: string;
  level: CefrLevel;
}

export const LanguageStep = () => {
  const { nextStep, prevStep, isLoading: contextLoading } = useOnboarding();
  const [availableLanguages, setAvailableLanguages] = useState<Language[]>([]);
  const [selectedLanguages, setSelectedLanguages] = useState<SelectedLanguage[]>([]);
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

        // Se já tem idiomas inscritos, carrega eles
        if (enrolled.length > 0) {
          setSelectedLanguages(
            enrolled.map((e) => ({
              code: e.languageCode,
              level: (e.cefrLevel as CefrLevel) || 'A1',
            }))
          );
        }
      } catch (err) {
        logger.error('Erro ao carregar idiomas:', err);
        setError('Erro ao carregar idiomas disponíveis');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  const toggleLanguage = (code: string) => {
    const isSelected = selectedLanguages.some((l) => l.code === code);

    if (isSelected) {
      // Remove
      setSelectedLanguages(selectedLanguages.filter((l) => l.code !== code));
    } else {
      // Adiciona (máximo 2)
      if (selectedLanguages.length < 2) {
        setSelectedLanguages([...selectedLanguages, { code, level: 'A1' }]);
      }
    }
  };

  const updateLevel = (code: string, level: CefrLevel) => {
    setSelectedLanguages(
      selectedLanguages.map((l) => (l.code === code ? { ...l, level } : l))
    );
  };

  const handleContinue = async () => {
    if (selectedLanguages.length === 0) {
      setError('Selecione pelo menos um idioma para continuar');
      return;
    }

    try {
      setIsSaving(true);
      setError(null);

      // Primeiro, remove idiomas que não estão mais selecionados
      const currentEnrollments = await languageService.getEnrollments();
      for (const enrollment of currentEnrollments) {
        if (!selectedLanguages.some((s) => s.code === enrollment.languageCode)) {
          await languageService.unenroll(enrollment.languageCode);
        }
      }

      // Depois, adiciona/atualiza os selecionados
      for (let i = 0; i < selectedLanguages.length; i++) {
        const lang = selectedLanguages[i];
        const existing = currentEnrollments.find((e) => e.languageCode === lang.code);

        if (!existing) {
          await languageService.enroll({
            languageCode: lang.code,
            cefrLevel: lang.level,
            isPrimary: i === 0, // Primeiro é o principal
          });
        }
      }

      nextStep();
    } catch (err) {
      setError('Erro ao salvar idiomas. Tente novamente.');
      logger.error('Erro ao salvar:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const allowedLanguages = availableLanguages.filter((l) =>
    ALLOWED_LANGUAGES.includes(l.code)
  );

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

  return (
    <div className="max-w-xl mx-auto">
      <div className="text-center mb-8">
        <div className="text-6xl mb-4">🌍</div>
        <h1 className="text-3xl font-serif italic text-text mb-2">
          Qual idioma você quer aprender?
        </h1>
        <p className="text-text-light">
          Selecione até 2 idiomas para estudar.
        </p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm mb-6">
          {error}
        </div>
      )}

      <div className="space-y-4 mb-6">
        {allowedLanguages.map((lang) => {
          const isSelected = selectedLanguages.some((l) => l.code === lang.code);
          const selectedLang = selectedLanguages.find((l) => l.code === lang.code);

          return (
            <div
              key={lang.code}
              className={`
                bg-white rounded-2xl shadow-card overflow-hidden transition-all
                ${isSelected ? 'ring-2 ring-accent' : ''}
              `}
            >
              <button
                type="button"
                onClick={() => toggleLanguage(lang.code)}
                className={`
                  w-full p-5 flex items-center gap-4 transition-all
                  ${isSelected ? 'bg-accent/5' : 'hover:bg-gray-50'}
                `}
              >
                <span className="text-4xl">{LANGUAGE_FLAGS[lang.code] || '🌐'}</span>
                <div className="flex-1 text-left">
                  <span className="text-lg font-semibold text-text">{lang.namePt}</span>
                  {isSelected && selectedLanguages[0]?.code === lang.code && (
                    <span className="ml-2 text-xs bg-accent/20 text-accent px-2 py-1 rounded-full">
                      Principal
                    </span>
                  )}
                </div>
                <div
                  className={`
                    w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all
                    ${isSelected ? 'border-accent bg-accent text-white' : 'border-gray-300'}
                  `}
                >
                  {isSelected && (
                    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fillRule="evenodd"
                        d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                        clipRule="evenodd"
                      />
                    </svg>
                  )}
                </div>
              </button>

              {/* Seletor de nível - aparece quando selecionado */}
              {isSelected && (
                <div className="px-5 pb-5 pt-2 border-t border-gray-100">
                  <label className="block text-sm font-medium text-text mb-2">
                    Seu nível atual
                  </label>
                  <select
                    value={selectedLang?.level || 'A1'}
                    onChange={(e) => updateLevel(lang.code, e.target.value as CefrLevel)}
                    className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:border-accent focus:ring-2 focus:ring-accent/20 transition-all text-sm"
                  >
                    {CEFR_LEVELS.map((level) => (
                      <option key={level.value} value={level.value}>
                        {level.label}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {selectedLanguages.length === 2 && (
        <p className="text-sm text-text-light text-center mb-4">
          Máximo de 2 idiomas selecionados
        </p>
      )}

      {/* Navegação */}
      <div className="flex gap-4">
        <Button
          type="button"
          variant="outline"
          onClick={prevStep}
          className="flex-1"
          disabled={contextLoading || isSaving}
        >
          Voltar
        </Button>
        <Button
          type="button"
          variant="primary"
          onClick={handleContinue}
          className="flex-1"
          disabled={selectedLanguages.length === 0 || contextLoading || isSaving}
        >
          {isSaving ? 'Salvando...' : 'Continuar'}
        </Button>
      </div>
    </div>
  );
};

export default LanguageStep;
