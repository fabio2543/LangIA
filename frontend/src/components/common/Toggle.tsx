import { type InputHTMLAttributes } from 'react';
import { cn } from '../../utils/cn';

interface ToggleProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'size'> {
  label?: string;
  description?: string;
  size?: 'sm' | 'md' | 'lg';
  labelPosition?: 'left' | 'right';
}

const sizeStyles = {
  sm: {
    track: 'w-8 h-5',
    thumb: 'w-3.5 h-3.5',
    translate: 'translate-x-3.5',
  },
  md: {
    track: 'w-11 h-6',
    thumb: 'w-4 h-4',
    translate: 'translate-x-5',
  },
  lg: {
    track: 'w-14 h-7',
    thumb: 'w-5 h-5',
    translate: 'translate-x-7',
  },
};

export const Toggle = ({
  label,
  description,
  size = 'md',
  labelPosition = 'right',
  className,
  id,
  checked,
  ...props
}: ToggleProps) => {
  const toggleId = id || label?.toLowerCase().replace(/\s+/g, '-');

  const toggleElement = (
    <label
      htmlFor={toggleId}
      className={cn(
        'relative inline-flex items-center cursor-pointer',
        sizeStyles[size].track
      )}
    >
      <input
        id={toggleId}
        type="checkbox"
        checked={checked}
        className="sr-only peer"
        {...props}
      />
      <div
        className={cn(
          'w-full h-full rounded-full transition-colors duration-200',
          'bg-gray-200 peer-checked:bg-primary',
          'peer-focus:ring-2 peer-focus:ring-primary/20 peer-focus:ring-offset-2'
        )}
      />
      <div
        className={cn(
          'absolute bg-white rounded-full shadow transition-transform duration-200',
          'left-1 top-1/2 -translate-y-1/2',
          checked ? sizeStyles[size].translate : 'translate-x-0',
          sizeStyles[size].thumb
        )}
      />
    </label>
  );

  if (!label && !description) {
    return toggleElement;
  }

  return (
    <div
      className={cn(
        'flex items-start gap-3',
        labelPosition === 'left' && 'flex-row-reverse',
        className
      )}
    >
      {toggleElement}
      <div className="flex flex-col">
        {label && (
          <span className="font-medium text-text">{label}</span>
        )}
        {description && (
          <span className="text-sm text-text-light mt-0.5">{description}</span>
        )}
      </div>
    </div>
  );
};
