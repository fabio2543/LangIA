import { useState, useEffect, useRef, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { useTranslation } from '../i18n';
import { passwordService, handleApiError } from '../services/api';

const RESEND_COOLDOWN_SECONDS = 60;

export const ForgotPasswordPage = () => {
  const { t } = useTranslation();

  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resendCooldown, setResendCooldown] = useState(0);

  const successRef = useRef<HTMLDivElement>(null);

  // Countdown timer for resend cooldown
  useEffect(() => {
    if (resendCooldown <= 0) return;

    const timer = setInterval(() => {
      setResendCooldown((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [resendCooldown]);

  // Focus management after submit
  useEffect(() => {
    if (isSubmitted && successRef.current) {
      successRef.current.focus();
    }
  }, [isSubmitted]);

  const validateEmail = (email: string): boolean => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    // Client-side validation
    if (!email.trim()) {
      setError(t.auth.errors.requiredField);
      return;
    }

    if (!validateEmail(email)) {
      setError(t.auth.errors.invalidEmail);
      return;
    }

    setIsLoading(true);

    try {
      await passwordService.forgotPassword(email.trim().toLowerCase());
      // Success - always show generic message
      setIsSubmitted(true);
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
    } catch (err) {
      const apiError = handleApiError(err);

      if (apiError.status === 429) {
        // Rate limit exceeded
        setError(t.auth.forgotPassword.rateLimitError);
      } else if (apiError.status && apiError.status >= 400 && apiError.status < 500) {
        // Other 4xx errors - show success to not reveal email existence
        setIsSubmitted(true);
        setResendCooldown(RESEND_COOLDOWN_SECONDS);
      } else {
        // Server errors
        setError(t.auth.errors.genericError);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = () => {
    if (resendCooldown > 0) return;
    setIsSubmitted(false);
    setError(null);
  };

  // Success state
  if (isSubmitted) {
    return (
      <AuthLayout
        title={t.auth.forgotPassword.successTitle}
        subtitle=""
      >
        <div
          ref={successRef}
          tabIndex={-1}
          className="text-center space-y-6 outline-none"
          role="status"
          aria-live="polite"
        >
          {/* Email icon */}
          <div className="text-6xl mb-4">
            <span role="img" aria-label="Email">&#9993;</span>
          </div>

          {/* Success message */}
          <p className="text-text-light leading-relaxed">
            {t.auth.forgotPassword.successMessage}
          </p>

          {/* Spam notice */}
          <p className="text-sm text-text-light">
            {t.auth.forgotPassword.checkSpam}
          </p>

          {/* Resend link */}
          <div className="pt-4">
            {resendCooldown > 0 ? (
              <p className="text-sm text-text-light">
                {t.auth.forgotPassword.resendCooldown.replace('{seconds}', String(resendCooldown))}
              </p>
            ) : (
              <button
                type="button"
                onClick={handleResend}
                className="text-primary hover:text-primary-dark transition-colors font-medium"
              >
                {t.auth.forgotPassword.resendLink}
              </button>
            )}
          </div>

          {/* Back to login */}
          <div className="pt-4">
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

  // Form state
  return (
    <AuthLayout
      title={t.auth.forgotPassword.title}
      subtitle={t.auth.forgotPassword.subtitle}
    >
      <form onSubmit={handleSubmit} className="space-y-5" noValidate>
        {/* Error message */}
        {error && (
          <div
            className="p-4 bg-red-50 border border-red-200 rounded-2xl text-red-600 text-sm"
            role="alert"
            aria-live="assertive"
          >
            {error}
          </div>
        )}

        {/* Email input */}
        <Input
          type="email"
          label={t.auth.forgotPassword.emailLabel}
          placeholder={t.auth.forgotPassword.emailPlaceholder}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          autoComplete="email"
          disabled={isLoading}
          autoFocus
          aria-describedby={error ? 'email-error' : undefined}
        />

        {/* Submit button */}
        <Button
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          disabled={isLoading}
        >
          {isLoading ? t.auth.forgotPassword.submitting : t.auth.forgotPassword.submitButton}
        </Button>

        {/* Back to login link */}
        <p className="text-center">
          <Link
            to="/login"
            className="inline-flex items-center gap-2 text-primary hover:text-primary-dark transition-colors"
          >
            <span>&larr;</span>
            {t.auth.forgotPassword.backToLogin}
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
};
