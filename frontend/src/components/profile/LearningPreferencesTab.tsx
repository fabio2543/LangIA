import { useState, useEffect } from 'react';
import { useTranslation } from '../../i18n';
import { MultiSelect } from '../common/MultiSelect';
import { Select } from '../common/Select';
import { CheckboxGroup } from '../common/CheckboxGroup';
import { RadioGroup } from '../common/RadioGroup';
import { Textarea } from '../common/Textarea';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { Alert } from '../common/Alert';
import { learningPreferencesService } from '../../services/profileService';
import { handleApiError } from '../../services/api';
import type {
  LearningPreferences,
  CefrLevel,
  TimeAvailable,
  StudyDayOfWeek,
  TimeOfDay,
  LearningFormat,
  LearningObjective,
} from '../../types';

export const LearningPreferencesTab = () => {
  const { t } = useTranslation();
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const [formData, setFormData] = useState<LearningPreferences>({
    studyLanguages: [],
    primaryLanguage: '',
    selfLevelByLanguage: {},
    dailyTimeAvailable: 'MIN_30' as TimeAvailable,
    preferredDays: [],
    preferredTimes: [],
    weeklyHoursGoal: 5,
    topicsOfInterest: [],
    customTopics: [],
    preferredFormats: [],
    formatRanking: [],
    primaryObjective: undefined,
    objectiveDescription: '',
    objectiveDeadline: '',
  });

  // Carregar preferências
  useEffect(() => {
    const loadPreferences = async () => {
      try {
        setIsLoading(true);
        const data = await learningPreferencesService.getLearningPreferences();
        if (data) {
          setFormData(data);
        }
      } catch (err) {
        // Se não houver dados, usa defaults
        console.log('No preferences found, using defaults');
      } finally {
        setIsLoading(false);
      }
    };

    loadPreferences();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    setSuccess(false);

    try {
      await learningPreferencesService.updateLearningPreferences(formData);
      setSuccess(true);
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsSaving(false);
    }
  };

  const updateField = <K extends keyof LearningPreferences>(
    field: K,
    value: LearningPreferences[K]
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setSuccess(false);
    setError(null);
  };

  const languageOptions = Object.entries(t.enums.languages).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const cefrOptions = Object.entries(t.enums.cefrLevel).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const timeAvailableOptions = Object.entries(t.enums.timeAvailable).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const dayOfWeekOptions = Object.entries(t.enums.dayOfWeek).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const timeOfDayOptions = Object.entries(t.enums.timeOfDay).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const learningFormatOptions = Object.entries(t.enums.learningFormat).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const objectiveOptions = Object.entries(t.enums.learningObjective).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const topicOptions = Object.entries(t.enums.topics).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const deadlineOptions = Object.entries(t.enums.deadlines).map(([value, label]) => ({
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
        {t.profile.learningPreferences.title}
      </h2>
      <p className="text-text-light mb-6">
        {t.profile.learningPreferences.subtitle}
      </p>

      {error && (
        <Alert variant="error" className="mb-6" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" className="mb-6" onClose={() => setSuccess(false)}>
          {t.profile.learningPreferences.successMessage}
        </Alert>
      )}

      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Seção: Idiomas */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.learningPreferences.languagesSection}
          </h3>
          <div className="space-y-4">
            <MultiSelect
              label={t.profile.learningPreferences.studyLanguages}
              options={languageOptions}
              value={formData.studyLanguages}
              onChange={(value) => updateField('studyLanguages', value)}
              maxItems={3}
              placeholder={t.profile.learningPreferences.studyLanguagesHint}
            />

            {formData.studyLanguages.length > 0 && (
              <Select
                label={t.profile.learningPreferences.primaryLanguage}
                value={formData.primaryLanguage}
                onChange={(e) => updateField('primaryLanguage', e.target.value)}
                options={formData.studyLanguages.map((lang) => ({
                  value: lang,
                  label: t.enums.languages[lang as keyof typeof t.enums.languages] || lang,
                }))}
                placeholder={t.profile.learningPreferences.selectPrimaryLanguage}
              />
            )}

            {/* Nível por idioma */}
            {formData.studyLanguages.map((lang) => (
              <Select
                key={lang}
                label={t.profile.learningPreferences.selfLevel.replace(
                  '{language}',
                  t.enums.languages[lang as keyof typeof t.enums.languages] || lang
                )}
                value={formData.selfLevelByLanguage[lang] || ''}
                onChange={(e) =>
                  updateField('selfLevelByLanguage', {
                    ...formData.selfLevelByLanguage,
                    [lang]: e.target.value as CefrLevel,
                  })
                }
                options={cefrOptions}
                placeholder="Selecione..."
              />
            ))}
          </div>
        </section>

        {/* Seção: Disponibilidade */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.learningPreferences.availabilitySection}
          </h3>
          <div className="space-y-4">
            <Select
              label={t.profile.learningPreferences.dailyTime}
              value={formData.dailyTimeAvailable}
              onChange={(e) => updateField('dailyTimeAvailable', e.target.value as TimeAvailable)}
              options={timeAvailableOptions}
              placeholder={t.profile.learningPreferences.selectDailyTime}
            />

            <CheckboxGroup
              label={t.profile.learningPreferences.preferredDays}
              options={dayOfWeekOptions}
              value={formData.preferredDays}
              onChange={(value) => updateField('preferredDays', value as StudyDayOfWeek[])}
              columns={2}
            />

            <CheckboxGroup
              label={t.profile.learningPreferences.preferredTimes}
              options={timeOfDayOptions}
              value={formData.preferredTimes || []}
              onChange={(value) => updateField('preferredTimes', value as TimeOfDay[])}
              direction="horizontal"
            />

            <div>
              <label className="block text-sm font-medium text-text mb-2">
                {t.profile.learningPreferences.weeklyGoal}
              </label>
              <Input
                type="number"
                value={formData.weeklyHoursGoal?.toString() || ''}
                onChange={(e) => updateField('weeklyHoursGoal', parseInt(e.target.value) || 0)}
                min={1}
                max={40}
              />
            </div>
          </div>
        </section>

        {/* Seção: Interesses */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.learningPreferences.interestsSection}
          </h3>
          <div className="space-y-4">
            <MultiSelect
              label={t.profile.learningPreferences.topics}
              options={topicOptions}
              value={formData.topicsOfInterest}
              onChange={(value) => updateField('topicsOfInterest', value)}
              placeholder={t.profile.learningPreferences.selectTopics}
            />
          </div>
        </section>

        {/* Seção: Formatos */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.learningPreferences.formatsSection}
          </h3>
          <div className="space-y-4">
            <CheckboxGroup
              label={t.profile.learningPreferences.preferredFormats}
              options={learningFormatOptions}
              value={formData.preferredFormats}
              onChange={(value) => updateField('preferredFormats', value as LearningFormat[])}
              columns={2}
            />
          </div>
        </section>

        {/* Seção: Objetivos */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.learningPreferences.objectivesSection}
          </h3>
          <div className="space-y-4">
            <RadioGroup
              label={t.profile.learningPreferences.primaryObjective}
              name="primaryObjective"
              options={objectiveOptions}
              value={formData.primaryObjective || ''}
              onChange={(value) => updateField('primaryObjective', value as LearningObjective)}
              columns={2}
            />

            <Textarea
              label={t.profile.learningPreferences.objectiveDescription}
              value={formData.objectiveDescription || ''}
              onChange={(e) => updateField('objectiveDescription', e.target.value)}
              placeholder={t.profile.learningPreferences.objectiveDescriptionPlaceholder}
              maxLength={500}
              showCount
            />

            <Select
              label={t.profile.learningPreferences.objectiveDeadline}
              value={formData.objectiveDeadline || ''}
              onChange={(e) => updateField('objectiveDeadline', e.target.value)}
              options={deadlineOptions}
              placeholder={t.profile.learningPreferences.selectDeadline}
            />
          </div>
        </section>

        {/* Botão de salvar */}
        <div className="pt-4">
          <Button type="submit" disabled={isSaving} fullWidth>
            {isSaving ? t.profile.learningPreferences.saving : t.profile.learningPreferences.saveButton}
          </Button>
        </div>
      </form>
    </div>
  );
};
