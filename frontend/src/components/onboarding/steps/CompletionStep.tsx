import { useState } from 'react';
import { useOnboarding } from '../../../hooks/useOnboarding';
import { Button } from '../../common/Button';
import { logger } from '../../../utils/logger';

export const CompletionStep = () => {
  const { completeOnboarding, prevStep, status, isLoading: contextLoading } = useOnboarding();

  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleComplete = async () => {
    logger.log('[CompletionStep] handleComplete called');
    logger.log('[CompletionStep] status:', status);
    try {
      setIsGenerating(true);
      setError(null);
      logger.log('[CompletionStep] Calling completeOnboarding...');
      const response = await completeOnboarding();
      logger.log('[CompletionStep] Response:', response);

      if (response.success) {
        setSuccess(true);
        // O redirect e feito pelo context
      } else {
        setError(response.message || 'Erro ao gerar sua trilha');
      }
    } catch (err) {
      setError('Ocorreu um erro. Tente novamente.');
      logger.error('Erro ao completar onboarding:', err);
    } finally {
      setIsGenerating(false);
    }
  };

  if (success) {
    return (
      <div className="max-w-xl mx-auto text-center">
        <div className="text-8xl mb-6 animate-bounce">🎉</div>
        <h1 className="text-3xl font-serif italic text-text mb-4">
          Tudo pronto!
        </h1>
        <p className="text-text-light mb-6">
          Sua trilha de aprendizado personalizada foi criada.
          Redirecionando...
        </p>
        <div className="animate-pulse">
          <div className="w-16 h-16 mx-auto border-4 border-accent border-t-transparent rounded-full animate-spin" />
        </div>
      </div>
    );
  }

  if (isGenerating) {
    return (
      <div className="max-w-xl mx-auto text-center">
        <div className="text-8xl mb-6">🚀</div>
        <h1 className="text-3xl font-serif italic text-text mb-4">
          Gerando sua trilha...
        </h1>
        <p className="text-text-light mb-8">
          Estamos criando um plano de estudos personalizado para voce.
        </p>
        <div className="w-full bg-gray-200 rounded-full h-2 mb-4">
          <div className="bg-accent h-2 rounded-full animate-pulse w-3/4" />
        </div>
        <p className="text-sm text-text-light">Isso pode levar alguns segundos...</p>
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto">
      <div className="text-center mb-8">
        <div className="text-8xl mb-6">🎯</div>
        <h1 className="text-3xl font-serif italic text-text mb-4">
          Pronto para comecar!
        </h1>
        <p className="text-text-light">
          Revisamos suas preferencias e estamos prontos para criar sua trilha de aprendizado personalizada.
        </p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm mb-6">
          {error}
        </div>
      )}

      {/* Resumo */}
      <div className="bg-white rounded-2xl shadow-card p-6 mb-8">
        <h2 className="text-lg font-semibold text-text mb-4">Resumo</h2>

        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${status?.steps.profileComplete ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'}`}>
              {status?.steps.profileComplete ? (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              ) : (
                <span>1</span>
              )}
            </div>
            <span className="text-text">Perfil configurado</span>
          </div>

          <div className="flex items-center gap-3">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${status?.steps.languageEnrolled ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'}`}>
              {status?.steps.languageEnrolled ? (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              ) : (
                <span>2</span>
              )}
            </div>
            <span className="text-text">Idioma selecionado</span>
          </div>

          <div className="flex items-center gap-3">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${status?.steps.preferencesSet ? 'bg-green-100 text-green-600' : 'bg-yellow-100 text-yellow-600'}`}>
              {status?.steps.preferencesSet ? (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              ) : (
                <span>-</span>
              )}
            </div>
            <span className="text-text">
              Preferencias {status?.steps.preferencesSet ? 'configuradas' : '(opcional)'}
            </span>
          </div>

          <div className="flex items-center gap-3">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${status?.steps.assessmentDone ? 'bg-green-100 text-green-600' : 'bg-yellow-100 text-yellow-600'}`}>
              {status?.steps.assessmentDone ? (
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              ) : (
                <span>-</span>
              )}
            </div>
            <span className="text-text">
              Autoavaliacao {status?.steps.assessmentDone ? 'completa' : '(opcional)'}
            </span>
          </div>
        </div>
      </div>

      {/* Botoes */}
      <div className="flex gap-4">
        <Button
          type="button"
          variant="outline"
          onClick={prevStep}
          className="flex-1"
          disabled={contextLoading || isGenerating}
        >
          Voltar
        </Button>
        <Button
          type="button"
          variant="primary"
          onClick={handleComplete}
          className="flex-1"
          disabled={!status?.steps?.languageEnrolled || contextLoading || isGenerating}
        >
          Gerar minha trilha
        </Button>
      </div>
    </div>
  );
};

export default CompletionStep;
