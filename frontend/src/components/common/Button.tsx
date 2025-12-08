import { type ButtonHTMLAttributes, type ReactNode } from 'react';
import { cn } from '../../utils/cn';

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'success' | 'streak';
type ButtonSize = 'sm' | 'md' | 'lg' | 'xl';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  children: ReactNode;
  fullWidth?: boolean;
  pill?: boolean;
  icon?: ReactNode;
  iconPosition?: 'left' | 'right';
}

const variantStyles: Record<ButtonVariant, string> = {
  primary:
    'bg-primary text-white hover:bg-primary-dark shadow-primary hover:shadow-lg active:scale-[0.98]',
  secondary:
    'bg-white text-primary border border-primary hover:bg-primary-light active:scale-[0.98]',
  outline: 'bg-transparent border border-white text-white hover:bg-white/10 active:scale-[0.98]',
  ghost: 'bg-transparent text-primary hover:bg-primary-light active:scale-[0.98]',
  success: 'bg-success text-white hover:bg-success/90 shadow-sm active:scale-[0.98]',
  streak:
    'bg-streak text-white hover:bg-streak/90 shadow-sm active:scale-[0.98]',
};

const sizeStyles: Record<ButtonSize, string> = {
  sm: 'px-4 py-2 text-sm gap-1.5',
  md: 'px-5 py-2.5 text-sm gap-2',
  lg: 'px-8 py-3.5 text-base gap-2',
  xl: 'px-10 py-4 text-lg gap-3',
};

export const Button = ({
  variant = 'primary',
  size = 'md',
  children,
  fullWidth = false,
  pill = true,
  icon,
  iconPosition = 'left',
  className,
  disabled,
  ...props
}: ButtonProps) => {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center font-semibold transition-all duration-200 cursor-pointer',
        pill ? 'rounded-full' : 'rounded-xl',
        variantStyles[variant],
        sizeStyles[size],
        fullWidth && 'w-full',
        disabled && 'opacity-50 cursor-not-allowed pointer-events-none',
        className
      )}
      disabled={disabled}
      {...props}
    >
      {icon && iconPosition === 'left' && <span className="flex-shrink-0">{icon}</span>}
      {children}
      {icon && iconPosition === 'right' && <span className="flex-shrink-0">{icon}</span>}
    </button>
  );
};
