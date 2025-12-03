import { cn } from '../../utils/cn';
import type { TrailSummary } from '../../types/trail';

interface TrailCardProps {
  trail: TrailSummary;
  onClick?: () => void;
  className?: string;
}

/**
 * Card de trilha para exibição no dashboard.
 */
export const TrailCard = ({ trail, onClick, className }: TrailCardProps) => {
  const isReady = trail.status === 'READY';
  const isGenerating = trail.status === 'GENERATING' || trail.status === 'PARTIAL';

  const progressPercentage = trail.progressPercentage ?? 0;

  const formatTimeSpent = (minutes: number): string => {
    if (minutes === 0) return '0min';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}min`;
    }
    return `${mins}min`;
  };

  return (
    <div
      className={cn(
        'bg-white rounded-2xl p-6 shadow-sm border border-gray-100',
        'hover:shadow-md transition-shadow duration-200 cursor-pointer',
        className
      )}
      onClick={onClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && onClick?.()}
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          {trail.languageFlag && (
            <span className="text-2xl">{trail.languageFlag}</span>
          )}
          <div>
            <h3 className="font-semibold text-text">{trail.languageName}</h3>
            <span className="text-sm text-textLight">Nível {trail.levelCode}</span>
          </div>
        </div>
        <StatusBadge status={trail.status} />
      </div>

      {/* Progress Bar */}
      {isReady && (
        <div className="mb-4">
          <div className="flex justify-between text-sm mb-1">
            <span className="text-textLight">Progresso</span>
            <span className="font-medium text-text">
              {progressPercentage.toFixed(0)}%
            </span>
          </div>
          <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
            <div
              className="h-full bg-primary rounded-full transition-all duration-500"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
        </div>
      )}

      {/* Generating State */}
      {isGenerating && (
        <div className="mb-4">
          <div className="flex items-center gap-2 text-sm text-textLight">
            <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            <span>Gerando sua trilha personalizada...</span>
          </div>
        </div>
      )}

      {/* Stats */}
      {isReady && (
        <div className="grid grid-cols-3 gap-4 pt-4 border-t border-gray-100">
          <div className="text-center">
            <p className="text-lg font-semibold text-text">
              {trail.lessonsCompleted}/{trail.totalLessons}
            </p>
            <p className="text-xs text-textLight">Lições</p>
          </div>
          <div className="text-center">
            <p className="text-lg font-semibold text-text">
              {trail.averageScore?.toFixed(0) ?? '-'}
            </p>
            <p className="text-xs text-textLight">Score</p>
          </div>
          <div className="text-center">
            <p className="text-lg font-semibold text-text">
              {formatTimeSpent(trail.timeSpentMinutes)}
            </p>
            <p className="text-xs text-textLight">Tempo</p>
          </div>
        </div>
      )}
    </div>
  );
};

/**
 * Badge de status da trilha.
 */
const StatusBadge = ({ status }: { status: TrailSummary['status'] }) => {
  const config = {
    GENERATING: {
      label: 'Gerando',
      className: 'bg-yellow-100 text-yellow-800',
    },
    PARTIAL: {
      label: 'Parcial',
      className: 'bg-orange-100 text-orange-800',
    },
    READY: {
      label: 'Pronta',
      className: 'bg-green-100 text-green-800',
    },
    ARCHIVED: {
      label: 'Arquivada',
      className: 'bg-gray-100 text-gray-600',
    },
  };

  const { label, className } = config[status];

  return (
    <span
      className={cn(
        'px-2 py-1 rounded-full text-xs font-medium',
        className
      )}
    >
      {label}
    </span>
  );
};

export default TrailCard;
