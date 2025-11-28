import { useState, useEffect, useCallback } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { Button } from '../components/common/Button';
import { useTranslation } from '../i18n';
import { emailVerificationService, handleApiError } from '../services/api';

const RESEND_COOLDOWN_SECONDS = 60;

interface LocationState {
  userId?: string;
  maskedEmail?: string;
  userName?: string;
}

export const VerifyEmailPage = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as LocationState | null;

  // Redirect if no state
  useEffect(() => {
    if (!state?.userId || !state?.maskedEmail) {
      navigate('/signup', { replace: true });
    }
  }, [state, navigate]);

  // Resend state
  const [isResending, setIsResending] = useState(false);
  const [resendError, setResendError] = useState<string | null>(null);
  const [resendSuccess, setResendSuccess] = useState(false);
  const [cooldownSeconds, setCooldownSeconds] = useState(0);
  const [remainingResends, setRemainingResends] = useState<number | null>(null);
  const [rateLimitSeconds, setRateLimitSeconds] = useState<number | null>(null);

  // Cooldown timer
  useEffect(() => {
    if (cooldownSeconds <= 0) return;

    const timer = setInterval(() => {
      setCooldownSeconds((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [cooldownSeconds]);

  // Rate limit timer
  useEffect(() => {
    if (!rateLimitSeconds || rateLimitSeconds <= 0) return;

    const timer = setInterval(() => {
      setRateLimitSeconds((prev) => {
        if (!prev || prev <= 1) {
          return null;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [rateLimitSeconds]);

  const handleResend = useCallback(async () => {
    if (!state?.userId || cooldownSeconds > 0 || rateLimitSeconds) return;

    setIsResending(true);
    setResendError(null);
    setResendSuccess(false);

    try {
      const response = await emailVerificationService.resendVerification({
        userId: state.userId,
      });

      if (response.success) {
        setResendSuccess(true);
        setCooldownSeconds(RESEND_COOLDOWN_SECONDS);
        if (response.remainingResends !== undefined) {
          setRemainingResends(response.remainingResends);
        }
      } else {
        if (response.retryAfterSeconds) {
          setRateLimitSeconds(response.retryAfterSeconds);
          setResendError(t.auth.verifyEmail.rateLimitError);
        } else {
          setResendError(response.message || t.auth.errors.genericError);
        }
      }
    } catch (err) {
      const apiError = handleApiError(err);
      if (apiError.status === 429) {
        setRateLimitSeconds(60); // Default to 60 seconds if not specified
        setResendError(t.auth.verifyEmail.rateLimitError);
      } else {
        setResendError(apiError.message || t.auth.errors.genericError);
      }
    } finally {
      setIsResending(false);
    }
  }, [state?.userId, cooldownSeconds, rateLimitSeconds, t]);

  if (!state?.userId || !state?.maskedEmail) {
    return null; // Will redirect
  }

  const isResendDisabled = isResending || cooldownSeconds > 0 || !!rateLimitSeconds;

  return (
    <AuthLayout
      title={t.auth.verifyEmail.title}
      subtitle={t.auth.verifyEmail.subtitle}
    >
      <div className="text-center space-y-6">
        {/* Email icon */}
        <div className="flex justify-center">
          <div className="w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center">
            <svg
              className="w-10 h-10 text-primary"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
          </div>
        </div>

        {/* Welcome message */}
        {state.userName && (
          <p className="text-lg font-medium text-text">
            {t.auth.verifyEmail.welcomeMessage.replace('{name}', state.userName)}
          </p>
        )}

        {/* Instructions */}
        <p className="text-text-light leading-relaxed">
          {t.auth.verifyEmail.instructions}
        </p>

        {/* Masked email */}
        <p className="font-medium text-text bg-background-dark rounded-lg py-3 px-4">
          {state.maskedEmail}
        </p>

        {/* Success message */}
        {resendSuccess && (
          <div
            className="p-4 bg-green-50 border border-green-200 rounded-2xl text-green-600 text-sm"
            role="status"
            aria-live="polite"
          >
            {t.auth.verifyEmail.resendSuccess}
          </div>
        )}

        {/* Error message */}
        {resendError && (
          <div
            className="p-4 bg-red-50 border border-red-200 rounded-2xl text-red-600 text-sm"
            role="alert"
            aria-live="assertive"
          >
            {resendError}
          </div>
        )}

        {/* Rate limit countdown */}
        {rateLimitSeconds && rateLimitSeconds > 0 && (
          <p className="text-sm text-amber-600">
            {t.auth.verifyEmail.rateLimitCountdown.replace('{seconds}', String(rateLimitSeconds))}
          </p>
        )}

        {/* Remaining resends info */}
        {remainingResends !== null && remainingResends > 0 && (
          <p className="text-sm text-text-light">
            {t.auth.verifyEmail.remainingResends.replace('{count}', String(remainingResends))}
          </p>
        )}

        {/* Resend button */}
        <div className="pt-2">
          <Button
            variant="outline"
            size="lg"
            onClick={handleResend}
            disabled={isResendDisabled}
            fullWidth
          >
            {isResending
              ? t.auth.verifyEmail.resending
              : cooldownSeconds > 0
                ? t.auth.verifyEmail.resendCooldown.replace('{seconds}', String(cooldownSeconds))
                : t.auth.verifyEmail.resendButton}
          </Button>
        </div>

        {/* Spam notice */}
        <p className="text-sm text-text-light">
          {t.auth.verifyEmail.spamNotice}
        </p>

        {/* Back to login */}
        <div className="pt-4 border-t border-border">
          <p className="text-sm text-text-light mb-2">
            {t.auth.verifyEmail.alreadyVerified}
          </p>
          <Link
            to="/login"
            className="inline-flex items-center gap-2 text-primary hover:text-primary-dark transition-colors font-medium"
          >
            {t.auth.verifyEmail.goToLogin}
            <span>&rarr;</span>
          </Link>
        </div>
      </div>
    </AuthLayout>
  );
};
