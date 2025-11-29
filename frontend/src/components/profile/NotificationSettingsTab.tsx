import { useState, useEffect } from 'react';
import { useTranslation } from '../../i18n';
import { Toggle } from '../common/Toggle';
import { Dropdown } from '../common/Dropdown';
import { Button } from '../common/Button';
import { Alert } from '../common/Alert';
import { notificationSettingsService } from '../../services/profileService';
import { handleApiError } from '../../services/api';
import type {
  NotificationSettings,
  NotificationChannel,
  NotificationCategory,
  ReminderFrequency,
} from '../../types';

const CHANNELS: NotificationChannel[] = ['PUSH', 'EMAIL', 'WHATSAPP'];
const CATEGORIES: NotificationCategory[] = [
  'SECURITY',
  'STUDY_REMINDERS',
  'PROGRESS',
  'CONTENT',
  'CLASS',
  'MARKETING',
];

const DEFAULT_SETTINGS: NotificationSettings = {
  activeChannels: { PUSH: true, EMAIL: true, WHATSAPP: false },
  categoryPreferences: {
    SECURITY: { active: true, channels: ['PUSH', 'EMAIL'] },
    STUDY_REMINDERS: { active: true, channels: ['PUSH'] },
    PROGRESS: { active: true, channels: ['EMAIL'] },
    CONTENT: { active: true, channels: ['EMAIL'] },
    CLASS: { active: true, channels: ['PUSH', 'EMAIL'] },
    MARKETING: { active: false, channels: [] },
  },
  reminderFrequency: 'DAILY',
  preferredTimeStart: '09:00',
  preferredTimeEnd: '21:00',
  quietModeStart: '22:00',
  quietModeEnd: '08:00',
};

