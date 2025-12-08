import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from '../i18n';

type Step = 1 | 2 | 3;

interface Language {
  code: string;
  name: string;
  flag: string;
}

interface Goal {
  id: string;
  name: string;
  emoji: string;
}

const LANGUAGES: Language[] = [
  { code: 'en', name: 'English', flag: '🇬🇧' },
  { code: 'es', name: 'Español', flag: '🇪🇸' },
  { code: 'fr', name: 'Français', flag: '🇫🇷' },
  { code: 'de', name: 'Deutsch', flag: '🇩🇪' },
  { code: 'it', name: 'Italiano', flag: '🇮🇹' },
  { code: 'pt', name: 'Português', flag: '🇧🇷' },
];

const GOALS: Goal[] = [
  { id: 'career', name: 'Career', emoji: '💼' },
  { id: 'travel', name: 'Travel', emoji: '🌍' },
  { id: 'hobby', name: 'Self-Improvement', emoji: '⭐' },
  { id: 'family', name: 'Family/Friends', emoji: '👥' },
];

export const OnboardingPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>(1);
  const [, setSelectedLanguage] = useState<string | null>(null);
  const [, setSelectedLevel] = useState<string | null>(null);
  const [, setSelectedGoal] = useState<string | null>(null);

  const handleNext = () => {
    if (step < 3) {
      setStep((step + 1) as Step);
    } else {
      // Save preferences and redirect to dashboard
      navigate('/dashboard');
    }
  };

  const handleBack = () => {
    if (step > 1) {
      setStep((step - 1) as Step);
    }
  };

  const getStepTitle = () => {
    switch (step) {
      case 1:
        return t.onboarding?.step1Title || 'What language do you want to learn?';
      case 2:
        return t.onboarding?.step2Title || 'What is your current level?';
      case 3:
        return t.onboarding?.step3Title || 'What is your main goal?';
      default:
        return '';
    }
  };

  return (
    <div className="min-h-screen bg-bg-warm flex flex-col items-center justify-center p-6">
      <div className="w-full max-w-md space-y-8">
        {/* Header with Logo */}
        <div className="text-center space-y-2">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-xl bg-primary text-white font-serif text-2xl font-bold italic mb-4">
            L
          </div>
          {/* Progress Bar */}
          <div className="h-2 w-full bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-primary rounded-full transition-all duration-500"
              style={{ width: `${(step / 3) * 100}%` }}
            />
          </div>
        </div>

        {/* Main Content */}
        <div className="space-y-6">
          {/* Title */}
          <div className="text-center">
            <h1 className="font-serif text-3xl font-bold text-indigo-950">
              {getStepTitle()}
            </h1>
            <p className="text-muted-foreground mt-2">
              {step === 1 && 'Choose one to start your journey.'}
              {step === 2 && "We'll adapt the content to you."}
              {step === 3 && 'Help us personalize your exercises.'}
            </p>
          </div>

          {/* Step 1: Language Selection */}
          {step === 1 && (
            <div className="grid grid-cols-1 gap-3 animate-in fade-in duration-500">
              {LANGUAGES.map((lang) => (
                <button
                  key={lang.code}
                  onClick={() => {
                    setSelectedLanguage(lang.code);
                    handleNext();
                  }}
                  className="group relative flex items-center p-4 rounded-2xl border-2 border-transparent bg-white shadow-sm hover:border-primary/50 hover:shadow-md transition-all text-left"
                >
                  <span className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-50 text-xl group-hover:bg-indigo-100 transition-colors">
                    {lang.flag}
                  </span>
                  <span className="ml-4 font-medium text-lg text-indigo-950">{lang.name}</span>
                  <span className="ml-auto text-muted-foreground opacity-0 group-hover:opacity-100 transition-all">
                    →
                  </span>
                </button>
              ))}
            </div>
          )}

          {/* Step 2: Level Selection */}
          {step === 2 && (
            <div className="space-y-3 animate-in fade-in duration-500">
              {[
                { code: 'A1', name: 'Beginner (A1)', desc: t.onboarding?.levels?.A1 || "I'm starting from scratch" },
                { code: 'A2', name: 'Elementary (A2)', desc: t.onboarding?.levels?.A2 || 'I know basic phrases' },
                { code: 'B1', name: 'Intermediate (B1-B2)', desc: t.onboarding?.levels?.B1 || 'I can have conversations' },
                { code: 'C1', name: 'Advanced (C1-C2)', desc: t.onboarding?.levels?.C1 || "I'm fluent or near-fluent" },
              ].map((level) => (
                <button
                  key={level.code}
                  onClick={() => {
                    setSelectedLevel(level.code);
                    handleNext();
                  }}
                  className="w-full p-5 rounded-2xl border-2 border-transparent bg-white shadow-sm hover:border-primary/50 hover:shadow-md transition-all text-left"
                >
                  <div className="font-semibold text-lg text-indigo-950">{level.name}</div>
                  <div className="text-muted-foreground">{level.desc}</div>
                </button>
              ))}
            </div>
          )}

          {/* Step 3: Goal Selection */}
          {step === 3 && (
            <div className="grid grid-cols-2 gap-4 animate-in fade-in duration-500">
              {GOALS.map((goal) => (
                <button
                  key={goal.id}
                  onClick={() => {
                    setSelectedGoal(goal.id);
                    handleNext();
                  }}
                  className="flex flex-col items-center justify-center p-6 rounded-2xl border-2 border-transparent bg-white shadow-sm hover:border-primary/50 hover:shadow-md hover:-translate-y-1 transition-all"
                >
                  <div className="h-12 w-12 rounded-full bg-indigo-50 flex items-center justify-center text-primary mb-3">
                    <span className="text-2xl">{goal.emoji}</span>
                  </div>
                  <span className="font-medium text-indigo-950">
                    {goal.name}
                  </span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Back button for steps 2 and 3 */}
        {step > 1 && (
          <div className="mt-6 text-center">
            <button
              onClick={handleBack}
              className="text-muted-foreground hover:text-primary transition-colors"
            >
              ← {t.onboarding?.back || 'Back'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
