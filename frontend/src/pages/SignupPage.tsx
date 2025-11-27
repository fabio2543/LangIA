import { useState, type FormEvent, type ChangeEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/auth/AuthLayout';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../i18n';
import type { UserProfile } from '../types';
import { cn } from '../utils/cn';

interface FormData {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  cpf: string;
  phone: string;
  profile: UserProfile;
  acceptTerms: boolean;
}

interface FormErrors {
  name?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  cpf?: string;
  phone?: string;
  profile?: string;
  acceptTerms?: string;
  general?: string;
}

// Funções de máscara
const formatCpf = (value: string): string => {
  const numbers = value.replace(/\D/g, '').slice(0, 11);
  return numbers
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
};

const formatPhone = (value: string): string => {
  const numbers = value.replace(/\D/g, '').slice(0, 11);
  if (numbers.length <= 10) {
    return numbers
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4})(\d)/, '$1-$2');
  }
  return numbers
    .replace(/(\d{2})(\d)/, '($1) $2')
    .replace(/(\d{5})(\d)/, '$1-$2');
};

// Validação de CPF
const isValidCpf = (cpf: string): boolean => {
  const numbers = cpf.replace(/\D/g, '');
  if (numbers.length !== 11) return false;
  if (/^(\d)\1+$/.test(numbers)) return false;

  let sum = 0;
  for (let i = 0; i < 9; i++) {
    sum += parseInt(numbers[i]) * (10 - i);
  }
  let digit = (sum * 10) % 11;
  if (digit === 10) digit = 0;
  if (digit !== parseInt(numbers[9])) return false;

  sum = 0;
  for (let i = 0; i < 10; i++) {
    sum += parseInt(numbers[i]) * (11 - i);
  }
  digit = (sum * 10) % 11;
  if (digit === 10) digit = 0;
  if (digit !== parseInt(numbers[10])) return false;

  return true;
};

