import { useState, useEffect } from 'react';
import { useOnboarding } from '../../../hooks/useOnboarding';
import { learningPreferencesService } from '../../../services/profileService';
import { Button } from '../../common/Button';
import { logger } from '../../../utils/logger';
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
    objectiveDescription: '',
  });
  const [selectedObjectives, setSelectedObjectives] = useState<LearningObjective[]>([]);

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
            objectiveDescription: prefs.objectiveDescription || '',
          });
          // Carrega objetivos existentes
          const existingObjectives: LearningObjective[] = [];
          if (prefs.primaryObjective) {
            existingObjectives.push(prefs.primaryObjective);
          }
          if (prefs.secondaryObjective) {
            existingObjectives.push(prefs.secondaryObjective);
          }
          setSelectedObjectives(existingObjectives);
        }
      } catch (err) {
        logger.error('Erro ao carregar preferencias:', err);
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

  const toggleObjective = (objective: LearningObjective) => {
    if (selectedObjectives.includes(objective)) {
      setSelectedObjectives(selectedObjectives.filter((o) => o !== objective));
    } else if (selectedObjectives.length < 2) {
      setSelectedObjectives([...selectedObjectives, objective]);
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
        primaryObjective: selectedObjectives[0] || undefined,
        secondaryObjective: selectedObjectives[1] || undefined,
        objectiveDescription: formData.objectiveDescription || undefined,
        topicsOfInterest: formData.topicsOfInterest || [],
        preferredDays: [],
      });
      nextStep();
    } catch (err) {
      setError('Erro ao salvar preferencias. Tente novamente.');
      logger.error('Erro ao salvar:', err);
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
        <div className="text-6xl mb-4">⚙️</div>
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
            Quais seus objetivos?
          </h2>
          <p className="text-sm text-text-light mb-4">
            Selecione até 2 objetivos
          </p>
          <div className="space-y-2">
            {OBJECTIVE_OPTIONS.map((option) => {
              const isSelected = selectedObjectives.includes(option.value);
              const isFirst = selectedObjectives[0] === option.value;
              return (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => toggleObjective(option.value)}
                  className={`
                    w-full py-3 px-4 rounded-xl text-sm font-medium transition-all text-left flex items-center justify-between
                    ${
                      isSelected
                        ? 'bg-accent text-white'
                        : 'bg-bg text-text hover:bg-gray-100'
                    }
                  `}
                >
                  <span>{option.label}</span>
                  {isFirst && (
                    <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full">
                      Principal
                    </span>
                  )}
                </button>
              );
            })}
          </div>
          {selectedObjectives.length === 2 && (
            <p className="text-xs text-text-light mt-3 text-center">
              Máximo de 2 objetivos selecionados
            </p>
          )}

          {/* Campo de detalhamento - aparece quando há objetivo selecionado */}
          {selectedObjectives.length > 0 && (
            <div className="mt-6 pt-6 border-t border-gray-100">
              <div className="flex items-start gap-2 mb-3">
                <span className="text-lg">✨</span>
                <div>
                  <h3 className="text-sm font-semibold text-text">
                    Conte mais sobre seus objetivos
                  </h3>
                  <p className="text-xs text-accent font-medium">
                    Super importante para personalizar sua trilha!
                  </p>
                </div>
              </div>
              <textarea
                value={formData.objectiveDescription || ''}
                onChange={(e) => setFormData({ ...formData, objectiveDescription: e.target.value })}
                placeholder="Ex: Preciso de ingles para uma entrevista de emprego em uma multinacional daqui a 3 meses. Quero focar em vocabulario de negocios e melhorar minha fluencia em reunioes..."
                className="w-full p-4 rounded-xl bg-bg border-2 border-transparent focus:border-accent focus:outline-none text-sm text-text placeholder:text-text-light/60 resize-none transition-colors"
                rows={4}
                maxLength={500}
              />
              <p className="text-xs text-text-light mt-2 text-right">
                {formData.objectiveDescription?.length || 0}/500 caracteres
              </p>
            </div>
          )}
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
