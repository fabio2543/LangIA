import { useState } from 'react';
import { cn } from '../../utils/cn';

type AvatarSize = 'sm' | 'md' | 'lg' | 'xl';

interface AvatarProps {
  src?: string;
  fallback?: string;
  alt: string;
  size?: AvatarSize;
  className?: string;
}

const sizeStyles: Record<AvatarSize, string> = {
  sm: 'w-8 h-8 text-sm',
  md: 'w-10 h-10 text-base',
  lg: 'w-[70px] h-[70px] text-2xl',
  xl: 'w-[280px] h-[320px] text-[100px]',
};

export const Avatar = ({
  src,
  fallback = '👤',
  alt,
  size = 'md',
  className,
}: AvatarProps) => {
  const [hasError, setHasError] = useState(false);

  if (!src || hasError) {
    return (
      <div
        className={cn(
          'flex items-center justify-center bg-primary-light rounded-2xl',
          sizeStyles[size],
          className
        )}
        role="img"
        aria-label={alt}
      >
        {fallback}
      </div>
    );
  }

  return (
    <img
      src={src}
      alt={alt}
      onError={() => setHasError(true)}
      className={cn(
        'object-cover rounded-2xl',
        sizeStyles[size],
        className
      )}
    />
  );
};
