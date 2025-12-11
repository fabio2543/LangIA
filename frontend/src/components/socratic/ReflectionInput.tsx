import { useState, useRef, useEffect } from 'react';
import { cn } from '../../utils/cn';

interface ReflectionInputProps {
  placeholder?: string;
  initialValue?: string;
  disabled?: boolean;
  isLoading?: boolean;
  onSubmit: (reflection: string) => void;
  className?: string;
}

export const ReflectionInput = ({
  placeholder = 'Reflita sobre sua resposta...',
  initialValue = '',
  disabled = false,
  isLoading = false,
  onSubmit,
  className,
}: ReflectionInputProps) => {
  const [value, setValue] = useState(initialValue);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }
  }, [value]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (value.trim() && !disabled && !isLoading) {
      onSubmit(value.trim());
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <form onSubmit={handleSubmit} className={cn('space-y-3', className)}>
      <div className="relative">
        <textarea
          ref={textareaRef}
          value={value}
          onChange={(e) => setValue(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          disabled={disabled || isLoading}
          rows={3}
          className={cn(
            'w-full px-4 py-3 rounded-xl border border-gray-200 resize-none',
            'focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent',
            'placeholder:text-text-light text-text',
            'transition-colors',
            (disabled || isLoading) && 'bg-gray-50 cursor-not-allowed'
          )}
        />
        {isLoading && (
          <div className="absolute right-3 top-3">
            <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          </div>
        )}
      </div>

      <div className="flex items-center justify-between">
        <p className="text-xs text-text-light">
          Shift + Enter para nova linha
        </p>
        <button
          type="submit"
          disabled={!value.trim() || disabled || isLoading}
          className={cn(
            'px-6 py-2 rounded-xl font-medium transition-colors',
            value.trim() && !disabled && !isLoading
              ? 'bg-primary text-white hover:bg-primary-dark'
              : 'bg-gray-100 text-text-light cursor-not-allowed'
          )}
        >
          {isLoading ? 'Enviando...' : 'Enviar'}
        </button>
      </div>
    </form>
  );
};
