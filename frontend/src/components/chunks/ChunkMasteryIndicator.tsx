import { cn } from '../../utils/cn';

interface ChunkMasteryIndicatorProps {
  level: number; // 0-5
  size?: 'sm' | 'md' | 'lg';
  showLabel?: boolean;
  className?: string;
}

const MASTERY_LABELS = [
  'Novo',
  'Iniciante',
  'Aprendendo',
  'Praticando',
  'Dominando',
  'Dominado',
];

const MASTERY_COLORS = [
  'bg-gray-200',
  'bg-red-400',
  'bg-orange-400',
  'bg-yellow-400',
  'bg-lime-400',
  'bg-green-500',
];

export const ChunkMasteryIndicator = ({
  level,
  size = 'md',
  showLabel = false,
  className,
}: ChunkMasteryIndicatorProps) => {
  const normalizedLevel = Math.max(0, Math.min(5, Math.round(level)));

  const sizeClasses = {
    sm: 'h-1.5 w-16 gap-0.5',
    md: 'h-2 w-24 gap-1',
    lg: 'h-3 w-32 gap-1.5',
  };

  const dotSizes = {
    sm: 'w-2.5 h-1.5',
    md: 'w-4 h-2',
    lg: 'w-5 h-3',
  };

  return (
    <div className={cn('flex items-center gap-2', className)}>
      <div className={cn('flex rounded-full overflow-hidden', sizeClasses[size])}>
        {Array.from({ length: 5 }).map((_, index) => (
          <div
            key={index}
            className={cn(
              dotSizes[size],
              'rounded-sm transition-colors',
              index < normalizedLevel ? MASTERY_COLORS[normalizedLevel] : 'bg-gray-200'
            )}
          />
        ))}
      </div>
      {showLabel && (
        <span
          className={cn(
            'text-text-light',
            size === 'sm' && 'text-xs',
            size === 'md' && 'text-sm',
            size === 'lg' && 'text-base'
          )}
        >
          {MASTERY_LABELS[normalizedLevel]}
        </span>
      )}
    </div>
  );
};
