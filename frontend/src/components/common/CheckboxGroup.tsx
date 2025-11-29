import { cn } from '../../utils/cn';
import { Checkbox } from './Checkbox';

interface CheckboxOption {
  value: string;
  label: string;
  description?: string;
  disabled?: boolean;
}

interface CheckboxGroupProps {
  label?: string;
  error?: string;
  options: CheckboxOption[];
  value: string[];
  onChange: (value: string[]) => void;
  direction?: 'horizontal' | 'vertical';
  columns?: 1 | 2 | 3 | 4;
  className?: string;
}

export const CheckboxGroup = ({
  label,
  error,
  options,
  value,
  onChange,
  direction = 'vertical',
  columns = 1,
  className,
}: CheckboxGroupProps) => {
  const toggleOption = (optionValue: string) => {
    if (value.includes(optionValue)) {
      onChange(value.filter((v) => v !== optionValue));
    } else {
      onChange([...value, optionValue]);
    }
  };

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
        role="group"
        aria-label={label}
      >
        {options.map((option) => (
          <Checkbox
            key={option.value}
            label={option.label}
            description={option.description}
            checked={value.includes(option.value)}
            onChange={() => toggleOption(option.value)}
            disabled={option.disabled}
          />
        ))}
      </div>
      {error && (
        <p className="mt-2 text-sm text-red-500">{error}</p>
      )}
    </div>
  );
};
