import { type TextareaHTMLAttributes } from 'react';
import { cn } from '../../utils/cn';

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  hint?: string;
  showCount?: boolean;
  maxLength?: number;
}

export const Textarea = ({
  label,
  error,
  hint,
  showCount = false,
  maxLength,
  className,
  id,
  value,
  ...props
}: TextareaProps) => {
  const textareaId = id || label?.toLowerCase().replace(/\s+/g, '-');
  const currentLength = typeof value === 'string' ? value.length : 0;

  return (
    <div className="w-full">
      {label && (
        <label
          htmlFor={textareaId}
          className="block text-sm font-medium text-text mb-2"
        >
          {label}
        </label>
      )}
      <textarea
        id={textareaId}
        value={value}
        maxLength={maxLength}
        className={cn(
          'w-full rounded-2xl border border-gray-200 bg-white text-text transition-colors duration-200',
          'placeholder:text-text-light',
          'focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20',
          'resize-none',
          error && 'border-red-400 focus:border-red-400 focus:ring-red-400/20',
          'px-5 py-4 text-base min-h-[120px]',
          className
        )}
        {...props}
      />
      <div className="flex justify-between mt-1.5">
        <div>
          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}
          {!error && hint && (
            <p className="text-sm text-text-light">{hint}</p>
          )}
        </div>
        {showCount && maxLength && (
          <p
            className={cn(
              'text-sm',
              currentLength >= maxLength ? 'text-red-500' : 'text-text-light'
            )}
          >
            {currentLength}/{maxLength}
          </p>
        )}
      </div>
    </div>
  );
};
