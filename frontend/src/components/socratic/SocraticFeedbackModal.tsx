import { useState, useCallback, useEffect } from 'react';
import { cn } from '../../utils/cn';
import { ReflectionInput } from './ReflectionInput';
import { LearningMomentCard } from './LearningMomentCard';
import { socraticService } from '../../services/socraticService';
import type {
  SkillType,
  SocraticReflectionResponse,
} from '../../types';

type ModalStep = 'question' | 'reflection' | 'followup' | 'result';

interface SocraticFeedbackModalProps {
  isOpen: boolean;
  onClose: () => void;
  languageCode: string;
  skillType: SkillType;
  userInput: string;
  expectedOutput?: string;
  errorContext?: string;
  lessonId?: string;
  exerciseId?: string;
  className?: string;
}

export const SocraticFeedbackModal = ({
  isOpen,
  onClose,
  languageCode,
  skillType,
  userInput,
  expectedOutput,
  errorContext,
  lessonId,
  exerciseId,
  className,
}: SocraticFeedbackModalProps) => {
  const [step, setStep] = useState<ModalStep>('question');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [interactionId, setInteractionId] = useState<string | null>(null);
  const [aiQuestion, setAiQuestion] = useState<string>('');
  const [hints, setHints] = useState<string[]>([]);
  const [aiFollowUp, setAiFollowUp] = useState<string | null>(null);
  const [result, setResult] = useState<SocraticReflectionResponse | null>(null);

  // Request initial feedback when modal opens
  useEffect(() => {
    if (isOpen && step === 'question') {
      requestFeedback();
    }
  }, [isOpen]);

  const requestFeedback = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await socraticService.requestFeedback({
        languageCode,
        skillType,
        userInput,
        expectedOutput,
        errorContext,
        lessonId,
        exerciseId,
      });
      setInteractionId(response.interactionId);
      setAiQuestion(response.aiQuestion);
      setHints(response.hints || []);
      setStep('reflection');
    } catch {
      setError('Erro ao solicitar feedback. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleReflectionSubmit = useCallback(
    async (reflection: string) => {
      if (!interactionId) return;

      setIsLoading(true);
      setError(null);
      try {
        const response = await socraticService.submitReflection({
          interactionId,
          userReflection: reflection,
        });

        if (response.needsFollowUp && response.aiFollowUp) {
          setAiFollowUp(response.aiFollowUp);
          setStep('followup');
        } else {
          setResult(response);
          setStep('result');
        }
      } catch {
        setError('Erro ao enviar reflexão. Tente novamente.');
      } finally {
        setIsLoading(false);
      }
    },
    [interactionId]
  );

  const handleFollowUpSubmit = useCallback(
    async (reflection: string) => {
      if (!interactionId) return;

      setIsLoading(true);
      setError(null);
      try {
        const response = await socraticService.submitReflection({
          interactionId,
          userReflection: reflection,
        });
        setResult(response);
        setStep('result');
      } catch {
        setError('Erro ao enviar reflexão. Tente novamente.');
      } finally {
        setIsLoading(false);
      }
    },
    [interactionId]
  );

  const handleRate = useCallback(
    async (rating: number) => {
      if (!interactionId) return;
      try {
        await socraticService.rateInteraction(interactionId, rating);
      } catch {
        // Silent fail for rating
      }
      onClose();
    },
    [interactionId, onClose]
  );

  const handleClose = () => {
    setStep('question');
    setInteractionId(null);
    setAiQuestion('');
    setHints([]);
    setAiFollowUp(null);
    setResult(null);
    setError(null);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={handleClose}
        aria-hidden="true"
      />

      {/* Modal */}
      <div
        className={cn(
          'relative w-full max-w-lg bg-white rounded-3xl shadow-2xl overflow-hidden',
          'max-h-[90vh] overflow-y-auto',
          className
        )}
        role="dialog"
        aria-modal="true"
        aria-labelledby="socratic-modal-title"
      >
        {/* Header */}
        <div className="sticky top-0 bg-gradient-to-r from-primary to-primary-dark px-6 py-5 text-white">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="text-2xl">🧠</span>
              <div>
                <h2 id="socratic-modal-title" className="font-bold text-lg">
                  Feedback Socrático
                </h2>
                <p className="text-sm text-white/70">
                  Aprenda refletindo sobre seus erros
                </p>
              </div>
            </div>
            <button
              onClick={handleClose}
              className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center hover:bg-white/30 transition-colors"
              aria-label="Fechar"
            >
              ✕
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Error */}
          {error && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-800 text-sm">
              {error}
            </div>
          )}

          {/* Loading State */}
          {isLoading && step === 'question' && (
            <div className="flex flex-col items-center py-12">
              <div className="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mb-4" />
              <p className="text-text-light">Analisando sua resposta...</p>
            </div>
          )}

          {/* User's Original Input */}
          {!isLoading && (
            <div className="bg-gray-50 rounded-xl p-4">
              <p className="text-xs text-text-light mb-1 uppercase tracking-wide">
                Sua resposta
              </p>
              <p className="text-text font-medium">{userInput}</p>
            </div>
          )}

          {/* Step: Reflection */}
          {step === 'reflection' && (
            <div className="space-y-4">
              {/* AI Question */}
              <div className="bg-primary-light rounded-xl p-4">
                <div className="flex items-start gap-3">
                  <span className="text-xl">🤔</span>
                  <div>
                    <p className="text-xs text-primary mb-1 uppercase tracking-wide">
                      Pergunta guiada
                    </p>
                    <p className="text-text font-medium">{aiQuestion}</p>
                  </div>
                </div>
              </div>

              {/* Hints */}
              {hints.length > 0 && (
                <details className="group">
                  <summary className="text-sm text-primary cursor-pointer hover:text-primary-dark">
                    💡 Ver dicas ({hints.length})
                  </summary>
                  <ul className="mt-2 space-y-2 pl-4">
                    {hints.map((hint, i) => (
                      <li key={i} className="text-sm text-text-light">
                        • {hint}
                      </li>
                    ))}
                  </ul>
                </details>
              )}

              {/* Reflection Input */}
              <ReflectionInput
                placeholder="Reflita sobre a pergunta acima..."
                onSubmit={handleReflectionSubmit}
                isLoading={isLoading}
              />
            </div>
          )}

          {/* Step: Follow-up */}
          {step === 'followup' && aiFollowUp && (
            <div className="space-y-4">
              {/* AI Follow-up */}
              <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
                <div className="flex items-start gap-3">
                  <span className="text-xl">🔍</span>
                  <div>
                    <p className="text-xs text-amber-700 mb-1 uppercase tracking-wide">
                      Vamos aprofundar
                    </p>
                    <p className="text-text font-medium">{aiFollowUp}</p>
                  </div>
                </div>
              </div>

              {/* Reflection Input */}
              <ReflectionInput
                placeholder="Continue sua reflexão..."
                onSubmit={handleFollowUpSubmit}
                isLoading={isLoading}
              />
            </div>
          )}

          {/* Step: Result */}
          {step === 'result' && result && (
            <LearningMomentCard
              learningMoment={result.learningMoment}
              selfCorrectionAchieved={result.selfCorrectionAchieved}
              finalCorrection={result.finalCorrection}
              onRate={handleRate}
            />
          )}
        </div>

        {/* Footer */}
        {step === 'result' && (
          <div className="px-6 py-4 bg-gray-50 border-t border-gray-100">
            <button
              onClick={handleClose}
              className="w-full py-3 bg-primary text-white rounded-xl font-medium hover:bg-primary-dark transition-colors"
            >
              Continuar
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
