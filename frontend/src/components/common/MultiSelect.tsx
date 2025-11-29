import { useState, useRef, useEffect } from 'react';
import { cn } from '../../utils/cn';

interface MultiSelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

interface MultiSelectProps {
  label?: string;
  error?: string;
  options: MultiSelectOption[];
  value: string[];
  onChange: (value: string[]) => void;
  placeholder?: string;
  maxItems?: number;
  disabled?: boolean;
  className?: string;
}

export const MultiSelect = ({
  label,
  error,
  options,
  value,
  onChange,
  placeholder = 'Selecione...',
  maxItems,
  disabled = false,
  className,
}: MultiSelectProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const toggleOption = (optionValue: string) => {
    if (disabled) return;

    if (value.includes(optionValue)) {
      onChange(value.filter((v) => v !== optionValue));
    } else {
      if (maxItems && value.length >= maxItems) return;
      onChange([...value, optionValue]);
    }
  };

  const removeTag = (optionValue: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!disabled) {
      onChange(value.filter((v) => v !== optionValue));
    }
  };

  const selectedLabels = value
    .map((v) => options.find((o) => o.value === v)?.label)
    .filter(Boolean);

  return (
    <div className={cn('w-full relative', className)} ref={containerRef}>
      {label && (
        <label className="block text-sm font-medium text-text mb-2">
          {label}
        </label>
      )}
      <div
        onClick={() => !disabled && setIsOpen(!isOpen)}
        className={cn(
          'min-h-[52px] px-5 py-3 rounded-full border border-gray-200 bg-white cursor-pointer transition-colors duration-200',
          'focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/20',
          error && 'border-red-400',
          disabled && 'bg-gray-50 cursor-not-allowed opacity-60',
          isOpen && 'border-primary ring-2 ring-primary/20'
        )}
      >
        <div className="flex flex-wrap gap-2 items-center">
          {selectedLabels.length > 0 ? (
            selectedLabels.map((label, index) => (
              <span
                key={value[index]}
                className="inline-flex items-center gap-1 px-3 py-1 bg-primary text-white rounded-full text-sm"
              >
                {label}
                <button
                  type="button"
                  onClick={(e) => removeTag(value[index], e)}
                  className="hover:text-white/80"
                  disabled={disabled}
                >
                  <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </span>
            ))
          ) : (
            <span className="text-text-light">{placeholder}</span>
          )}
        </div>
      </div>

      {isOpen && !disabled && (
        <ul className="absolute z-50 left-0 right-0 mt-2 max-h-60 overflow-auto bg-white rounded-2xl shadow-lg border border-gray-200">
          {options.map((option) => {
            const isSelected = value.includes(option.value);
            const isDisabledOption = option.disabled || (maxItems !== undefined && !isSelected && value.length >= maxItems);

            return (
              <li
                key={option.value}
                onClick={() => !isDisabledOption && toggleOption(option.value)}
                className={cn(
                  'px-5 py-3 cursor-pointer transition-colors duration-150 flex items-center justify-between',
                  'first:rounded-t-2xl last:rounded-b-2xl',
                  isSelected ? 'bg-primary text-white' : 'text-text hover:bg-primary-light',
                  isDisabledOption && 'opacity-50 cursor-not-allowed'
                )}
              >
                <span>{option.label}</span>
                {isSelected && (
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                )}
              </li>
            );
          })}
        </ul>
      )}

      {error && (
        <p className="mt-1.5 text-sm text-red-500">{error}</p>
      )}
      {maxItems && (
        <p className="mt-1.5 text-sm text-text-light">
          {value.length}/{maxItems} selecionados
        </p>
      )}
    </div>
  );
};
