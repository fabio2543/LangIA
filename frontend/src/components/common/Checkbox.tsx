import { type InputHTMLAttributes } from 'react';
import { cn } from '../../utils/cn';

interface CheckboxProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'size'> {
  label?: string;
  description?: string;
  error?: string;
  size?: 'sm' | 'md' | 'lg';
}

const sizeStyles = {
  sm: 'w-4 h-4',
  md: 'w-5 h-5',
  lg: 'w-6 h-6',
};

const labelSizeStyles = {
  sm: 'text-sm',
  md: 'text-base',
  lg: 'text-base',
};

export const Checkbox = ({
  label,
  description,
  error,
  size = 'md',
  className,
  id,
  checked,
  ...props
}: CheckboxProps) => {
  const checkboxId = id || label?.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className={cn('flex items-start gap-3', className)}>
      <div className="relative flex items-center">
        <input
          id={checkboxId}
          type="checkbox"
          checked={checked}
          className={cn(
            'appearance-none rounded border-2 border-gray-300 bg-white transition-colors duration-200 cursor-pointer',
            'checked:bg-primary checked:border-primary',
            'focus:outline-none focus:ring-2 focus:ring-primary/20 focus:ring-offset-2',
            'hover:border-primary/50',
            error && 'border-red-400',
            sizeStyles[size]
          )}
          {...props}
        />
        {checked && (
          <svg
            className={cn(
              'absolute pointer-events-none text-white',
              size === 'sm' ? 'w-3 h-3 left-0.5 top-0.5' : '',
              size === 'md' ? 'w-3.5 h-3.5 left-0.5 top-0.5' : '',
              size === 'lg' ? 'w-4 h-4 left-1 top-1' : ''
            )}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={3}
              d="M5 13l4 4L19 7"
            />
          </svg>
        )}
      </div>
      {(label || description) && (
        <div className="flex flex-col">
          {label && (
            <label
              htmlFor={checkboxId}
              className={cn(
                'font-medium text-text cursor-pointer',
                labelSizeStyles[size]
              )}
            >
              {label}
            </label>
          )}
          {description && (
            <span className="text-sm text-text-light mt-0.5">{description}</span>
          )}
          {error && (
            <span className="text-sm text-red-500 mt-1">{error}</span>
          )}
        </div>
      )}
    </div>
  );
};
