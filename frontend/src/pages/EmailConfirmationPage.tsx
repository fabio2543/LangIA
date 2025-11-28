import { useState, useEffect, useRef } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { Button } from '../components/common/Button';
import { useTranslation } from '../i18n';
import { emailVerificationService, handleApiError } from '../services/api';

const REDIRECT_COUNTDOWN_SECONDS = 5;

type TokenError = 'invalid' | 'expired' | 'used' | null;

export const EmailConfirmationPage = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token') || '';

  // Confirmation state
  const [isConfirming, setIsConfirming] = useState(true);
  const [isSuccess, setIsSuccess] = useState(false);
  const [tokenError, setTokenError] = useState<TokenError>(null);

  // Redirect countdown
  const [redirectCountdown, setRedirectCountdown] = useState(REDIRECT_COUNTDOWN_SECONDS);

  // Refs
  const successRef = useRef<HTMLDivElement>(null);

  // Confirm email on mount
  useEffect(() => {
    const confirmEmail = async () => {
      if (!token) {
        setTokenError('invalid');
        setIsConfirming(false);
        return;
      }

      try {
        const response = await emailVerificationService.confirmEmail(token);
        if (response.success) {
          setIsSuccess(true);
        } else {
          // Determine error type from response
          if (response.error === 'TOKEN_EXPIRED') {
            setTokenError('expired');
          } else if (response.error === 'TOKEN_USED') {
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
        setIsConfirming(false);
      }
    };

    confirmEmail();
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

  // Loading state
  if (isConfirming) {
    return (
      <AuthLayout title={t.auth.emailConfirmation.verifying} subtitle="">
        <div className="flex flex-col items-center justify-center py-12 space-y-4">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary border-t-transparent" />
          <p className="text-text-light">{t.auth.emailConfirmation.pleaseWait}</p>
        </div>
      </AuthLayout>
    );
  }

  // Success state
  if (isSuccess) {
    return (
      <AuthLayout title={t.auth.emailConfirmation.successTitle} subtitle="">
        <div
          ref={successRef}
          tabIndex={-1}
          className="text-center space-y-6 outline-none"
          role="status"
          aria-live="polite"
        >
          {/* Success icon */}
          <div className="flex justify-center">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center">
              <svg
                className="w-10 h-10 text-green-500"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
          </div>

          {/* Success message */}
          <p className="text-text-light leading-relaxed">
            {t.auth.emailConfirmation.successMessage}
          </p>

          {/* Redirect countdown */}
          <p className="text-sm text-text-light">
            {t.auth.emailConfirmation.redirectMessage.replace('{seconds}', String(redirectCountdown))}
          </p>

          {/* Go to login button */}
          <div className="pt-4">
            <Link to="/login" className="inline-block">
              <Button variant="primary" size="lg">
                {t.auth.emailConfirmation.goToLogin}
              </Button>
            </Link>
          </div>
        </div>
      </AuthLayout>
    );
  }

  // Error state
  const errorConfig = {
    invalid: {
      title: t.auth.emailConfirmation.invalidToken,
      message: t.auth.emailConfirmation.invalidTokenMessage,
      icon: (
        <svg
          className="w-10 h-10 text-red-500"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      ),
    },
    expired: {
      title: t.auth.emailConfirmation.expiredToken,
      message: t.auth.emailConfirmation.expiredTokenMessage,
      icon: (
        <svg
          className="w-10 h-10 text-amber-500"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      ),
    },
    used: {
      title: t.auth.emailConfirmation.usedToken,
      message: t.auth.emailConfirmation.usedTokenMessage,
      icon: (
        <svg
          className="w-10 h-10 text-blue-500"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      ),
    },
  };

  const config = errorConfig[tokenError || 'invalid'];

  return (
    <AuthLayout title={config.title} subtitle="">
      <div className="text-center space-y-6">
        {/* Error icon */}
        <div className="flex justify-center">
          <div className="w-20 h-20 bg-background-dark rounded-full flex items-center justify-center">
            {config.icon}
          </div>
        </div>

        {/* Error message */}
        <p className="text-text-light leading-relaxed">{config.message}</p>

        {/* Actions based on error type */}
        {tokenError === 'used' ? (
          // Already verified - go to login
          <div className="pt-4">
            <Link to="/login" className="inline-block">
              <Button variant="primary" size="lg">
                {t.auth.emailConfirmation.goToLogin}
              </Button>
            </Link>
          </div>
        ) : (
          // Invalid or expired - go to signup
          <>
            <div className="pt-4">
              <Link to="/signup" className="inline-block">
                <Button variant="primary" size="lg">
                  {t.auth.emailConfirmation.createNewAccount}
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
          </>
        )}
      </div>
    </AuthLayout>
  );
};