export const NotificationSettingsTab = () => {
  const { t } = useTranslation();
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const [settings, setSettings] = useState<NotificationSettings>(DEFAULT_SETTINGS);

  // Carregar configurações
  useEffect(() => {
    const loadSettings = async () => {
      try {
        setIsLoading(true);
        const data = await notificationSettingsService.getNotificationSettings();
        if (data) {
          setSettings(data);
        }
      } catch {
        // Se não houver dados, usa defaults
        console.log('No notification settings found, using defaults');
      } finally {
        setIsLoading(false);
      }
    };

    loadSettings();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    setSuccess(false);

    try {
      await notificationSettingsService.updateNotificationSettings(settings);
      setSuccess(true);
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsSaving(false);
    }
  };

  const toggleChannel = (channel: NotificationChannel) => {
    setSettings((prev) => ({
      ...prev,
      activeChannels: {
        ...prev.activeChannels,
        [channel]: !prev.activeChannels[channel],
      },
    }));
    setSuccess(false);
    setError(null);
  };

  const toggleCategory = (category: NotificationCategory) => {
    setSettings((prev) => ({
      ...prev,
      categoryPreferences: {
        ...prev.categoryPreferences,
        [category]: {
          ...prev.categoryPreferences[category],
          active: !prev.categoryPreferences[category]?.active,
        },
      },
    }));
    setSuccess(false);
    setError(null);
  };

  const toggleCategoryChannel = (category: NotificationCategory, channel: NotificationChannel) => {
    setSettings((prev) => {
      const current = prev.categoryPreferences[category]?.channels || [];
      const newChannels = current.includes(channel)
        ? current.filter((c) => c !== channel)
        : [...current, channel];
      return {
        ...prev,
        categoryPreferences: {
          ...prev.categoryPreferences,
          [category]: {
            ...prev.categoryPreferences[category],
            channels: newChannels,
          },
        },
      };
    });
    setSuccess(false);
    setError(null);
  };

  const updateField = <K extends keyof NotificationSettings>(
    field: K,
    value: NotificationSettings[K]
  ) => {
    setSettings((prev) => ({ ...prev, [field]: value }));
    setSuccess(false);
    setError(null);
  };

  const getChannelLabel = (channel: NotificationChannel) => {
    const labels: Record<NotificationChannel, string> = {
      PUSH: t.profile.notificationSettings.channelPush,
      EMAIL: t.profile.notificationSettings.channelEmail,
      WHATSAPP: t.profile.notificationSettings.channelWhatsapp,
    };
    return labels[channel];
  };

  const getCategoryInfo = (category: NotificationCategory) => {
    const info: Record<NotificationCategory, { label: string; desc: string }> = {
      SECURITY: {
        label: t.profile.notificationSettings.categories.security,
        desc: t.profile.notificationSettings.categories.securityDesc,
      },
      STUDY_REMINDERS: {
        label: t.profile.notificationSettings.categories.studyReminders,
        desc: t.profile.notificationSettings.categories.studyRemindersDesc,
      },
      PROGRESS: {
        label: t.profile.notificationSettings.categories.progress,
        desc: t.profile.notificationSettings.categories.progressDesc,
      },
      CONTENT: {
        label: t.profile.notificationSettings.categories.content,
        desc: t.profile.notificationSettings.categories.contentDesc,
      },
      CLASS: {
        label: t.profile.notificationSettings.categories.class,
        desc: t.profile.notificationSettings.categories.classDesc,
      },
      MARKETING: {
        label: t.profile.notificationSettings.categories.marketing,
        desc: t.profile.notificationSettings.categories.marketingDesc,
      },
    };
    return info[category];
  };

  const frequencyOptions = Object.entries(t.enums.reminderFrequency).map(([value, label]) => ({
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
        {t.profile.notificationSettings.title}
      </h2>
      <p className="text-text-light mb-6">{t.profile.notificationSettings.subtitle}</p>

      {error && (
        <Alert variant="error" className="mb-6" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" className="mb-6" onClose={() => setSuccess(false)}>
          {t.profile.notificationSettings.successMessage}
        </Alert>
      )}

      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Seção: Canais */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.notificationSettings.channelsSection}
          </h3>
          <div className="space-y-4">
            {CHANNELS.map((channel) => (
              <Toggle
                key={channel}
                label={getChannelLabel(channel)}
                checked={settings.activeChannels[channel] || false}
                onChange={() => toggleChannel(channel)}
              />
            ))}
          </div>
        </section>

        {/* Seção: Categorias */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.notificationSettings.categoriesSection}
          </h3>
          <div className="space-y-4">
            {CATEGORIES.map((category) => {
              const info = getCategoryInfo(category);
              const pref = settings.categoryPreferences[category] || { active: false, channels: [] };
              return (
                <div key={category} className="p-4 bg-gray-50 rounded-2xl">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1">
                      <h4 className="font-medium text-text">{info.label}</h4>
                      <p className="text-sm text-text-light mt-1">{info.desc}</p>
                    </div>
                    <Toggle
                      checked={pref.active}
                      onChange={() => toggleCategory(category)}
                    />
                  </div>
                  {pref.active && (
                    <div className="mt-4 pt-4 border-t border-gray-200">
                      <p className="text-sm text-text-light mb-2">
                        {t.profile.notificationSettings.categoryChannels}:
                      </p>
                      <div className="flex flex-wrap gap-2">
                        {CHANNELS.filter((c) => settings.activeChannels[c]).map((channel) => (
                          <button
                            key={channel}
                            type="button"
                            onClick={() => toggleCategoryChannel(category, channel)}
                            className={`px-3 py-1 rounded-full text-sm transition-colors ${
                              pref.channels.includes(channel)
                                ? 'bg-primary text-white'
                                : 'bg-white text-text-light border border-gray-200 hover:border-primary'
                            }`}
                          >
                            {getChannelLabel(channel)}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </section>

        {/* Seção: Frequência */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.notificationSettings.remindersSection}
          </h3>
          <Dropdown
            label={t.profile.notificationSettings.reminderFrequency}
            value={settings.reminderFrequency}
            onChange={(value) => updateField('reminderFrequency', value as ReminderFrequency)}
            options={frequencyOptions}
            placeholder={t.profile.notificationSettings.selectFrequency}
          />
        </section>

        {/* Seção: Horários */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.notificationSettings.timeSection}
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-text mb-2">
                {t.profile.notificationSettings.preferredTimeStart}
              </label>
              <input
                type="time"
                value={settings.preferredTimeStart || ''}
                onChange={(e) => updateField('preferredTimeStart', e.target.value)}
                className="w-full rounded-full border border-gray-200 bg-white text-text px-5 py-3 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-text mb-2">
                {t.profile.notificationSettings.preferredTimeEnd}
              </label>
              <input
                type="time"
                value={settings.preferredTimeEnd || ''}
                onChange={(e) => updateField('preferredTimeEnd', e.target.value)}
                className="w-full rounded-full border border-gray-200 bg-white text-text px-5 py-3 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-text mb-2">
                {t.profile.notificationSettings.quietModeStart}
              </label>
              <input
                type="time"
                value={settings.quietModeStart || ''}
                onChange={(e) => updateField('quietModeStart', e.target.value)}
                className="w-full rounded-full border border-gray-200 bg-white text-text px-5 py-3 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-text mb-2">
                {t.profile.notificationSettings.quietModeEnd}
              </label>
              <input
                type="time"
                value={settings.quietModeEnd || ''}
                onChange={(e) => updateField('quietModeEnd', e.target.value)}
                className="w-full rounded-full border border-gray-200 bg-white text-text px-5 py-3 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
              />
            </div>
          </div>
          <p className="text-sm text-text-light mt-2">
            {t.profile.notificationSettings.quietModeHint}
          </p>
        </section>

        {/* Botão de salvar */}
        <div className="pt-4">
          <Button type="submit" disabled={isSaving} fullWidth>
            {isSaving
              ? t.profile.notificationSettings.saving
              : t.profile.notificationSettings.saveButton}
          </Button>
        </div>
      </form>
    </div>
  );
};
