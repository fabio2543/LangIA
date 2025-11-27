import { type ReactNode } from 'react';
import { cn } from '../../utils/cn';

type BadgeVariant = 'primary' | 'accent' | 'light';

interface BadgeProps {
  variant?: BadgeVariant;
  children: ReactNode;
  className?: string;
}

const variantStyles: Record<BadgeVariant, string> = {
  primary: 'bg-primary text-white',
  accent: 'bg-primary-light text-primary',
  light: 'bg-white text-text',
};

export const Badge = ({
  variant = 'primary',
  children,
  className,
}: BadgeProps) => {
  return (
    <span
      className={cn(
        'inline-flex items-center px-3.5 py-1.5 rounded-full text-xs font-semibold',
        variantStyles[variant],
        className
      )}
    >
      {children}
    </span>
  );
};
