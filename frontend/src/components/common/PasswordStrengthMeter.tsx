import { type PasswordStrength } from '../../hooks/usePasswordValidation';
import { useTranslation } from '../../i18n';
import { cn } from '../../utils/cn';

interface PasswordStrengthMeterProps {
  strength: PasswordStrength;
  criteriaCount: number;
}

export const PasswordStrengthMeter = ({ strength, criteriaCount }: PasswordStrengthMeterProps) => {
  const { t } = useTranslation();

  const strengthConfig = {
    weak: {
      color: 'bg-red-500',
      width: 'w-1/3',
      label: t.auth.resetPassword.strengthWeak,
      textColor: 'text-red-600',
    },
    medium: {
      color: 'bg-yellow-500',
      width: 'w-2/3',
      label: t.auth.resetPassword.strengthMedium,
      textColor: 'text-yellow-600',
    },
    strong: {
      color: 'bg-green-500',
      width: 'w-full',
      label: t.auth.resetPassword.strengthStrong,
      textColor: 'text-green-600',
    },
  };

  const config = strengthConfig[strength];

  // Don't show anything if password is empty
  if (criteriaCount === 0) {
    return null;
  }

  return (
    <div className="space-y-1.5">
      {/* Progress bar */}
      <div className="h-1.5 bg-gray-200 rounded-full overflow-hidden">
        <div
          className={cn(
            'h-full rounded-full transition-all duration-300',
            config.color,
            config.width
          )}
        />
      </div>

      {/* Label */}
      <p className={cn('text-xs font-medium', config.textColor)}>
        {config.label}
      </p>
    </div>
  );
};
