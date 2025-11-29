import { type SelectHTMLAttributes, type ReactNode } from 'react';
import { cn } from '../../utils/cn';

interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

interface SelectProps extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'size'> {
  label?: string;
  error?: string;
  options: SelectOption[];
  placeholder?: string;
  size?: 'sm' | 'md' | 'lg';
  icon?: ReactNode;
}

const sizeStyles = {
  sm: 'px-4 py-2 text-sm',
  md: 'px-5 py-3 text-base',
  lg: 'px-6 py-4 text-base',
};

export const Select = ({
  label,
  error,
  options,
  placeholder,
  size = 'lg',
  icon,
  className,
  id,
  value,
  ...props
}: SelectProps) => {
  const selectId = id || label?.toLowerCase().replace(/\s+/g, '-');
  const hasValue = value !== undefined && value !== '';

  return (
    <div className="w-full">
      {label && (
        <label
          htmlFor={selectId}
          className="block text-sm font-medium text-text mb-2"
        >
          {label}
        </label>
      )}
      <div className="relative">
        {icon && (
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-text-light pointer-events-none">
            {icon}
          </span>
        )}
        <select
          id={selectId}
          value={value}
          className={cn(
            'w-full rounded-full border border-gray-200 bg-white text-text transition-colors duration-200 appearance-none cursor-pointer',
            'focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20',
            error && 'border-red-400 focus:border-red-400 focus:ring-red-400/20',
            !hasValue && 'text-text-light',
            icon ? 'pl-12 pr-12' : 'pl-5 pr-12',
            sizeStyles[size],
            className
          )}
          {...props}
        >
          {placeholder && (
            <option value="" disabled className="text-text-light bg-white">
              {placeholder}
            </option>
          )}
          {options.map((option) => (
            <option
              key={option.value}
              value={option.value}
              disabled={option.disabled}
              className="bg-white text-text py-2"
            >
              {option.label}
            </option>
          ))}
        </select>
        <span className="absolute right-4 top-1/2 -translate-y-1/2 text-text-light pointer-events-none">
          <svg
            className="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </span>
      </div>
      {error && (
        <p className="mt-1.5 text-sm text-red-500">{error}</p>
      )}
    </div>
  );
};
