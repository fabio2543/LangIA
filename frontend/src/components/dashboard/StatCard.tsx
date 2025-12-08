import { cn } from '../../utils/cn';

interface StatCardProps {
  icon: string;
  value: string | number;
  label: string;
  variant?: 'default' | 'streak' | 'xp' | 'progress';
  sublabel?: string;
  className?: string;
}

const variantStyles = {
  default: 'bg-white',
  streak: 'bg-streak-light',
  xp: 'bg-primary-light',
  progress: 'bg-success-light',
};

const iconBgStyles = {
  default: 'bg-gray-100',
  streak: 'bg-white',
  xp: 'bg-white',
  progress: 'bg-white',
};

const valueStyles = {
  default: 'text-text',
  streak: 'text-streak',
  xp: 'text-primary',
  progress: 'text-success',
};

export const StatCard = ({
  icon,
  value,
  label,
  variant = 'default',
  sublabel,
  className,
}: StatCardProps) => {
  return (
    <div
      className={cn(
        'p-4 rounded-2xl shadow-card card-hover',
        variantStyles[variant],
        className
      )}
    >
      <div className="flex items-center gap-3">
        <div
          className={cn(
            'w-12 h-12 rounded-xl flex items-center justify-center text-2xl',
            iconBgStyles[variant]
          )}
        >
          {icon}
        </div>
        <div>
          <p className={cn('text-2xl font-bold', valueStyles[variant])}>
            {value}
            {sublabel && (
              <span className="text-sm font-normal text-text-light ml-1">
                {sublabel}
              </span>
            )}
          </p>
          <p className="text-sm text-text-light">{label}</p>
        </div>
      </div>
    </div>
  );
};