export const SignupPage = () => {
  const { t } = useTranslation();
  const { register, isLoading } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState<FormData>({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    cpf: '',
    phone: '',
    profile: 'STUDENT',
    acceptTerms: false,
  });
  const [errors, setErrors] = useState<FormErrors>({});

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    let formattedValue = value;

    if (name === 'cpf') {
      formattedValue = formatCpf(value);
    } else if (name === 'phone') {
      formattedValue = formatPhone(value);
    }

    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : formattedValue,
    }));

    // Limpa o erro do campo ao digitar
    if (errors[name as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const setProfile = (profile: UserProfile) => {
    setFormData((prev) => ({ ...prev, profile }));
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = t.auth.errors.requiredField;
    }

    if (!formData.email.trim()) {
      newErrors.email = t.auth.errors.requiredField;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = t.auth.errors.invalidEmail;
    }

    if (!formData.password) {
      newErrors.password = t.auth.errors.requiredField;
    } else if (formData.password.length < 6) {
      newErrors.password = t.auth.errors.passwordTooShort;
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = t.auth.errors.requiredField;
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = t.auth.errors.passwordMismatch;
    }

    if (!formData.cpf.trim()) {
      newErrors.cpf = t.auth.errors.requiredField;
    } else if (!isValidCpf(formData.cpf)) {
      newErrors.cpf = t.auth.errors.invalidCpf;
    }

    const phoneNumbers = formData.phone.replace(/\D/g, '');
    if (!formData.phone.trim()) {
      newErrors.phone = t.auth.errors.requiredField;
    } else if (phoneNumbers.length < 10 || phoneNumbers.length > 11) {
      newErrors.phone = t.auth.errors.invalidPhone;
    }

    if (!formData.acceptTerms) {
      newErrors.acceptTerms = t.auth.errors.termsRequired;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrors({});

    if (!validateForm()) return;

    try {
      await register({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        cpf: formData.cpf.replace(/\D/g, ''),
        phone: formData.phone.replace(/\D/g, ''),
        profile: formData.profile,
      });
      navigate('/dashboard');
    } catch (error) {
      const errorMessage = (error as { message?: string })?.message || '';
      if (errorMessage.toLowerCase().includes('email')) {
        setErrors({ email: t.auth.errors.emailExists });
      } else {
        setErrors({ general: t.auth.errors.genericError });
      }
    }
  };

  return (
    <AuthLayout title={t.auth.signup.title} subtitle={t.auth.signup.subtitle}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Erro geral */}
        {errors.general && (
          <div className="p-4 bg-red-50 border border-red-200 rounded-2xl text-red-600 text-sm">
            {errors.general}
          </div>
        )}

        {/* Nome */}
        <Input
          name="name"
          type="text"
          label={t.auth.signup.nameLabel}
          placeholder={t.auth.signup.namePlaceholder}
          value={formData.name}
          onChange={handleChange}
          error={errors.name}
          autoComplete="name"
          disabled={isLoading}
        />

        {/* Email */}
        <Input
          name="email"
          type="email"
          label={t.auth.signup.emailLabel}
          placeholder={t.auth.signup.emailPlaceholder}
          value={formData.email}
          onChange={handleChange}
          error={errors.email}
          autoComplete="email"
          disabled={isLoading}
        />

        {/* Senha e Confirmar Senha lado a lado em desktop */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input
            name="password"
            type="password"
            label={t.auth.signup.passwordLabel}
            placeholder={t.auth.signup.passwordPlaceholder}
            value={formData.password}
            onChange={handleChange}
            error={errors.password}
            autoComplete="new-password"
            disabled={isLoading}
          />
          <Input
            name="confirmPassword"
            type="password"
            label={t.auth.signup.confirmPasswordLabel}
            placeholder={t.auth.signup.confirmPasswordPlaceholder}
            value={formData.confirmPassword}
            onChange={handleChange}
            error={errors.confirmPassword}
            autoComplete="new-password"
            disabled={isLoading}
          />
        </div>

        {/* CPF e Telefone lado a lado em desktop */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input
            name="cpf"
            type="text"
            label={t.auth.signup.cpfLabel}
            placeholder={t.auth.signup.cpfPlaceholder}
            value={formData.cpf}
            onChange={handleChange}
            error={errors.cpf}
            disabled={isLoading}
          />
          <Input
            name="phone"
            type="tel"
            label={t.auth.signup.phoneLabel}
            placeholder={t.auth.signup.phonePlaceholder}
            value={formData.phone}
            onChange={handleChange}
            error={errors.phone}
            autoComplete="tel"
            disabled={isLoading}
          />
        </div>

        {/* Seletor de Perfil */}
        <div>
          <label className="block text-sm font-medium text-text mb-2">
            {t.auth.signup.profileLabel}
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setProfile('STUDENT')}
              className={cn(
                'flex items-center justify-center gap-2 px-4 py-3 rounded-full border-2 transition-all duration-200 font-medium',
                formData.profile === 'STUDENT'
                  ? 'border-primary bg-primary-light text-primary'
                  : 'border-gray-200 bg-white text-text-light hover:border-primary/50'
              )}
            >
              <span>🎓</span>
              {t.auth.signup.profileStudent}
            </button>
            <button
              type="button"
              onClick={() => setProfile('TEACHER')}
              className={cn(
                'flex items-center justify-center gap-2 px-4 py-3 rounded-full border-2 transition-all duration-200 font-medium',
                formData.profile === 'TEACHER'
                  ? 'border-primary bg-primary-light text-primary'
                  : 'border-gray-200 bg-white text-text-light hover:border-primary/50'
              )}
            >
              <span>👨‍🏫</span>
              {t.auth.signup.profileTeacher}
            </button>
          </div>
        </div>

        {/* Termos de Uso */}
        <div>
          <label className="flex items-start gap-3 cursor-pointer">
            <input
              type="checkbox"
              name="acceptTerms"
              checked={formData.acceptTerms}
              onChange={handleChange}
              className="mt-1 w-5 h-5 rounded border-gray-300 text-primary focus:ring-primary"
              disabled={isLoading}
            />
            <span className="text-sm text-text-light">
              {t.auth.signup.termsCheckbox}{' '}
              <Link to="/terms" className="text-primary hover:text-primary-dark">
                {t.auth.signup.termsLink}
              </Link>{' '}
              {t.auth.signup.andThe}{' '}
              <Link to="/privacy" className="text-primary hover:text-primary-dark">
                {t.auth.signup.privacyLink}
              </Link>
            </span>
          </label>
          {errors.acceptTerms && (
            <p className="mt-1.5 text-sm text-red-500">{errors.acceptTerms}</p>
          )}
        </div>

        {/* Botão de Submit */}
        <Button
          type="submit"
          variant="primary"
          size="lg"
          fullWidth
          disabled={isLoading}
        >
          {isLoading ? t.auth.signup.submitting : t.auth.signup.submitButton}
        </Button>

        {/* Link para Login */}
        <p className="text-center text-text-light">
          {t.auth.signup.hasAccount}{' '}
          <Link
            to="/login"
            className="text-primary font-medium hover:text-primary-dark transition-colors"
          >
            {t.auth.signup.loginLink}
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
};
