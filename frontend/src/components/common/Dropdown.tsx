import { useState, useRef, useEffect, type ReactNode } from 'react';
import { cn } from '../../utils/cn';

interface DropdownOption {
  value: string;
  label: string;
  disabled?: boolean;
}

interface DropdownProps {
  label?: string;
  error?: string;
  options: DropdownOption[];
  placeholder?: string;
  value?: string;
  onChange?: (value: string) => void;
  size?: 'sm' | 'md' | 'lg';
  icon?: ReactNode;
  disabled?: boolean;
  className?: string;
}

const sizeStyles = {
  sm: 'px-4 py-2 text-sm',
  md: 'px-5 py-3 text-base',
  lg: 'px-6 py-4 text-base',
};

export const Dropdown = ({
  label,
  error,
  options,
  placeholder = 'Selecione uma opção',
  value,
  onChange,
  size = 'lg',
  icon,
  disabled = false,
  className,
}: DropdownProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const containerRef = useRef<HTMLDivElement>(null);
  const listRef = useRef<HTMLUListElement>(null);

  const selectedOption = options.find((opt) => opt.value === value);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Handle keyboard navigation
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (disabled) return;

    switch (e.key) {
      case 'Enter':
      case ' ':
        e.preventDefault();
        if (isOpen && highlightedIndex >= 0) {
          const option = options[highlightedIndex];
          if (!option.disabled) {
            onChange?.(option.value);
            setIsOpen(false);
          }
        } else {
          setIsOpen(true);
        }
        break;
      case 'ArrowDown':
        e.preventDefault();
        if (!isOpen) {
          setIsOpen(true);
        } else {
          setHighlightedIndex((prev) => {
            const next = prev + 1;
            return next >= options.length ? 0 : next;
          });
        }
        break;
      case 'ArrowUp':
        e.preventDefault();
        if (isOpen) {
          setHighlightedIndex((prev) => {
            const next = prev - 1;
            return next < 0 ? options.length - 1 : next;
          });
        }
        break;
      case 'Escape':
        setIsOpen(false);
        break;
    }
  };

  // Scroll to highlighted option
  useEffect(() => {
    if (isOpen && listRef.current && highlightedIndex >= 0) {
      const highlightedEl = listRef.current.children[highlightedIndex] as HTMLElement;
      if (highlightedEl) {
        highlightedEl.scrollIntoView({ block: 'nearest' });
      }
    }
  }, [highlightedIndex, isOpen]);

  const handleSelect = (option: DropdownOption) => {
    if (option.disabled) return;
    onChange?.(option.value);
    setIsOpen(false);
  };

  return (
    <div className="w-full" ref={containerRef}>
      {label && (
        <label className="block text-sm font-medium text-text mb-2">
          {label}
        </label>
      )}
      <div className="relative">
        {/* Trigger Button */}
        <button
          type="button"
          onClick={() => !disabled && setIsOpen(!isOpen)}
          onKeyDown={handleKeyDown}
          disabled={disabled}
          className={cn(
            'w-full rounded-full border border-gray-200 bg-white text-left transition-all duration-200 cursor-pointer',
            'focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20',
            error && 'border-red-400 focus:border-red-400 focus:ring-red-400/20',
            disabled && 'opacity-50 cursor-not-allowed bg-gray-50',
            isOpen && 'border-primary ring-2 ring-primary/20',
            icon ? 'pl-12 pr-12' : 'pl-5 pr-12',
            sizeStyles[size],
            className
          )}
        >
          {icon && (
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-text-light pointer-events-none">
              {icon}
            </span>
          )}
          <span className={cn(selectedOption ? 'text-text' : 'text-text-light')}>
            {selectedOption?.label || placeholder}
          </span>
          <span className={cn(
            'absolute right-4 top-1/2 -translate-y-1/2 text-text-light pointer-events-none transition-transform duration-200',
            isOpen && 'rotate-180'
          )}>
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </span>
        </button>

        {/* Dropdown Menu */}
        {isOpen && (
          <ul
            ref={listRef}
            className="absolute z-50 w-full mt-2 bg-white border border-gray-200 rounded-2xl shadow-lg max-h-60 overflow-auto"
            role="listbox"
          >
            {options.map((option, index) => (
              <li
                key={option.value}
                role="option"
                aria-selected={option.value === value}
                onClick={() => handleSelect(option)}
                onMouseEnter={() => setHighlightedIndex(index)}
                className={cn(
                  'px-5 py-3 cursor-pointer transition-colors duration-150',
                  'first:rounded-t-2xl last:rounded-b-2xl',
                  option.value === value && 'bg-primary text-white',
                  option.value !== value && highlightedIndex === index && 'bg-primary-light',
                  option.value !== value && highlightedIndex !== index && 'text-text hover:bg-gray-50',
                  option.disabled && 'opacity-50 cursor-not-allowed text-text-light'
                )}
              >
                {option.label}
              </li>
            ))}
          </ul>
        )}
      </div>
      {error && (
        <p className="mt-1.5 text-sm text-red-500">{error}</p>
      )}
    </div>
  );
};
