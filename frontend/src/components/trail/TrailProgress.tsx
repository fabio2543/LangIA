import { cn } from '../../utils/cn';
import type { TrailProgress as TrailProgressType } from '../../types/trail';

interface TrailProgressProps {
  progress: TrailProgressType;
  className?: string;
  showDetails?: boolean;
}

/**
 * Componente de progresso de trilha.
 */
export const TrailProgress = ({
  progress,
  className,
  showDetails = true,
}: TrailProgressProps) => {
  const progressPercentage = progress.progressPercentage ?? 0;

  const formatTimeSpent = (minutes: number): string => {
    if (minutes === 0) return '0min';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}min`;
    }
    return `${mins}min`;
  };

  const remainingLessons = progress.totalLessons - progress.lessonsCompleted;

  return (
    <div className={cn('space-y-4', className)}>
      {/* Progress Bar */}
      <div>
        <div className="flex justify-between text-sm mb-2">
          <span className="font-medium text-text">Seu progresso</span>
          <span className="text-primary font-semibold">
            {progressPercentage.toFixed(0)}%
          </span>
        </div>
        <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-primary to-accent rounded-full transition-all duration-500"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      {/* Stats Grid */}
      {showDetails && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatCard
            icon="📚"
            label="Lições completadas"
            value={`${progress.lessonsCompleted}/${progress.totalLessons}`}
          />
          <StatCard
            icon="📝"
            label="Restantes"
            value={remainingLessons.toString()}
          />
          <StatCard
            icon="⭐"
            label="Score médio"
            value={progress.averageScore?.toFixed(1) ?? '-'}
          />
          <StatCard
            icon="⏱️"
            label="Tempo estudado"
            value={formatTimeSpent(progress.timeSpentMinutes)}
          />
        </div>
      )}

      {/* Last Activity */}
      {progress.lastActivityAt && (
        <p className="text-sm text-textLight text-center">
          Última atividade: {formatLastActivity(progress.lastActivityAt)}
        </p>
      )}
    </div>
  );
};

/**
 * Card de estatística individual.
 */
const StatCard = ({
  icon,
  label,
  value,
}: {
  icon: string;
  label: string;
  value: string;
}) => (
  <div className="bg-gray-50 rounded-xl p-4 text-center">
    <span className="text-2xl mb-2 block">{icon}</span>
    <p className="text-lg font-semibold text-text">{value}</p>
    <p className="text-xs text-textLight">{label}</p>
  </div>
);

/**
 * Formata data da última atividade.
 */
const formatLastActivity = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMins < 1) return 'agora';
  if (diffMins < 60) return `há ${diffMins} min`;
  if (diffHours < 24) return `há ${diffHours}h`;
  if (diffDays === 1) return 'ontem';
  if (diffDays < 7) return `há ${diffDays} dias`;

  return date.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short',
  });
};

export default TrailProgress;
