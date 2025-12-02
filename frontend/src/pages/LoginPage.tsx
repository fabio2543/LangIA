import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios, { type AxiosError } from 'axios';
import { AuthLayout } from '../components/auth/AuthLayout';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { useAuth } from '../hooks/useAuth';
import { useTranslation } from '../i18n';
import type { PendingVerificationResponse } from '../types';

interface FormErrors {
  email?: string;
  password?: string;
  general?: string;
}

export const LoginPage = () => {
  const { t } = useTranslation();
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<FormErrors>({});

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!email.trim()) {
      newErrors.email = t.auth.errors.requiredField;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = t.auth.errors.invalidEmail;
    }

    if (!password) {
      newErrors.password = t.auth.errors.requiredField;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrors({});

    if (!validateForm()) return;

    try {
      await login({ email, password });
      navigate('/dashboard');
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<PendingVerificationResponse>;
        const status = axiosError.response?.status;

        // 403 - Email não verificado
        if (status === 403 && axiosError.response?.data?.emailVerificationRequired) {
          navigate('/verify-email', {
            state: {
              userId: axiosError.response.data.userId,
              maskedEmail: axiosError.response.data.maskedEmail,
            },
          });
          return;
        }

        // 401 - Credenciais inválidas
        if (status === 401) {
          setErrors({ general: t.auth.errors.invalidCredentials });
          return;
        }

        // 429 - Muitas tentativas (rate limit)
        if (status === 429) {
          setErrors({ general: t.auth.errors.tooManyAttempts });
          return;
        }

        // 5xx - Erro do servidor
        if (status && status >= 500) {
          setErrors({ general: t.auth.errors.serverError });
          return;
        }

        // Erro de rede (sem resposta do servidor)
        if (!axiosError.response) {
          setErrors({ general: t.auth.errors.networkError });
          return;
        }
      }

      // Fallback para outros erros
      setErrors({ general: t.auth.errors.genericError });
    }
  };

  return (
    <AuthLayout title={t.auth.login.title} subtitle={t.auth.login.subtitle}>
      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Erro geral */}
        {errors.general && (
          <div className="p-4 bg-red-50 border border-red-200 rounded-2xl text-red-600 text-sm">
            {errors.general}
          </div>
        )}

        {/* Email */}
        <Input
          type="email"
          label={t.auth.login.emailLabel}
          placeholder={t.auth.login.emailPlaceholder}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={errors.email}
          autoComplete="email"
          disabled={isLoading}
        />

        {/* Senha */}
        <Input
          type="password"
          label={t.auth.login.passwordLabel}
          placeholder={t.auth.login.passwordPlaceholder}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={errors.password}
          autoComplete="current-password"
          disabled={isLoading}
        />

        {/* Esqueceu a senha */}
        <div className="text-right">
          <Link
            to="/forgot-password"
            className="text-sm text-primary hover:text-primary-dark transition-colors"
          >
            {t.auth.login.forgotPassword}
          </Link>
        </div>

        {/* Botão de Submit */}
        <Button
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          disabled={isLoading}
        >
          {isLoading ? t.auth.login.submitting : t.auth.login.submitButton}
        </Button>

        {/* Link para Cadastro */}
        <p className="text-center text-text-light">
          {t.auth.login.noAccount}{' '}
          <Link
            to="/signup"
            className="text-primary font-medium hover:text-primary-dark transition-colors"
          >
            {t.auth.login.createAccount}
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
};
