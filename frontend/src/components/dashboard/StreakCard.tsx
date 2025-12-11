import { useState, useEffect } from 'react';
import { cn } from '../../utils/cn';
import { streakService, handleApiError } from '../../services/streakService';
import type { DailyStreak } from '../../types';
import { Button } from '../common/Button';

interface StreakCardProps {
  languageCode: string;
  className?: string;
}

export const StreakCard = ({ languageCode, className }: StreakCardProps) => {
  const [streak, setStreak] = useState<DailyStreak | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isFreezing, setIsFreezing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadStreak = async () => {
      try {
        setIsLoading(true);
        const data = await streakService.getStreak(languageCode);
        setStreak(data);
      } catch (err) {
        const apiError = handleApiError(err);
        setError(apiError.message);
      } finally {
        setIsLoading(false);
      }
    };

    loadStreak();
  }, [languageCode]);

  const handleFreezeStreak = async () => {
    if (!streak || streak.streakFrozenUntil) return;

    try {
      setIsFreezing(true);
      const updated = await streakService.freezeStreak(languageCode);
      setStreak(updated);
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsFreezing(false);
    }
  };

  const isAtRisk = streak && streak.currentStreak > 0 && !streak.streakFrozenUntil;
  const isFrozen = streak?.streakFrozenUntil;

  if (isLoading) {
    return (
      <div className={cn('p-6 rounded-2xl bg-streak-light animate-pulse', className)}>
        <div className="h-20"></div>
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
    <div
      className={cn(
        'p-6 rounded-2xl shadow-card transition-all duration-300',
        streak && streak.currentStreak > 0 ? 'bg-streak-light' : 'bg-white',
        className
      )}
    >
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-4">
          <div
            className={cn(
              'w-16 h-16 rounded-2xl flex items-center justify-center text-4xl',
              streak && streak.currentStreak > 0 ? 'bg-white' : 'bg-gray-100'
            )}
          >
            {streak && streak.currentStreak > 0 ? '🔥' : '❄️'}
          </div>
          <div>
            <p
              className={cn(
                'text-4xl font-bold',
                streak && streak.currentStreak > 0 ? 'text-streak' : 'text-text-light'
              )}
            >
              {streak?.currentStreak || 0}
              <span className="text-lg font-normal ml-1">dias</span>
            </p>
            <p className="text-sm text-text-light">
              {streak && streak.currentStreak > 0
                ? 'Sequência atual'
                : 'Comece sua sequência hoje!'}
            </p>
          </div>
        </div>

        {isFrozen && (
          <div className="flex items-center gap-1 bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm">
            <span>🧊</span>
            <span>Congelado</span>
          </div>
        )}
      </div>

      {streak && (
        <div className="mt-4 pt-4 border-t border-gray-200/50">
          <div className="flex justify-between text-sm">
            <div>
              <p className="text-text-light">Recorde</p>
              <p className="font-semibold text-text">
                {streak.longestStreak} dias
              </p>
            </div>
            <div>
              <p className="text-text-light">Total de dias</p>
              <p className="font-semibold text-text">
                {streak.totalStudyDays} dias
              </p>
            </div>
            {isAtRisk && !isFrozen && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleFreezeStreak}
                disabled={isFreezing}
                className="text-blue-600 border-blue-600 hover:bg-blue-50"
              >
                {isFreezing ? 'Congelando...' : '🧊 Congelar'}
              </Button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
