import { cn } from '../../utils/cn';

interface SrsProgressBarProps {
  current: number;
  total: number;
  className?: string;
}

export const SrsProgressBar = ({ current, total, className }: SrsProgressBarProps) => {
  const progress = total > 0 ? Math.round((current / total) * 100) : 0;

  return (
    <div className={cn('w-full', className)}>
      <div className="flex justify-between items-center mb-2">
        <span className="text-sm text-text-light">
          {current} de {total} cards
        </span>
        <span className="text-sm font-medium text-primary">{progress}%</span>
      </div>
      <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
        <div
          className="h-full bg-gradient-to-r from-primary to-primary-dark rounded-full transition-all duration-500 ease-out"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
};
