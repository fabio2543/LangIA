import { useState, useEffect, useRef, type FormEvent } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { PasswordStrengthMeter } from '../components/common/PasswordStrengthMeter';
import { useTranslation } from '../i18n';
import { usePasswordValidation } from '../hooks/usePasswordValidation';
import { passwordService, handleApiError } from '../services/api';
import { cn } from '../utils/cn';

const REDIRECT_COUNTDOWN_SECONDS = 5;

export const ResetPasswordPage = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token') || '';

  // Token validation state
  const [isValidatingToken, setIsValidatingToken] = useState(true);
  const [isTokenValid, setIsTokenValid] = useState(false);
  const [tokenError, setTokenError] = useState<'invalid' | 'expired' | 'used' | null>(null);
  const [maskedEmail, setMaskedEmail] = useState<string | null>(null);

  // Form state
  const [password, setPassword] = useState('');
  const [passwordConfirmation, setPasswordConfirmation] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Success state
  const [isSuccess, setIsSuccess] = useState(false);
  const [redirectCountdown, setRedirectCountdown] = useState(REDIRECT_COUNTDOWN_SECONDS);

  // Password validation hook
  const { validationResults, strength, isValid: isPasswordValid, criteriaCount } = usePasswordValidation(password);

  // Refs
  const successRef = useRef<HTMLDivElement>(null);
  const formRef = useRef<HTMLFormElement>(null);

  // Validate token on mount
  useEffect(() => {
    const validateToken = async () => {
      if (!token) {
        setTokenError('invalid');
        setIsValidatingToken(false);
        return;
      }

      try {
        const response = await passwordService.validateToken(token);
        if (response.valid && response.maskedEmail) {
          setIsTokenValid(true);
          setMaskedEmail(response.maskedEmail);
        } else {
          // Determine error type from response
          if (response.error === 'EXPIRED_TOKEN') {
            setTokenError('expired');
          } else if (response.error === 'USED_TOKEN') {
            setTokenError('used');
          } else {
            setTokenError('invalid');
          }
        }
      } catch (err) {
        const apiError = handleApiError(err);
        if (apiError.status === 410) {
          setTokenError('expired');
        } else if (apiError.status === 409) {
          setTokenError('used');
        } else {
          setTokenError('invalid');
        }
      } finally {
        setIsValidatingToken(false);
      }
    };

    validateToken();
  }, [token]);

  // Redirect countdown after success
  useEffect(() => {
    if (!isSuccess) return;

    if (redirectCountdown <= 0) {
      navigate('/login');
      return;
    }

    const timer = setInterval(() => {
      setRedirectCountdown((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [isSuccess, redirectCountdown, navigate]);

  // Focus management
  useEffect(() => {
    if (isSuccess && successRef.current) {
      successRef.current.focus();
    }
  }, [isSuccess]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitError(null);

    // Validate password meets all requirements
    if (!isPasswordValid) {
      setSubmitError(t.auth.errors.passwordTooShort);
      return;
    }

    // Validate passwords match
    if (password !== passwordConfirmation) {
      setSubmitError(t.auth.errors.passwordMismatch);
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await passwordService.resetPassword(token, password);
      if (response.success) {
        setIsSuccess(true);
      } else {
        // Handle specific errors
        if (response.error === 'PASSWORD_RECENTLY_USED') {
          setSubmitError(t.auth.resetPassword.passwordRecentlyUsed);
        } else if (response.error === 'INVALID_TOKEN') {
          setTokenError('invalid');
          setIsTokenValid(false);
        } else if (response.error === 'EXPIRED_TOKEN') {
          setTokenError('expired');
          setIsTokenValid(false);
        } else {
          setSubmitError(t.auth.errors.genericError);
        }
      }
    } catch (err) {
      const apiError = handleApiError(err);
      if (apiError.status === 400 && apiError.message.includes('recently')) {
        setSubmitError(t.auth.resetPassword.passwordRecentlyUsed);
      } else if (apiError.status === 410) {
        setTokenError('expired');
        setIsTokenValid(false);
      } else if (apiError.status === 409) {
        setTokenError('used');
        setIsTokenValid(false);
      } else {
        setSubmitError(t.auth.errors.genericError);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // Loading state
  if (isValidatingToken) {
    return (
      <AuthLayout title={t.auth.resetPassword.loadingToken} subtitle="">
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary border-t-transparent" />
        </div>
      </AuthLayout>
    );
  }

  // Token error state
  if (tokenError || !isTokenValid) {
    const errorConfig = {
      invalid: {
        title: t.auth.resetPassword.invalidToken,
        message: t.auth.resetPassword.invalidTokenMessage,
      },
      expired: {
        title: t.auth.resetPassword.expiredToken,
        message: t.auth.resetPassword.expiredTokenMessage,
      },
      used: {
        title: t.auth.resetPassword.usedToken,
        message: t.auth.resetPassword.usedTokenMessage,
      },
    };

    const config = errorConfig[tokenError || 'invalid'];

    return (
      <AuthLayout title={config.title} subtitle="">
        <div className="text-center space-y-6">
          {/* Error icon */}
          <div className="text-6xl text-red-500 mb-4">
            <span role="img" aria-label="Error">&#9888;</span>
          </div>

          {/* Error message */}
          <p className="text-text-light leading-relaxed">
            {config.message}
          </p>

          {/* Request new link button */}
          <div className="pt-4">
            <Link
              to="/forgot-password"
              className="inline-block"
            >
              <Button variant="primary" size="lg">
                {t.auth.resetPassword.requestNewLink}
              </Button>
            </Link>
          </div>

          {/* Back to login */}
          <div className="pt-2">
            <Link
              to="/login"
              className="inline-flex items-center gap-2 text-primary hover:text-primary-dark transition-colors"
            >
              <span>&larr;</span>
              {t.auth.forgotPassword.backToLogin}
            </Link>
          </div>
        </div>
      </AuthLayout>
    );
  }

  // Success state
  if (isSuccess) {
    return (
      <AuthLayout title={t.auth.resetPassword.successTitle} subtitle="">
        <div
          ref={successRef}
          tabIndex={-1}
          className="text-center space-y-6 outline-none"
          role="status"
          aria-live="polite"
        >
          {/* Success icon */}
          <div className="text-6xl text-green-500 mb-4">
            <span role="img" aria-label="Success">&#10003;</span>
          </div>

          {/* Success message */}
          <p className="text-text-light leading-relaxed">
            {t.auth.resetPassword.successMessage}
          </p>

          {/* Redirect countdown */}
          <p className="text-sm text-text-light">
            {t.auth.resetPassword.redirectMessage.replace('{seconds}', String(redirectCountdown))}
          </p>

          {/* Go to login button */}
          <div className="pt-4">
            <Link to="/login" className="inline-block">
              <Button variant="primary" size="lg">
                {t.auth.resetPassword.goToLogin}
              </Button>
            </Link>
          </div>
        </div>
      </AuthLayout>
    );
  }

  // Form state
  return (
    <AuthLayout
      title={t.auth.resetPassword.title}
      subtitle={t.auth.resetPassword.subtitle}
    >
      <form ref={formRef} onSubmit={handleSubmit} className="space-y-5" noValidate>
        {/* Masked email display */}
        {maskedEmail && (
          <div className="text-center text-sm text-text-light mb-4">
            <span>{t.auth.resetPassword.accountFor}</span>{' '}
            <span className="font-medium text-text">{maskedEmail}</span>
          </div>
        )}

        {/* Error message */}
        {submitError && (
          <div
            className="p-4 bg-red-50 border border-red-200 rounded-2xl text-red-600 text-sm"
            role="alert"
            aria-live="assertive"
          >
            {submitError}
          </div>
        )}

        {/* New password input */}
        <div className="space-y-2">
          <Input
            type="password"
            label={t.auth.resetPassword.passwordLabel}
            placeholder={t.auth.resetPassword.passwordPlaceholder}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            disabled={isSubmitting}
            autoFocus
          />

          {/* Password strength meter */}
          {password && (
            <PasswordStrengthMeter strength={strength} criteriaCount={criteriaCount} />
          )}
        </div>

        {/* Password requirements checklist */}
        <div className="space-y-2 text-sm">
          <p className="font-medium text-text-light">{t.auth.resetPassword.requirementsTitle}</p>
          <ul className="space-y-1.5">
            <RequirementItem met={validationResults.minLength} label={t.auth.resetPassword.reqMinLength} />
            <RequirementItem met={validationResults.hasUppercase} label={t.auth.resetPassword.reqUppercase} />
            <RequirementItem met={validationResults.hasLowercase} label={t.auth.resetPassword.reqLowercase} />
            <RequirementItem met={validationResults.hasNumber} label={t.auth.resetPassword.reqNumber} />
            <RequirementItem met={validationResults.hasSpecial} label={t.auth.resetPassword.reqSpecial} />
          </ul>
        </div>

        {/* Confirm password input */}
        <Input
          type="password"
          label={t.auth.resetPassword.confirmPasswordLabel}
          placeholder={t.auth.resetPassword.confirmPasswordPlaceholder}
          value={passwordConfirmation}
          onChange={(e) => setPasswordConfirmation(e.target.value)}
          autoComplete="new-password"
          disabled={isSubmitting}
        />

        {/* Submit button */}
        <Button
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          disabled={isSubmitting || !isPasswordValid || password !== passwordConfirmation}
        >
          {isSubmitting ? t.auth.resetPassword.submitting : t.auth.resetPassword.submitButton}
        </Button>

        {/* Cancel link */}
        <p className="text-center">
          <Link
            to="/login"
            className="inline-flex items-center gap-2 text-primary hover:text-primary-dark transition-colors"
          >
            <span>&larr;</span>
            {t.auth.resetPassword.cancel}
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
};

// Helper component for requirement items
const RequirementItem = ({ met, label }: { met: boolean; label: string }) => (
  <li className={cn('flex items-center gap-2', met ? 'text-green-600' : 'text-text-light')}>
    <span className="flex-shrink-0">
      {met ? (
        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
        </svg>
      ) : (
        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm0-2a6 6 0 100-12 6 6 0 000 12z" clipRule="evenodd" />
        </svg>
      )}
    </span>
    <span>{label}</span>
  </li>
);
