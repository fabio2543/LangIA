import { cn } from '../../utils/cn';
import type { Lesson } from '../../types/trail';

interface LessonCardProps {
  lesson: Lesson;
  onClick?: () => void;
  className?: string;
  showDetails?: boolean;
}

/**
 * Card de lição para exibição na lista de módulos.
 */
export const LessonCard = ({
  lesson,
  onClick,
  className,
  showDetails = false,
}: LessonCardProps) => {
  const isCompleted = !!lesson.completedAt;
  const isPlaceholder = lesson.isPlaceholder;

  const lessonTypeConfig: Record<
    Lesson['type'],
    { icon: string; label: string; color: string }
  > = {
    interactive: { icon: '🎮', label: 'Interativo', color: 'bg-purple-100 text-purple-600' },
    video: { icon: '🎬', label: 'Vídeo', color: 'bg-red-100 text-red-600' },
    reading: { icon: '📖', label: 'Leitura', color: 'bg-blue-100 text-blue-600' },
    exercise: { icon: '✏️', label: 'Exercício', color: 'bg-yellow-100 text-yellow-600' },
    conversation: { icon: '💬', label: 'Conversação', color: 'bg-green-100 text-green-600' },
    flashcard: { icon: '🃏', label: 'Flashcard', color: 'bg-orange-100 text-orange-600' },
    game: { icon: '🎯', label: 'Jogo', color: 'bg-pink-100 text-pink-600' },
  };

  const typeConfig = lessonTypeConfig[lesson.type];

  return (
    <div
      className={cn(
        'bg-white rounded-xl p-4 border',
        isCompleted ? 'border-green-200' : 'border-gray-100',
        !isPlaceholder && 'hover:shadow-sm transition-shadow cursor-pointer',
        isPlaceholder && 'opacity-60',
        className
      )}
      onClick={!isPlaceholder ? onClick : undefined}
      role={!isPlaceholder ? 'button' : undefined}
      tabIndex={!isPlaceholder ? 0 : undefined}
      onKeyDown={(e) => !isPlaceholder && e.key === 'Enter' && onClick?.()}
    >
      <div className="flex items-center gap-4">
        {/* Status/Type Icon */}
        <div
          className={cn(
            'w-10 h-10 rounded-lg flex items-center justify-center',
            isCompleted ? 'bg-green-100' : typeConfig.color.split(' ')[0]
          )}
        >
          {isCompleted ? (
            <span className="text-green-600 text-lg">✓</span>
          ) : (
            <span className="text-lg">{typeConfig.icon}</span>
          )}
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <h4 className={cn(
              'font-medium truncate',
              isCompleted ? 'text-green-600' : 'text-text'
            )}>
              {lesson.title}
            </h4>
            {isPlaceholder && (
              <span className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded">
                Em breve
              </span>
            )}
          </div>
          <div className="flex items-center gap-3 mt-1">
            <span className={cn('text-xs px-2 py-0.5 rounded', typeConfig.color)}>
              {typeConfig.label}
            </span>
            <span className="text-xs text-textLight">
              {lesson.durationMinutes} min
            </span>
          </div>
        </div>

        {/* Score */}
        {showDetails && isCompleted && lesson.score !== null && (
          <div className="text-right">
            <p className="text-lg font-semibold text-primary">
              {lesson.score.toFixed(0)}
            </p>
            <p className="text-xs text-textLight">Score</p>
          </div>
        )}

        {/* Arrow */}
        {!isPlaceholder && !isCompleted && (
          <svg
            className="w-5 h-5 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 5l7 7-7 7"
            />
          </svg>
        )}
      </div>

      {/* Details */}
      {showDetails && isCompleted && (
        <div className="mt-3 pt-3 border-t border-gray-100 flex items-center justify-between text-sm text-textLight">
          <span>
            Completada em {formatDate(lesson.completedAt!)}
          </span>
          {lesson.timeSpentSeconds && (
            <span>
              Tempo: {formatTimeSpent(lesson.timeSpentSeconds)}
            </span>
          )}
        </div>
      )}
    </div>
  );
};

/**
 * Formata data para exibição.
 */
const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short',
  });
};

/**
 * Formata tempo em segundos para exibição.
 */
const formatTimeSpent = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60);
  if (minutes < 1) return '<1min';
  if (minutes < 60) return `${minutes}min`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}h ${mins}min`;
};

export default LessonCard;
