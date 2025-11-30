import { useMemo } from 'react';

export interface PasswordValidationResult {
  minLength: boolean;
  hasUppercase: boolean;
  hasLowercase: boolean;
  hasNumber: boolean;
  hasSpecial: boolean;
}

export type PasswordStrength = 'weak' | 'medium' | 'strong';

export interface UsePasswordValidationReturn {
  validationResults: PasswordValidationResult;
  strength: PasswordStrength;
  isValid: boolean;
  criteriaCount: number;
}

const MIN_PASSWORD_LENGTH = 8;

export const usePasswordValidation = (password: string): UsePasswordValidationReturn => {
  return useMemo(() => {
    const validationResults: PasswordValidationResult = {
      minLength: password.length >= MIN_PASSWORD_LENGTH,
      hasUppercase: /[A-Z]/.test(password),
      hasLowercase: /[a-z]/.test(password),
      hasNumber: /\d/.test(password),
      hasSpecial: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password),
    };

    const criteriaCount = Object.values(validationResults).filter(Boolean).length;

    let strength: PasswordStrength;
    if (criteriaCount <= 2) {
      strength = 'weak';
    } else if (criteriaCount <= 4) {
      strength = 'medium';
    } else {
      strength = 'strong';
    }

    const isValid = Object.values(validationResults).every(Boolean);

    return {
      validationResults,
      strength,
      isValid,
      criteriaCount,
    };
  }, [password]);
};
