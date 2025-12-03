import { useState, useEffect } from 'react';
import { useOnboarding } from '../../../context/OnboardingContext';
import { learningPreferencesService } from '../../../services/profileService';
import { Button } from '../../common/Button';
import type { LearningPreferences, TimeAvailable, LearningFormat, LearningObjective } from '../../../types';

const TIME_OPTIONS: { value: TimeAvailable; label: string }[] = [
  { value: 'MIN_15', label: '15 minutos' },
  { value: 'MIN_30', label: '30 minutos' },
  { value: 'MIN_45', label: '45 minutos' },
  { value: 'H_1', label: '1 hora' },
  { value: 'H_1_30', label: '1h30' },
  { value: 'H_2_PLUS', label: '2+ horas' },
];

const FORMAT_OPTIONS: { value: LearningFormat; label: string; icon: string }[] = [
  { value: 'VIDEO_LESSONS', label: 'Video Aulas', icon: '1' },
  { value: 'WRITTEN_EXERCISES', label: 'Exercicios Escritos', icon: '2' },
  { value: 'CONVERSATION', label: 'Conversacao', icon: '3' },
  { value: 'GAMES', label: 'Jogos', icon: '4' },
  { value: 'READING', label: 'Leitura', icon: '5' },
  { value: 'AUDIO_PODCAST', label: 'Audio/Podcast', icon: '6' },
  { value: 'FLASHCARDS', label: 'Flashcards', icon: '7' },
];

const OBJECTIVE_OPTIONS: { value: LearningObjective; label: string }[] = [
  { value: 'CAREER', label: 'Carreira profissional' },
  { value: 'UNIVERSITY', label: 'Estudos academicos' },
  { value: 'EXAMS', label: 'Preparacao para exames' },
  { value: 'TRAVEL', label: 'Viagens' },
  { value: 'HOBBY', label: 'Hobby / Interesse pessoal' },
  { value: 'IMMIGRATION', label: 'Imigracao' },
  { value: 'OTHER', label: 'Outro' },
];

export const PreferencesStep = () => {
  const { nextStep, prevStep, isLoading: contextLoading } = useOnboarding();
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState<Partial<LearningPreferences>>({
    dailyTimeAvailable: 'MIN_30',
    preferredFormats: [],
    primaryObjective: undefined,
    topicsOfInterest: [],
  });

  useEffect(() => {
    const loadPreferences = async () => {
      try {
        const prefs = await learningPreferencesService.getLearningPreferences();
        if (prefs) {
          setFormData({
            dailyTimeAvailable: prefs.dailyTimeAvailable || 'MIN_30',
            preferredFormats: prefs.preferredFormats || [],
            primaryObjective: prefs.primaryObjective,
            topicsOfInterest: prefs.topicsOfInterest || [],
          });
        }
      } catch (err) {
        console.error('Erro ao carregar preferencias:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadPreferences();
  }, []);

  const toggleFormat = (format: LearningFormat) => {
    const current = formData.preferredFormats || [];
    if (current.includes(format)) {
      setFormData({
        ...formData,
        preferredFormats: current.filter((f) => f !== format),
      });
    } else {
      setFormData({
        ...formData,
        preferredFormats: [...current, format],
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      setIsSaving(true);
      await learningPreferencesService.updateLearningPreferences({
        dailyTimeAvailable: formData.dailyTimeAvailable || null,
        preferredFormats: formData.preferredFormats || [],
        primaryObjective: formData.primaryObjective,
        topicsOfInterest: formData.topicsOfInterest || [],
        preferredDays: [],
      });
      nextStep();
    } catch (err) {
      setError('Erro ao salvar preferencias. Tente novamente.');
      console.error('Erro ao salvar:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleSkip = () => {
    nextStep();
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="text-4xl mb-4 animate-bounce">...</div>
          <p className="text-text-light">Carregando...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto">
      <div className="text-center mb-8">
        <div className="text-6xl mb-4">{"\\u2699\\uFE0F"}</div>
        <h1 className="text-3xl font-serif italic text-text mb-2">
          Como voce gosta de aprender?
        </h1>
        <p className="text-text-light">
          Personalize sua experiencia de aprendizado.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm">
            {error}
          </div>
        )}

        {/* Tempo Disponivel */}
        <div className="bg-white rounded-2xl shadow-card p-6">
          <h2 className="text-lg font-semibold text-text mb-4">
            Quanto tempo voce tem por dia?
          </h2>
          <div className="grid grid-cols-3 gap-3">
            {TIME_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => setFormData({ ...formData, dailyTimeAvailable: option.value })}
                className={`
                  py-3 px-4 rounded-xl text-sm font-medium transition-all
                  ${
                    formData.dailyTimeAvailable === option.value
                      ? 'bg-accent text-white'
                      : 'bg-bg text-text hover:bg-gray-100'
                  }
                `}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        {/* Formatos Preferidos */}
        <div className="bg-white rounded-2xl shadow-card p-6">
          <h2 className="text-lg font-semibold text-text mb-4">
            Formatos preferidos
          </h2>
          <p className="text-sm text-text-light mb-4">
            Selecione todos que voce gosta
          </p>
          <div className="grid grid-cols-2 gap-3">
            {FORMAT_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => toggleFormat(option.value)}
                className={`
                  py-3 px-4 rounded-xl text-sm font-medium transition-all text-left
                  ${
                    formData.preferredFormats?.includes(option.value)
                      ? 'bg-accent text-white'
                      : 'bg-bg text-text hover:bg-gray-100'
                  }
                `}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        {/* Objetivo */}
        <div className="bg-white rounded-2xl shadow-card p-6">
          <h2 className="text-lg font-semibold text-text mb-4">
            Qual seu principal objetivo?
          </h2>
          <div className="space-y-2">
            {OBJECTIVE_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => setFormData({ ...formData, primaryObjective: option.value })}
                className={`
                  w-full py-3 px-4 rounded-xl text-sm font-medium transition-all text-left
                  ${
                    formData.primaryObjective === option.value
                      ? 'bg-accent text-white'
                      : 'bg-bg text-text hover:bg-gray-100'
                  }
                `}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

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
            variant="ghost"
            onClick={handleSkip}
            disabled={contextLoading}
          >
            Pular
          </Button>
          <Button
            type="submit"
            variant="primary"
            className="flex-1"
            disabled={isSaving || contextLoading}
          >
            {isSaving ? 'Salvando...' : 'Continuar'}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default PreferencesStep;
