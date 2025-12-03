import type { OnboardingStep } from '../../types';

interface Step {
  id: OnboardingStep;
  label: string;
  icon: string;
}

const steps: Step[] = [
  { id: 'welcome', label: 'Perfil', icon: '1' },
  { id: 'language', label: 'Idioma', icon: '2' },
  { id: 'preferences', label: 'Preferencias', icon: '3' },
  { id: 'assessment', label: 'Avaliacao', icon: '4' },
  { id: 'complete', label: 'Trilha', icon: '5' },
];

interface OnboardingStepperProps {
  currentStep: OnboardingStep;
  onStepClick?: (step: OnboardingStep) => void;
}

export const OnboardingStepper = ({ currentStep, onStepClick }: OnboardingStepperProps) => {
  const currentIndex = steps.findIndex((s) => s.id === currentStep);

  return (
    <div className="w-full py-6">
      <div className="flex items-center justify-between max-w-2xl mx-auto px-4">
        {steps.map((step, index) => {
          const isCompleted = index < currentIndex;
          const isCurrent = step.id === currentStep;
          const isClickable = onStepClick && (isCompleted || isCurrent);

          return (
            <div key={step.id} className="flex items-center flex-1 last:flex-none">
              {/* Step Circle */}
              <button
                type="button"
                onClick={() => isClickable && onStepClick?.(step.id)}
                disabled={!isClickable}
                className={`
                  w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm
                  transition-all duration-200
                  ${
                    isCompleted
                      ? 'bg-primary text-white'
                      : isCurrent
                        ? 'bg-accent text-white ring-4 ring-accent/20'
                        : 'bg-gray-200 text-gray-500'
                  }
                  ${isClickable ? 'cursor-pointer hover:scale-110' : 'cursor-default'}
                `}
              >
                {isCompleted ? (
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                ) : (
                  step.icon
                )}
              </button>

              {/* Connector Line */}
              {index < steps.length - 1 && (
                <div
                  className={`
                    flex-1 h-1 mx-2 rounded-full transition-colors duration-200
                    ${index < currentIndex ? 'bg-primary' : 'bg-gray-200'}
                  `}
                />
              )}
            </div>
          );
        })}
      </div>

      {/* Step Labels (Mobile Hidden) */}
      <div className="hidden md:flex items-center justify-between max-w-2xl mx-auto px-4 mt-2">
        {steps.map((step, index) => {
          const isCompleted = index < currentIndex;
          const isCurrent = step.id === currentStep;

          return (
            <div key={`label-${step.id}`} className="flex-1 last:flex-none text-center">
              <span
                className={`
                  text-xs font-medium
                  ${isCompleted ? 'text-primary' : isCurrent ? 'text-accent' : 'text-gray-400'}
                `}
              >
                {step.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default OnboardingStepper;
