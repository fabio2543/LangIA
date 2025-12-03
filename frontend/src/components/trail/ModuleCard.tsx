import { cn } from '../../utils/cn';
import type { TrailModule } from '../../types/trail';

interface ModuleCardProps {
  module: TrailModule;
  onClick?: () => void;
  className?: string;
}

/**
 * Card de módulo para exibição na página de trilha.
 */
export const ModuleCard = ({ module, onClick, className }: ModuleCardProps) => {
  const isReady = module.status === 'READY';
  const totalLessons = module.lessons.length;
  const completedLessons = module.lessons.filter((l) => l.completedAt).length;
  const progressPercentage = totalLessons > 0
    ? (completedLessons / totalLessons) * 100
    : 0;

  const competencyIcons: Record<string, string> = {
    speaking: '🗣️',
    listening: '👂',
    reading: '📖',
    writing: '✍️',
    grammar: '📝',
    vocabulary: '📚',
    pronunciation: '🎤',
  };

  const icon = competencyIcons[module.competencyCode] ?? '📘';

  return (
    <div
      className={cn(
        'bg-white rounded-2xl p-6 shadow-sm border border-gray-100',
        isReady && 'hover:shadow-md transition-shadow duration-200 cursor-pointer',
        !isReady && 'opacity-60',
        className
      )}
      onClick={isReady ? onClick : undefined}
      role={isReady ? 'button' : undefined}
      tabIndex={isReady ? 0 : undefined}
      onKeyDown={(e) => isReady && e.key === 'Enter' && onClick?.()}
    >
      {/* Header */}
      <div className="flex items-start gap-4 mb-4">
        <div className="w-12 h-12 bg-primary-light rounded-xl flex items-center justify-center text-2xl">
          {icon}
        </div>
        <div className="flex-1">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-text">{module.title}</h3>
            <span className="text-sm text-textLight">
              {module.orderIndex + 1}
            </span>
          </div>
          <p className="text-sm text-textLight mt-1">{module.competencyName}</p>
        </div>
      </div>

      {/* Description */}
      {module.description && (
        <p className="text-sm text-textLight mb-4 line-clamp-2">
          {module.description}
        </p>
      )}

      {/* Progress */}
      {isReady && (
        <div className="mb-4">
          <div className="flex justify-between text-sm mb-1">
            <span className="text-textLight">
              {completedLessons} de {totalLessons} lições
            </span>
            <span className="font-medium text-text">
              {progressPercentage.toFixed(0)}%
            </span>
          </div>
          <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
            <div
              className="h-full bg-primary rounded-full transition-all duration-300"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
        </div>
      )}

      {/* Pending State */}
      {!isReady && (
        <div className="flex items-center gap-2 text-sm text-textLight">
          <div className="w-4 h-4 border-2 border-gray-300 border-t-transparent rounded-full animate-spin" />
          <span>Gerando conteúdo...</span>
        </div>
      )}

      {/* Lessons Preview */}
      {isReady && module.lessons.length > 0 && (
        <div className="pt-4 border-t border-gray-100">
          <p className="text-xs text-textLight mb-2 uppercase tracking-wide">
            Lições
          </p>
          <div className="flex flex-wrap gap-1">
            {module.lessons.slice(0, 8).map((lesson, index) => (
              <LessonDot
                key={lesson.id}
                completed={!!lesson.completedAt}
                index={index + 1}
              />
            ))}
            {module.lessons.length > 8 && (
              <span className="text-xs text-textLight ml-1">
                +{module.lessons.length - 8}
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

/**
 * Indicador de lição (círculo colorido).
 */
const LessonDot = ({
  completed,
  index,
}: {
  completed: boolean;
  index: number;
}) => (
  <div
    className={cn(
      'w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium',
      completed
        ? 'bg-green-100 text-green-600'
        : 'bg-gray-100 text-gray-400'
    )}
    title={`Lição ${index}`}
  >
    {completed ? '✓' : index}
  </div>
);

export default ModuleCard;
