import { useState, useEffect } from 'react';
import { useTranslation } from '../../i18n';
import { Select } from '../common/Select';
import { CheckboxGroup } from '../common/CheckboxGroup';
import { Button } from '../common/Button';
import { Alert } from '../common/Alert';
import { skillAssessmentService, languageService } from '../../services/profileService';
import { handleApiError } from '../../services/api';
import type { SkillAssessment, SkillAssessmentResponse, DifficultyLevel, CefrLevel, LanguageEnrollment } from '../../types';

const DIFFICULTY_LEVELS: DifficultyLevel[] = ['NONE', 'LOW', 'MODERATE', 'HIGH'];

export const SkillAssessmentTab = () => {
  const { t } = useTranslation();
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [showForm, setShowForm] = useState(false);

  const [assessments, setAssessments] = useState<SkillAssessmentResponse[]>([]);
  const [enrollments, setEnrollments] = useState<LanguageEnrollment[]>([]);
  const [noPrimaryLanguage, setNoPrimaryLanguage] = useState(false);

  const [formData, setFormData] = useState<SkillAssessment>({
    language: '',
    listeningDifficulty: 'MODERATE',
    speakingDifficulty: 'MODERATE',
    readingDifficulty: 'MODERATE',
    writingDifficulty: 'MODERATE',
    listeningDetails: [],
    speakingDetails: [],
    readingDetails: [],
    writingDetails: [],
    selfCefrLevel: undefined,
  });

  // Carregar avaliações e idiomas matriculados
  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        const [assessmentsData, enrollmentsData] = await Promise.all([
          skillAssessmentService.getSkillAssessments(),
          languageService.getEnrollments().catch(() => []),
        ]);
        setAssessments(assessmentsData);
        setEnrollments(enrollmentsData);
      } catch (err) {
        const apiError = handleApiError(err);
        setError(apiError.message);
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  // Pré-selecionar idioma primário quando enrollments carregarem
  useEffect(() => {
    if (enrollments.length > 0 && !formData.language) {
      const primaryEnrollment = enrollments.find((e) => e.isPrimary);
      if (primaryEnrollment) {
        setFormData((prev) => ({ ...prev, language: primaryEnrollment.languageCode }));
        setNoPrimaryLanguage(false);
      } else {
        setNoPrimaryLanguage(true);
      }
    }
  }, [enrollments, formData.language]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.language) {
      setError(t.profile.common.required);
      return;
    }

    setIsSaving(true);
    setError(null);
    setSuccess(false);

    try {
      const newAssessment = await skillAssessmentService.createSkillAssessment(formData);
      setAssessments((prev) => [newAssessment, ...prev]);
      setSuccess(true);
      setShowForm(false);
      resetForm();
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsSaving(false);
    }
  };

  const resetForm = () => {
    const primaryEnrollment = enrollments.find((e) => e.isPrimary);
    setFormData({
      language: primaryEnrollment?.languageCode || '',
      listeningDifficulty: 'MODERATE',
      speakingDifficulty: 'MODERATE',
      readingDifficulty: 'MODERATE',
      writingDifficulty: 'MODERATE',
      listeningDetails: [],
      speakingDetails: [],
      readingDetails: [],
      writingDetails: [],
      selfCefrLevel: undefined,
    });
  };

  const updateField = <K extends keyof SkillAssessment>(field: K, value: SkillAssessment[K]) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setSuccess(false);
    setError(null);
  };

  const getDifficultyLabel = (level: DifficultyLevel) => {
    const labels: Record<DifficultyLevel, string> = {
      NONE: t.profile.skillAssessment.difficultyNone,
      LOW: t.profile.skillAssessment.difficultyLow,
      MODERATE: t.profile.skillAssessment.difficultyModerate,
      HIGH: t.profile.skillAssessment.difficultyHigh,
    };
    return labels[level];
  };

  const difficultyOptions = DIFFICULTY_LEVELS.map((level) => ({
    value: level,
    label: getDifficultyLabel(level),
  }));

  const cefrOptions = Object.entries(t.enums.cefrLevel).map(([value, label]) => ({
    value,
    label: label as string,
  }));

  const languageOptions = enrollments.map((enrollment) => ({
    value: enrollment.languageCode,
    label: enrollment.languageNamePt, // TODO: Use locale-based name
  }));

  const listeningDetailOptions = Object.entries(t.profile.skillAssessment.listeningDetails).map(
    ([value, label]) => ({ value, label: label as string })
  );

  const speakingDetailOptions = Object.entries(t.profile.skillAssessment.speakingDetails).map(
    ([value, label]) => ({ value, label: label as string })
  );

  const readingDetailOptions = Object.entries(t.profile.skillAssessment.readingDetails).map(
    ([value, label]) => ({ value, label: label as string })
  );

  const writingDetailOptions = Object.entries(t.profile.skillAssessment.writingDetails).map(
    ([value, label]) => ({ value, label: label as string })
  );

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
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-semibold text-text mb-2">
            {t.profile.skillAssessment.title}
          </h2>
          <p className="text-text-light">{t.profile.skillAssessment.subtitle}</p>
        </div>
        {!showForm && (
          <Button onClick={() => setShowForm(true)}>
            {t.profile.skillAssessment.newAssessment}
          </Button>
        )}
      </div>

      {error && (
        <Alert variant="error" className="mb-6" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" className="mb-6" onClose={() => setSuccess(false)}>
          {t.profile.skillAssessment.successMessage}
        </Alert>
      )}

      {showForm ? (
        // Formulário de nova avaliação
        <form onSubmit={handleSubmit} className="space-y-6">
          {noPrimaryLanguage && (
            <Alert variant="warning">
              {t.profile.skillAssessment.noPrimaryLanguageWarning}
            </Alert>
          )}
          <Select
            label={t.profile.skillAssessment.selectLanguage}
            value={formData.language}
            onChange={(e) => updateField('language', e.target.value)}
            options={languageOptions}
            placeholder="Selecione..."
          />

          {/* Listening */}
          <div className="p-4 bg-gray-50 rounded-2xl space-y-4">
            <h4 className="font-medium text-text">{t.profile.skillAssessment.listening}</h4>
            <Select
              label={t.profile.skillAssessment.difficultyLabel}
              value={formData.listeningDifficulty}
              onChange={(e) => updateField('listeningDifficulty', e.target.value as DifficultyLevel)}
              options={difficultyOptions}
            />
            <CheckboxGroup
              label={t.profile.skillAssessment.detailsLabel}
              options={listeningDetailOptions}
              value={formData.listeningDetails || []}
              onChange={(value) => updateField('listeningDetails', value)}
              columns={2}
            />
          </div>

          {/* Speaking */}
          <div className="p-4 bg-gray-50 rounded-2xl space-y-4">
            <h4 className="font-medium text-text">{t.profile.skillAssessment.speaking}</h4>
            <Select
              label={t.profile.skillAssessment.difficultyLabel}
              value={formData.speakingDifficulty}
              onChange={(e) => updateField('speakingDifficulty', e.target.value as DifficultyLevel)}
              options={difficultyOptions}
            />
            <CheckboxGroup
              label={t.profile.skillAssessment.detailsLabel}
              options={speakingDetailOptions}
              value={formData.speakingDetails || []}
              onChange={(value) => updateField('speakingDetails', value)}
              columns={2}
            />
          </div>

          {/* Reading */}
          <div className="p-4 bg-gray-50 rounded-2xl space-y-4">
            <h4 className="font-medium text-text">{t.profile.skillAssessment.reading}</h4>
            <Select
              label={t.profile.skillAssessment.difficultyLabel}
              value={formData.readingDifficulty}
              onChange={(e) => updateField('readingDifficulty', e.target.value as DifficultyLevel)}
              options={difficultyOptions}
            />
            <CheckboxGroup
              label={t.profile.skillAssessment.detailsLabel}
              options={readingDetailOptions}
              value={formData.readingDetails || []}
              onChange={(value) => updateField('readingDetails', value)}
              columns={2}
            />
          </div>

          {/* Writing */}
          <div className="p-4 bg-gray-50 rounded-2xl space-y-4">
            <h4 className="font-medium text-text">{t.profile.skillAssessment.writing}</h4>
            <Select
              label={t.profile.skillAssessment.difficultyLabel}
              value={formData.writingDifficulty}
              onChange={(e) => updateField('writingDifficulty', e.target.value as DifficultyLevel)}
              options={difficultyOptions}
            />
            <CheckboxGroup
              label={t.profile.skillAssessment.detailsLabel}
              options={writingDetailOptions}
              value={formData.writingDetails || []}
              onChange={(value) => updateField('writingDetails', value)}
              columns={2}
            />
          </div>

          {/* CEFR Level */}
          <Select
            label={t.profile.skillAssessment.selfCefrLevel}
            value={formData.selfCefrLevel || ''}
            onChange={(e) => updateField('selfCefrLevel', e.target.value as CefrLevel)}
            options={cefrOptions}
            placeholder={t.profile.skillAssessment.selectCefrLevel}
          />
          <p className="text-sm text-text-light -mt-4">{t.profile.skillAssessment.cefrHint}</p>

          {/* Botões */}
          <div className="flex gap-3 pt-4">
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setShowForm(false);
                resetForm();
              }}
              className="flex-1"
            >
              {t.profile.skillAssessment.cancel}
            </Button>
            <Button type="submit" disabled={isSaving} className="flex-1">
              {isSaving ? t.profile.skillAssessment.saving : t.profile.skillAssessment.saveButton}
            </Button>
          </div>
        </form>
      ) : (
        // Lista de avaliações
        <div className="space-y-4">
          {assessments.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-5xl mb-4">📊</div>
              <p className="text-text-light mb-4">{t.profile.skillAssessment.noAssessments}</p>
              <Button onClick={() => setShowForm(true)}>
                {t.profile.skillAssessment.createFirst}
              </Button>
            </div>
          ) : (
            assessments.map((assessment) => (
              <div
                key={assessment.id}
                className="p-4 bg-gray-50 rounded-2xl"
              >
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium text-text">
                    {t.enums.languages[assessment.language as keyof typeof t.enums.languages] ||
                      assessment.language}
                  </h4>
                  <span className="text-sm text-text-light">
                    {t.profile.skillAssessment.assessedAt}{' '}
                    {new Date(assessment.assessedAt).toLocaleDateString()}
                  </span>
                </div>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  <div className="text-center p-2 bg-white rounded-lg">
                    <div className="text-xs text-text-light mb-1">
                      {t.profile.skillAssessment.listening}
                    </div>
                    <div className="font-medium text-sm">
                      {getDifficultyLabel(assessment.listeningDifficulty)}
                    </div>
                  </div>
                  <div className="text-center p-2 bg-white rounded-lg">
                    <div className="text-xs text-text-light mb-1">
                      {t.profile.skillAssessment.speaking}
                    </div>
                    <div className="font-medium text-sm">
                      {getDifficultyLabel(assessment.speakingDifficulty)}
                    </div>
                  </div>
                  <div className="text-center p-2 bg-white rounded-lg">
                    <div className="text-xs text-text-light mb-1">
                      {t.profile.skillAssessment.reading}
                    </div>
                    <div className="font-medium text-sm">
                      {getDifficultyLabel(assessment.readingDifficulty)}
                    </div>
                  </div>
                  <div className="text-center p-2 bg-white rounded-lg">
                    <div className="text-xs text-text-light mb-1">
                      {t.profile.skillAssessment.writing}
                    </div>
                    <div className="font-medium text-sm">
                      {getDifficultyLabel(assessment.writingDifficulty)}
                    </div>
                  </div>
                </div>
                {assessment.selfCefrLevel && (
                  <div className="mt-3 text-center">
                    <span className="inline-flex items-center px-3 py-1 bg-primary/10 text-primary rounded-full text-sm">
                      {t.enums.cefrLevel[assessment.selfCefrLevel]}
                    </span>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};
