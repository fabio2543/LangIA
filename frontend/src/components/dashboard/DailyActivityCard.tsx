import { useState, useEffect } from 'react';
import { cn } from '../../utils/cn';
import { streakService, handleApiError } from '../../services/streakService';
import type { DailyActivityLog, SkillType } from '../../types';

interface DailyActivityCardProps {
  languageCode: string;
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

const skillTypeColors: Record<SkillType, string> = {
  listening: 'bg-blue-100 text-blue-700',
  speaking: 'bg-green-100 text-green-700',
  reading: 'bg-purple-100 text-purple-700',
  writing: 'bg-yellow-100 text-yellow-700',
  grammar: 'bg-pink-100 text-pink-700',
  vocabulary: 'bg-indigo-100 text-indigo-700',
  pronunciation: 'bg-orange-100 text-orange-700',
};

export const DailyActivityCard = ({ languageCode, className }: DailyActivityCardProps) => {
  const [activity, setActivity] = useState<DailyActivityLog | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadActivity = async () => {
      try {
        setIsLoading(true);
        const logs = await streakService.getActivityLog(languageCode, 1);
        // Pega apenas a atividade de hoje
        const today = new Date().toISOString().split('T')[0];
        const todayActivity = logs.find((log) => log.activityDate === today);
        setActivity(todayActivity || null);
      } catch (err) {
        const apiError = handleApiError(err);
        setError(apiError.message);
      } finally {
        setIsLoading(false);
      }
    };

    loadActivity();
  }, [languageCode]);

  if (isLoading) {
    return (
      <div className={cn('p-6 rounded-2xl bg-white shadow-card animate-pulse', className)}>
        <div className="h-40"></div>
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

  const hasActivity = activity && (
    activity.lessonsCompleted > 0 ||
    activity.exercisesCompleted > 0 ||
    activity.cardsReviewed > 0 ||
    activity.minutesStudied > 0
  );

  return (
    <div className={cn('p-6 rounded-2xl bg-white shadow-card', className)}>
      <div className="flex items-center gap-3 mb-4">
        <div className="w-10 h-10 rounded-xl bg-success-light flex items-center justify-center text-xl">
          📊
        </div>
        <div>
          <h3 className="font-semibold text-text">Atividade de Hoje</h3>
          <p className="text-sm text-text-light">
            {new Date().toLocaleDateString('pt-BR', {
              weekday: 'long',
              day: 'numeric',
              month: 'short',
            })}
          </p>
        </div>
      </div>

      {!hasActivity ? (
        <div className="text-center py-6">
          <div className="text-4xl mb-2">🌅</div>
          <p className="text-text-light">Nenhuma atividade ainda</p>
          <p className="text-sm text-text-light">Comece uma lição para começar!</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-3 mb-4">
            <div className="bg-gray-50 rounded-xl p-3 text-center">
              <p className="text-2xl font-bold text-primary">{activity.lessonsCompleted}</p>
              <p className="text-xs text-text-light">Lições</p>
            </div>
            <div className="bg-gray-50 rounded-xl p-3 text-center">
              <p className="text-2xl font-bold text-success">{activity.exercisesCompleted}</p>
              <p className="text-xs text-text-light">Exercícios</p>
            </div>
            <div className="bg-gray-50 rounded-xl p-3 text-center">
              <p className="text-2xl font-bold text-text">{activity.cardsReviewed}</p>
              <p className="text-xs text-text-light">Cards</p>
            </div>
            <div className="bg-gray-50 rounded-xl p-3 text-center">
              <p className="text-2xl font-bold text-streak">{activity.minutesStudied}</p>
              <p className="text-xs text-text-light">Minutos</p>
            </div>
          </div>

          <div className="flex items-center justify-between pt-3 border-t border-gray-100">
            <div className="flex items-center gap-2">
              <span className="text-lg">⚡</span>
              <span className="text-sm font-medium text-text">
                +{activity.xpEarned} XP
              </span>
            </div>
            {activity.skillsPracticed.length > 0 && (
              <div className="flex gap-1 flex-wrap justify-end">
                {activity.skillsPracticed.slice(0, 3).map((skill) => (
                  <span
                    key={skill}
                    className={cn(
                      'text-xs px-2 py-0.5 rounded-full',
                      skillTypeColors[skill]
                    )}
                  >
                    {skillTypeLabels[skill]}
                  </span>
                ))}
                {activity.skillsPracticed.length > 3 && (
                  <span className="text-xs text-text-light">
                    +{activity.skillsPracticed.length - 3}
                  </span>
                )}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};
