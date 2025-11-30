import { cn } from '../../utils/cn';

interface RadioOption {
  value: string;
  label: string;
  description?: string;
  disabled?: boolean;
}

interface RadioGroupProps {
  label?: string;
  error?: string;
  options: RadioOption[];
  value: string;
  onChange: (value: string) => void;
  name: string;
  direction?: 'horizontal' | 'vertical';
  columns?: 1 | 2 | 3 | 4;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeStyles = {
  sm: 'w-4 h-4',
  md: 'w-5 h-5',
  lg: 'w-6 h-6',
};

const dotSizeStyles = {
  sm: 'w-2 h-2',
  md: 'w-2.5 h-2.5',
  lg: 'w-3 h-3',
};

export const RadioGroup = ({
  label,
  error,
  options,
  value,
  onChange,
  name,
  direction = 'vertical',
  columns = 1,
  size = 'md',
  className,
}: RadioGroupProps) => {
  const gridStyles = {
    1: 'grid-cols-1',
    2: 'grid-cols-2',
    3: 'grid-cols-3',
    4: 'grid-cols-4',
  };

  return (
    <div className={cn('w-full', className)}>
      {label && (
        <label className="block text-sm font-medium text-text mb-3">
          {label}
        </label>
      )}
      <div
        className={cn(
          direction === 'horizontal'
            ? 'flex flex-wrap gap-4'
            : `grid gap-3 ${gridStyles[columns]}`
        )}
        role="radiogroup"
        aria-label={label}
      >
        {options.map((option) => {
          const isSelected = value === option.value;
          const radioId = `${name}-${option.value}`;

          return (
            <label
              key={option.value}
              htmlFor={radioId}
              className={cn(
                'flex items-start gap-3 cursor-pointer',
                option.disabled && 'opacity-50 cursor-not-allowed'
              )}
            >
              <div className="relative flex items-center">
                <input
                  id={radioId}
                  type="radio"
                  name={name}
                  value={option.value}
                  checked={isSelected}
                  onChange={() => !option.disabled && onChange(option.value)}
                  disabled={option.disabled}
                  className="sr-only"
                />
                <div
                  className={cn(
                    'rounded-full border-2 transition-colors duration-200 flex items-center justify-center',
                    isSelected
                      ? 'border-primary bg-primary'
                      : 'border-gray-300 bg-white hover:border-primary/50',
                    error && !isSelected && 'border-red-400',
                    sizeStyles[size]
                  )}
                >
                  {isSelected && (
                    <div className={cn('rounded-full bg-white', dotSizeStyles[size])} />
                  )}
                </div>
              </div>
              <div className="flex flex-col">
                <span className="font-medium text-text">{option.label}</span>
                {option.description && (
                  <span className="text-sm text-text-light mt-0.5">
                    {option.description}
                  </span>
                )}
              </div>
            </label>
          );
        })}
      </div>
      {error && (
        <p className="mt-2 text-sm text-red-500">{error}</p>
      )}
    </div>
  );
};
