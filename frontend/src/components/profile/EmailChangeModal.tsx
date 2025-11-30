import { useState, useEffect } from 'react';
import { useTranslation } from '../../i18n';
import { Modal } from '../common/Modal';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { Alert } from '../common/Alert';
import { emailChangeService } from '../../services/profileService';
import { handleApiError } from '../../services/api';

interface EmailChangeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onEmailChanged: (newEmail: string) => void;
  currentEmail: string;
}

export const EmailChangeModal = ({
  isOpen,
  onClose,
  onEmailChanged,
  currentEmail,
}: EmailChangeModalProps) => {
  const { t } = useTranslation();
  const [step, setStep] = useState<1 | 2>(1);
  const [newEmail, setNewEmail] = useState('');
  const [code, setCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resendCooldown, setResendCooldown] = useState(0);

  // Reset state quando o modal fecha
  useEffect(() => {
    if (!isOpen) {
      setStep(1);
      setNewEmail('');
      setCode('');
      setError(null);
      setResendCooldown(0);
    }
  }, [isOpen]);

  // Cooldown timer
  useEffect(() => {
    if (resendCooldown > 0) {
      const timer = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [resendCooldown]);

  const handleRequestCode = async () => {
    if (!newEmail || !newEmail.includes('@')) {
      setError(t.auth.errors.invalidEmail);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await emailChangeService.requestEmailChange(newEmail);
      setStep(2);
      setResendCooldown(60);
    } catch (err) {
      const apiError = handleApiError(err);
      if (apiError.status === 409) {
        setError(t.profile.emailChange.errors.emailExists);
      } else {
        setError(apiError.message);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyCode = async () => {
    if (!code || code.length !== 6) {
      setError(t.profile.emailChange.errors.invalidCode);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await emailChangeService.verifyEmailChange(code);
      onEmailChanged(newEmail);
    } catch (err) {
      const apiError = handleApiError(err);
      if (apiError.status === 400) {
        setError(t.profile.emailChange.errors.invalidCode);
      } else if (apiError.status === 410) {
        setError(t.profile.emailChange.errors.expiredCode);
      } else {
        setError(apiError.message);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendCode = async () => {
    if (resendCooldown > 0) return;
    await handleRequestCode();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={t.profile.emailChange.title}
      size="md"
    >
      {error && (
        <Alert variant="error" className="mb-4" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {step === 1 ? (
        // Step 1: Enter new email
        <div className="space-y-4">
          <div>
            <h3 className="font-medium text-text mb-1">
              {t.profile.emailChange.step1Title}
            </h3>
            <p className="text-sm text-text-light">
              {t.profile.emailChange.step1Subtitle}
            </p>
          </div>

          <div className="text-sm text-text-light">
            <span className="font-medium">Email atual:</span> {currentEmail}
          </div>

          <Input
            label={t.profile.emailChange.newEmailLabel}
            type="email"
            value={newEmail}
            onChange={(e) => setNewEmail(e.target.value)}
            placeholder={t.profile.emailChange.newEmailPlaceholder}
            disabled={isLoading}
          />

          <div className="flex gap-3 pt-2">
            <Button
              type="button"
              variant="ghost"
              onClick={onClose}
              disabled={isLoading}
              className="flex-1"
            >
              {t.profile.emailChange.cancel}
            </Button>
            <Button
              type="button"
              onClick={handleRequestCode}
              disabled={isLoading || !newEmail}
              className="flex-1"
            >
              {isLoading ? t.profile.emailChange.sending : t.profile.emailChange.sendCode}
            </Button>
          </div>
        </div>
      ) : (
        // Step 2: Enter verification code
        <div className="space-y-4">
          <div>
            <h3 className="font-medium text-text mb-1">
              {t.profile.emailChange.step2Title}
            </h3>
            <p className="text-sm text-text-light">
              {t.profile.emailChange.step2Subtitle} <strong>{newEmail}</strong>
            </p>
          </div>

          <Input
            label={t.profile.emailChange.codeLabel}
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
            placeholder={t.profile.emailChange.codePlaceholder}
            disabled={isLoading}
            className="text-center text-2xl tracking-widest"
          />

          <div className="text-center">
            <button
              type="button"
              onClick={handleResendCode}
              disabled={resendCooldown > 0 || isLoading}
              className="text-sm text-primary hover:text-primary-dark disabled:text-text-light disabled:cursor-not-allowed transition-colors"
            >
              {resendCooldown > 0
                ? t.profile.emailChange.resendCooldown.replace('{seconds}', String(resendCooldown))
                : t.profile.emailChange.resendCode}
            </button>
          </div>

          <div className="flex gap-3 pt-2">
            <Button
              type="button"
              variant="ghost"
              onClick={() => setStep(1)}
              disabled={isLoading}
              className="flex-1"
            >
              {t.profile.common.cancel}
            </Button>
            <Button
              type="button"
              onClick={handleVerifyCode}
              disabled={isLoading || code.length !== 6}
              className="flex-1"
            >
              {isLoading ? t.profile.emailChange.verifying : t.profile.emailChange.verifyButton}
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
};
