import { useState, useEffect } from 'react';
import { useOnboarding } from '../../../hooks/useOnboarding';
import { languageService, skillAssessmentService } from '../../../services/profileService';
import { Button } from '../../common/Button';
import type { SkillAssessment, DifficultyLevel, CefrLevel, LanguageEnrollment } from '../../../types';

const DIFFICULTY_OPTIONS: { value: DifficultyLevel; label: string; color: string }[] = [
  { value: 'NONE', label: 'Sem dificuldade', color: 'bg-green-100 text-green-700' },
  { value: 'LOW', label: 'Pouca dificuldade', color: 'bg-yellow-100 text-yellow-700' },
  { value: 'MODERATE', label: 'Dificuldade moderada', color: 'bg-orange-100 text-orange-700' },
  { value: 'HIGH', label: 'Muita dificuldade', color: 'bg-red-100 text-red-700' },
];

const SKILLS = [
  { key: 'listening', label: 'Compreensao Auditiva', icon: '1', description: 'Entender falantes nativos' },
  { key: 'speaking', label: 'Expressao Oral', icon: '2', description: 'Falar e se expressar' },
  { key: 'reading', label: 'Leitura', icon: '3', description: 'Ler e compreender textos' },
  { key: 'writing', label: 'Escrita', icon: '4', description: 'Escrever textos' },
] as const;

type SkillKey = typeof SKILLS[number]['key'];

export const AssessmentStep = () => {
  const { nextStep, prevStep, isLoading: contextLoading } = useOnboarding();
  const [enrolledLanguages, setEnrolledLanguages] = useState<LanguageEnrollment[]>([]);
  const [currentLanguageIndex, setCurrentLanguageIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [assessment, setAssessment] = useState<{
    listeningDifficulty: DifficultyLevel;
    speakingDifficulty: DifficultyLevel;
    readingDifficulty: DifficultyLevel;
    writingDifficulty: DifficultyLevel;
    selfCefrLevel?: CefrLevel;
  }>({
    listeningDifficulty: 'MODERATE',
    speakingDifficulty: 'MODERATE',
    readingDifficulty: 'MODERATE',
    writingDifficulty: 'MODERATE',
  });

  useEffect(() => {
    const loadData = async () => {
      try {
        const enrolled = await languageService.getEnrollments();
        setEnrolledLanguages(enrolled);
      } catch (err) {
        console.error('Erro ao carregar idiomas:', err);
        setError('Erro ao carregar seus idiomas');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  const currentLanguage = enrolledLanguages[currentLanguageIndex];

  const setDifficulty = (skill: SkillKey, value: DifficultyLevel) => {
    setAssessment({
      ...assessment,
      [`${skill}Difficulty`]: value,
    });
  };

  const handleSaveAssessment = async () => {
    if (!currentLanguage) return;

    try {
      setIsSaving(true);
      setError(null);

      const data: SkillAssessment = {
        language: currentLanguage.languageCode,
        listeningDifficulty: assessment.listeningDifficulty,
        speakingDifficulty: assessment.speakingDifficulty,
        readingDifficulty: assessment.readingDifficulty,
        writingDifficulty: assessment.writingDifficulty,
        selfCefrLevel: assessment.selfCefrLevel,
      };

      await skillAssessmentService.createSkillAssessment(data);

      // Se tem mais idiomas, vai para o proximo
      if (currentLanguageIndex < enrolledLanguages.length - 1) {
        setCurrentLanguageIndex(currentLanguageIndex + 1);
        // Reset assessment para o proximo idioma
        setAssessment({
          listeningDifficulty: 'MODERATE',
          speakingDifficulty: 'MODERATE',
          readingDifficulty: 'MODERATE',
          writingDifficulty: 'MODERATE',
        });
      } else {
        // Todos os idiomas avaliados, vai para o proximo step
        nextStep();
      }
    } catch (err) {
      setError('Erro ao salvar avaliacao. Tente novamente.');
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

  if (enrolledLanguages.length === 0) {
    return (
      <div className="max-w-xl mx-auto text-center">
        <div className="text-6xl mb-4">⚠️</div>
        <h1 className="text-2xl font-serif italic text-text mb-4">
          Nenhum idioma selecionado
        </h1>
        <p className="text-text-light mb-6">
          Volte e adicione pelo menos um idioma para fazer a autoavaliacao.
        </p>
        <Button variant="primary" onClick={prevStep}>
          Voltar
        </Button>
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto">
      <div className="text-center mb-8">
        <div className="text-6xl mb-4">📝</div>
        <h1 className="text-3xl font-serif italic text-text mb-2">
          Autoavaliacao: {currentLanguage?.languageNamePt}
        </h1>
        <p className="text-text-light">
          Avalie sua dificuldade em cada habilidade.
          {enrolledLanguages.length > 1 && (
            <span className="block mt-1 text-sm">
              Idioma {currentLanguageIndex + 1} de {enrolledLanguages.length}
            </span>
          )}
        </p>
      </div>

      <div className="space-y-6">
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm">
            {error}
          </div>
        )}

        {SKILLS.map((skill) => (
          <div key={skill.key} className="bg-white rounded-2xl shadow-card p-6">
            <div className="mb-4">
              <h3 className="text-lg font-semibold text-text">{skill.label}</h3>
              <p className="text-sm text-text-light">{skill.description}</p>
            </div>

            <div className="grid grid-cols-2 gap-2">
              {DIFFICULTY_OPTIONS.map((option) => (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => setDifficulty(skill.key, option.value)}
                  className={`
                    py-2 px-3 rounded-lg text-sm font-medium transition-all
                    ${
                      assessment[`${skill.key}Difficulty` as keyof typeof assessment] === option.value
                        ? option.color + ' ring-2 ring-offset-1 ring-current'
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    }
                  `}
                >
                  {option.label}
                </button>
              ))}
            </div>
          </div>
        ))}

        {/* Navegacao */}
        <div className="flex gap-4 pt-4">
          <Button
            type="button"
            variant="outline"
            onClick={currentLanguageIndex > 0 ? () => setCurrentLanguageIndex(currentLanguageIndex - 1) : prevStep}
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
            type="button"
            variant="primary"
            onClick={handleSaveAssessment}
            className="flex-1"
            disabled={isSaving || contextLoading}
          >
            {isSaving
              ? 'Salvando...'
              : currentLanguageIndex < enrolledLanguages.length - 1
                ? 'Proximo idioma'
                : 'Continuar'}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AssessmentStep;
