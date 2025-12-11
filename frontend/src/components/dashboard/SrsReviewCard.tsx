import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { cn } from '../../utils/cn';
import { srsService, handleApiError } from '../../services/srsService';
import type { SrsStats } from '../../types';
import { Button } from '../common/Button';

interface SrsReviewCardProps {
  languageCode: string;
  className?: string;
}

export const SrsReviewCard = ({ languageCode, className }: SrsReviewCardProps) => {
  const navigate = useNavigate();
  const [stats, setStats] = useState<SrsStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadStats = async () => {
      try {
        setIsLoading(true);
        const data = await srsService.getStats(languageCode);
        setStats(data);
      } catch (err) {
        const apiError = handleApiError(err);
        setError(apiError.message);
      } finally {
        setIsLoading(false);
      }
    };

    loadStats();
  }, [languageCode]);

  const handleStartReview = () => {
    navigate(`/review?language=${languageCode}`);
  };

  if (isLoading) {
    return (
      <div className={cn('p-6 rounded-2xl bg-primary-light animate-pulse', className)}>
        <div className="h-32"></div>
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

  const hasDueCards = stats && stats.dueToday > 0;
  const progress = stats ? Math.round((stats.reviewedToday / (stats.dueToday + stats.reviewedToday || 1)) * 100) : 0;

  return (
    <div
      className={cn(
        'p-6 rounded-2xl shadow-card',
        hasDueCards ? 'bg-primary-light' : 'bg-white',
        className
      )}
    >
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div
            className={cn(
              'w-12 h-12 rounded-xl flex items-center justify-center text-2xl',
              hasDueCards ? 'bg-white' : 'bg-gray-100'
            )}
          >
            📚
          </div>
          <div>
            <h3 className="font-semibold text-text">Revisão SRS</h3>
            <p className="text-sm text-text-light">Flashcards para hoje</p>
          </div>
        </div>
      </div>

      {stats && (
        <>
          <div className="grid grid-cols-3 gap-4 mb-4">
            <div className="text-center">
              <p className={cn('text-2xl font-bold', hasDueCards ? 'text-primary' : 'text-text')}>
                {stats.dueToday}
              </p>
              <p className="text-xs text-text-light">Pendentes</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-success">{stats.reviewedToday}</p>
              <p className="text-xs text-text-light">Revisados</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-text">{stats.mastered}</p>
              <p className="text-xs text-text-light">Dominados</p>
            </div>
          </div>

          {/* Progress bar */}
          <div className="mb-4">
            <div className="flex justify-between text-xs text-text-light mb-1">
              <span>Progresso de hoje</span>
              <span>{progress}%</span>
            </div>
            <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div
                className="h-full bg-primary rounded-full transition-all duration-500"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>

          <Button
            variant={hasDueCards ? 'primary' : 'outline'}
            fullWidth
            onClick={handleStartReview}
            disabled={!hasDueCards}
          >
            {hasDueCards
              ? `Revisar ${stats.dueToday} cards`
              : 'Nenhum card para revisar'}
          </Button>
        </>
      )}
    </div>
  );
};
