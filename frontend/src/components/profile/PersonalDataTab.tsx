import { useState, useEffect } from 'react';
import { useTranslation } from '../../i18n';
import { Input } from '../common/Input';
import { Dropdown } from '../common/Dropdown';
import { Textarea } from '../common/Textarea';
import { Button } from '../common/Button';
import { Alert } from '../common/Alert';
import { EmailChangeModal } from './EmailChangeModal';
import { profileService } from '../../services/profileService';
import { handleApiError } from '../../services/api';
import type { UpdatePersonalDataRequest } from '../../types';

const TIMEZONES = [
  { value: 'America/Sao_Paulo', label: 'America/Sao_Paulo (GMT-3)' },
  { value: 'America/New_York', label: 'America/New_York (GMT-5)' },
  { value: 'America/Los_Angeles', label: 'America/Los_Angeles (GMT-8)' },
  { value: 'Europe/London', label: 'Europe/London (GMT+0)' },
  { value: 'Europe/Paris', label: 'Europe/Paris (GMT+1)' },
  { value: 'Asia/Tokyo', label: 'Asia/Tokyo (GMT+9)' },
  { value: 'Australia/Sydney', label: 'Australia/Sydney (GMT+11)' },
];

export const PersonalDataTab = () => {
  const { t } = useTranslation();
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [isEmailModalOpen, setIsEmailModalOpen] = useState(false);

  const [formData, setFormData] = useState<UpdatePersonalDataRequest>({
    fullName: '',
    whatsappPhone: '',
    nativeLanguage: '',
    timezone: '',
    birthDate: '',
    bio: '',
  });
  const [email, setEmail] = useState('');

  // Carregar dados do perfil
  useEffect(() => {
    const loadProfile = async () => {
      try {
        setIsLoading(true);
        const data = await profileService.getProfileDetails();
        setFormData({
          fullName: data.fullName || '',
          whatsappPhone: data.whatsappPhone || '',
          nativeLanguage: data.nativeLanguage || '',
          timezone: data.timezone || 'America/Sao_Paulo',
          birthDate: data.birthDate || '',
          bio: data.bio || '',
        });
        setEmail(data.email);
      } catch (err) {
        const apiError = handleApiError(err);
        setError(apiError.message);
      } finally {
        setIsLoading(false);
      }
    };

    loadProfile();
  }, []);

  const handleInputChange = (field: keyof UpdatePersonalDataRequest, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setSuccess(false);
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    setSuccess(false);

    try {
      await profileService.updateProfileDetails(formData);
      setSuccess(true);
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsSaving(false);
    }
  };

  const handleEmailChanged = (newEmail: string) => {
    setEmail(newEmail);
    setIsEmailModalOpen(false);
  };

  const languageOptions = Object.entries(t.enums.languages).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <div className="text-4xl mb-4 animate-pulse">⏳</div>
          <p className="text-text-light">{t.profile.common.loading}</p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <h2 className="text-xl font-semibold text-text mb-2">
        {t.profile.personalData.title}
      </h2>
      <p className="text-text-light mb-6">
        {t.profile.subtitle}
      </p>

      {error && (
        <Alert variant="error" className="mb-6" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" className="mb-6" onClose={() => setSuccess(false)}>
          {t.profile.personalData.successMessage}
        </Alert>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Nome completo */}
        <Input
          label={t.profile.personalData.fullName}
          value={formData.fullName}
          onChange={(e) => handleInputChange('fullName', e.target.value)}
          placeholder="João Silva"
        />

        {/* Email (readonly + botão de alteração) */}
        <div>
          <label className="block text-sm font-medium text-text mb-2">
            {t.profile.personalData.email}
          </label>
          <div className="flex gap-3">
            <Input
              value={email}
              disabled
              className="flex-1 opacity-60"
            />
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsEmailModalOpen(true)}
              className="whitespace-nowrap"
            >
              {t.profile.personalData.changeEmail}
            </Button>
          </div>
        </div>

        {/* WhatsApp */}
        <Input
          label={t.profile.personalData.whatsappPhone}
          value={formData.whatsappPhone}
          onChange={(e) => handleInputChange('whatsappPhone', e.target.value)}
          placeholder={t.profile.personalData.whatsappPhonePlaceholder}
        />

        {/* Idioma nativo */}
        <Dropdown
          label={t.profile.personalData.nativeLanguage}
          value={formData.nativeLanguage}
          onChange={(value) => handleInputChange('nativeLanguage', value)}
          options={languageOptions}
          placeholder={t.profile.personalData.selectNativeLanguage}
        />

        {/* Fuso horário */}
        <Dropdown
          label={t.profile.personalData.timezone}
          value={formData.timezone}
          onChange={(value) => handleInputChange('timezone', value)}
          options={TIMEZONES}
          placeholder={t.profile.personalData.selectTimezone}
        />

        {/* Data de nascimento */}
        <div>
          <label className="block text-sm font-medium text-text mb-2">
            {t.profile.personalData.birthDate}
          </label>
          <input
            type="date"
            value={formData.birthDate}
            onChange={(e) => handleInputChange('birthDate', e.target.value)}
            className="w-full rounded-full border border-gray-200 bg-white text-text px-5 py-3 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>

        {/* Bio */}
        <Textarea
          label={t.profile.personalData.bio}
          value={formData.bio}
          onChange={(e) => handleInputChange('bio', e.target.value)}
          placeholder={t.profile.personalData.bioPlaceholder}
          maxLength={500}
          showCount
        />

        {/* Botão de salvar */}
        <div className="pt-4">
          <Button
            type="submit"
            disabled={isSaving}
            fullWidth
          >
            {isSaving ? t.profile.personalData.saving : t.profile.personalData.saveButton}
          </Button>
        </div>
      </form>

      {/* Email Change Modal */}
      <EmailChangeModal
        isOpen={isEmailModalOpen}
        onClose={() => setIsEmailModalOpen(false)}
        onEmailChanged={handleEmailChanged}
        currentEmail={email}
      />
    </div>
  );
};
