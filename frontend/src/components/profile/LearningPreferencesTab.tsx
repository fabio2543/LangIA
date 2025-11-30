import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from '../../i18n';
import { MultiSelect } from '../common/MultiSelect';
import { Dropdown } from '../common/Dropdown';
import { CheckboxGroup } from '../common/CheckboxGroup';
import { RadioGroup } from '../common/RadioGroup';
import { Textarea } from '../common/Textarea';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { Alert } from '../common/Alert';
import { learningPreferencesService, languageService } from '../../services/profileService';
import { handleApiError } from '../../services/api';
import type {
  LearningPreferences,
  CefrLevel,
  TimeAvailable,
  StudyDayOfWeek,
  TimeOfDay,
  LearningFormat,
  LearningObjective,
  Language,
  LanguageEnrollment,
} from '../../types';

export const LearningPreferencesTab = () => {
  const { t, locale } = useTranslation();
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // Language enrollment states
  const [availableLanguages, setAvailableLanguages] = useState<Language[]>([]);
  const [enrollments, setEnrollments] = useState<LanguageEnrollment[]>([]);
  const [isAddingLanguage, setIsAddingLanguage] = useState(false);
  const [newLanguageCode, setNewLanguageCode] = useState('');
  const [newLanguageLevel, setNewLanguageLevel] = useState<CefrLevel | ''>('');

  const [formData, setFormData] = useState<LearningPreferences>({
    dailyTimeAvailable: null,
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

  // Carregar idiomas disponíveis
  const loadLanguages = useCallback(async () => {
    try {
      const [available, userEnrollments] = await Promise.all([
        languageService.getAvailableLanguages(),
        languageService.getEnrollments(),
      ]);
      setAvailableLanguages(available);
      setEnrollments(userEnrollments);
    } catch (err) {
      console.error('Error loading languages:', err);
    }
  }, []);

  // Carregar preferências e idiomas
  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        // Carregar idiomas e preferências em paralelo
        const [prefsData] = await Promise.all([
          learningPreferencesService.getLearningPreferences().catch(() => null),
          loadLanguages(),
        ]);

        if (prefsData) {
          setFormData({
            ...prefsData,
            preferredDays: prefsData.preferredDays || [],
            preferredTimes: prefsData.preferredTimes || [],
            topicsOfInterest: prefsData.topicsOfInterest || [],
            customTopics: prefsData.customTopics || [],
            preferredFormats: prefsData.preferredFormats || [],
            formatRanking: prefsData.formatRanking || [],
          });
        }
      } catch {
        console.log('No preferences found, using defaults');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [loadLanguages]);

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

  // Funções de gerenciamento de idiomas
  const getLanguageName = (lang: Language | LanguageEnrollment) => {
    if ('languageNamePt' in lang) {
      // LanguageEnrollment
      return locale === 'pt' ? lang.languageNamePt : locale === 'es' ? lang.languageNameEs : lang.languageNameEn;
    }
    // Language
    return locale === 'pt' ? lang.namePt : locale === 'es' ? lang.nameEs : lang.nameEn;
  };

  const handleAddLanguage = async () => {
    if (!newLanguageCode) return;

    try {
      setError(null);
      const enrollment = await languageService.enroll({
        languageCode: newLanguageCode,
        cefrLevel: newLanguageLevel || undefined,
        isPrimary: enrollments.length === 0, // First language is primary
      });
      setEnrollments((prev) => [...prev, enrollment]);
      setNewLanguageCode('');
      setNewLanguageLevel('');
      setIsAddingLanguage(false);
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    }
  };

  const handleUpdateLevel = async (languageCode: string, cefrLevel: CefrLevel) => {
    try {
      setError(null);
      const updated = await languageService.updateEnrollment(languageCode, { cefrLevel });
      setEnrollments((prev) =>
        prev.map((e) => (e.languageCode === languageCode ? updated : e))
      );
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    }
  };

  const handleSetPrimary = async (languageCode: string) => {
    try {
      setError(null);
      await languageService.setPrimary(languageCode);
      // Refresh all enrollments to get updated primary status
      await loadLanguages();
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    }
  };

  const handleRemoveLanguage = async (languageCode: string) => {
    try {
      setError(null);
      await languageService.unenroll(languageCode);
      setEnrollments((prev) => prev.filter((e) => e.languageCode !== languageCode));
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    }
  };

  // Available languages for adding (not yet enrolled)
  const availableForEnrollment = availableLanguages.filter(
    (lang) => !enrollments.some((e) => e.languageCode === lang.code)
  );

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
            {/* Lista de idiomas matriculados */}
            {enrollments.length > 0 ? (
              <div className="space-y-3">
                {enrollments.map((enrollment) => (
                  <div
                    key={enrollment.id}
                    className={`flex flex-col sm:flex-row sm:items-center gap-3 p-4 rounded-xl border ${
                      enrollment.isPrimary ? 'bg-primary/5 border-primary/30' : 'bg-gray-50 border-gray-200'
                    }`}
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-text">
                          {getLanguageName(enrollment)}
                        </span>
                        {enrollment.isPrimary && (
                          <span className="px-2 py-0.5 text-xs font-medium bg-primary text-white rounded-full">
                            {t.profile.learningPreferences.primaryLanguage}
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      {/* Dropdown de nível CEFR */}
                      <Dropdown
                        value={enrollment.cefrLevel || ''}
                        onChange={(value) => handleUpdateLevel(enrollment.languageCode, value as CefrLevel)}
                        options={cefrOptions}
                        placeholder={t.profile.learningPreferences.selectLevel || 'Nível'}
                        className="w-28"
                      />

                      {/* Botão definir como primário */}
                      {!enrollment.isPrimary && (
                        <button
                          type="button"
                          onClick={() => handleSetPrimary(enrollment.languageCode)}
                          className="p-2 text-primary hover:bg-primary/10 rounded-lg transition-colors"
                          title={t.profile.learningPreferences.setPrimary || 'Definir como principal'}
                        >
                          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                          </svg>
                        </button>
                      )}

                      {/* Botão remover */}
                      <button
                        type="button"
                        onClick={() => handleRemoveLanguage(enrollment.languageCode)}
                        className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                        title={t.profile.learningPreferences.removeLanguage || 'Remover idioma'}
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-text-light text-sm">
                {t.profile.learningPreferences.noLanguagesEnrolled || 'Nenhum idioma adicionado ainda.'}
              </p>
            )}

            {/* Adicionar novo idioma */}
            {enrollments.length < 3 && (
              <div className="mt-4">
                {isAddingLanguage ? (
                  <div className="p-4 bg-gray-50 rounded-xl border border-gray-200 space-y-3">
                    <Dropdown
                      label={t.profile.learningPreferences.selectNewLanguage || 'Novo idioma'}
                      value={newLanguageCode}
                      onChange={setNewLanguageCode}
                      options={availableForEnrollment.map((lang) => ({
                        value: lang.code,
                        label: getLanguageName(lang),
                      }))}
                      placeholder={t.profile.learningPreferences.selectLanguage || 'Selecione um idioma'}
                    />

                    <Dropdown
                      label={t.profile.learningPreferences.selfLevelOptional || 'Nível CEFR (opcional)'}
                      value={newLanguageLevel}
                      onChange={(value) => setNewLanguageLevel(value as CefrLevel | '')}
                      options={cefrOptions}
                      placeholder={t.profile.learningPreferences.selectLevel || 'Selecione...'}
                    />

                    <div className="flex gap-2">
                      <Button
                        type="button"
                        variant="primary"
                        onClick={handleAddLanguage}
                        disabled={!newLanguageCode}
                      >
                        {t.profile.learningPreferences.addLanguage || 'Adicionar'}
                      </Button>
                      <Button
                        type="button"
                        variant="secondary"
                        onClick={() => {
                          setIsAddingLanguage(false);
                          setNewLanguageCode('');
                          setNewLanguageLevel('');
                        }}
                      >
                        {t.profile.learningPreferences.cancel || 'Cancelar'}
                      </Button>
                    </div>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={() => setIsAddingLanguage(true)}
                    className="flex items-center gap-2 px-4 py-2 text-primary hover:bg-primary/10 rounded-lg transition-colors font-medium"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                    {t.profile.learningPreferences.addNewLanguage || 'Adicionar idioma'}
                  </button>
                )}
              </div>
            )}

            {enrollments.length >= 3 && (
              <p className="text-sm text-text-light">
                {t.profile.learningPreferences.maxLanguagesReached || 'Máximo de 3 idiomas atingido.'}
              </p>
            )}
          </div>
        </section>

        {/* Seção: Disponibilidade */}
        <section>
          <h3 className="text-lg font-medium text-text mb-4 pb-2 border-b">
            {t.profile.learningPreferences.availabilitySection}
          </h3>
          <div className="space-y-4">
            <Dropdown
              label={t.profile.learningPreferences.dailyTime}
              value={formData.dailyTimeAvailable || ''}
              onChange={(value) => updateField('dailyTimeAvailable', value as TimeAvailable)}
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

            <Dropdown
              label={t.profile.learningPreferences.objectiveDeadline}
              value={formData.objectiveDeadline || ''}
              onChange={(value) => updateField('objectiveDeadline', value)}
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
