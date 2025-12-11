import { useState, useEffect } from 'react';
import { cn } from '../../utils/cn';
import { exerciseService, handleApiError } from '../../services/exerciseService';
import type { ErrorPattern, SkillType } from '../../types';

interface TopErrorsCardProps {
  languageCode: string;
  limit?: number;
  className?: string;
}

const skillTypeLabels: Record<SkillType, string> = {
  listening: 'Listening',
  speaking: 'Speaking',
  reading: 'Reading',
  writing: 'Writing',
  grammar: 'Gramática',
  vocabulary: 'Vocabulário',
  pronunciation: 'Pronúncia',
};

const skillTypeIcons: Record<SkillType, string> = {
  listening: '👂',
  speaking: '🗣️',
  reading: '📖',
  writing: '✍️',
  grammar: '📝',
  vocabulary: '📚',
  pronunciation: '🎤',
};

export const TopErrorsCard = ({
  languageCode,
  limit = 5,
  className,
}: TopErrorsCardProps) => {
  const [errors, setErrors] = useState<ErrorPattern[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadErrors = async () => {
      try {
        setIsLoading(true);
        const data = await exerciseService.getTopErrors(languageCode, limit);
        setErrors(data);
      } catch (err) {
        const apiError = handleApiError(err);
        setError(apiError.message);
      } finally {
        setIsLoading(false);
      }
    };

    loadErrors();
  }, [languageCode, limit]);

  const handleMarkResolved = async (errorId: string) => {
    try {
      await exerciseService.markErrorResolved(errorId);
      setErrors((prev) => prev.filter((e) => e.id !== errorId));
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    }
  };

  if (isLoading) {
    return (
      <div className={cn('p-6 rounded-2xl bg-white shadow-card animate-pulse', className)}>
        <div className="h-48"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={cn('p-6 rounded-2xl bg-red-50 text-red-600', className)}>
        <p className="text-sm">{error}</p>
      </div>
    );
  }

  return (
    <div className={cn('p-6 rounded-2xl bg-white shadow-card', className)}>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-red-100 flex items-center justify-center text-xl">
            ⚠️
          </div>
          <div>
            <h3 className="font-semibold text-text">Erros Recorrentes</h3>
            <p className="text-sm text-text-light">Foque nestes pontos</p>
          </div>
        </div>
        {errors.length > 0 && (
          <span className="bg-red-100 text-red-600 text-xs font-medium px-2 py-1 rounded-full">
            {errors.length} {errors.length === 1 ? 'erro' : 'erros'}
          </span>
        )}
      </div>

      {errors.length === 0 ? (
        <div className="text-center py-8">
          <div className="text-4xl mb-2">🎉</div>
          <p className="text-text-light">Nenhum erro recorrente!</p>
          <p className="text-sm text-text-light">Continue praticando</p>
        </div>
      ) : (
        <div className="space-y-3">
          {errors.map((errorPattern, index) => (
            <div
              key={errorPattern.id}
              className="flex items-start gap-3 p-3 bg-gray-50 rounded-xl group hover:bg-gray-100 transition-colors"
            >
              <div className="flex items-center justify-center w-6 h-6 rounded-full bg-red-100 text-red-600 text-xs font-bold flex-shrink-0">
                {index + 1}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-sm">
                    {skillTypeIcons[errorPattern.skillType]}
                  </span>
                  <span className="text-xs text-text-light">
                    {skillTypeLabels[errorPattern.skillType]}
                  </span>
                  <span className="text-xs bg-red-100 text-red-600 px-1.5 py-0.5 rounded">
                    {errorPattern.occurrenceCount}x
                  </span>
                </div>
                <p className="text-sm text-text font-medium truncate">
                  {errorPattern.errorDescription}
                </p>
                {errorPattern.exampleErrors.length > 0 && (
                  <p className="text-xs text-text-light mt-1 truncate">
                    Ex: {errorPattern.exampleErrors[0]}
                  </p>
                )}
              </div>
              <button
                onClick={() => handleMarkResolved(errorPattern.id)}
                className="opacity-0 group-hover:opacity-100 text-xs text-success hover:text-success-dark transition-all"
                title="Marcar como resolvido"
              >
                ✓
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
