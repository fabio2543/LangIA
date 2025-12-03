import { useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { OnboardingProvider, useOnboarding } from '../context/OnboardingContext';
import { OnboardingStepper } from '../components/onboarding';
import { useAuth } from '../hooks/useAuth';

const OnboardingContent = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const { currentStep, goToStep, isLoading, status } = useOnboarding();

  // Se ja completou onboarding, redireciona
  useEffect(() => {
    if (user?.onboardingCompleted) {
      navigate('/dashboard');
    }
  }, [user?.onboardingCompleted, navigate]);

  // Sincroniza URL com step atual
  useEffect(() => {
    const pathStep = location.pathname.split('/').pop();
    if (pathStep && pathStep !== currentStep && !isLoading) {
      // URL nao corresponde ao step atual - redireciona para o step correto
      navigate(`/onboarding/${currentStep}`, { replace: true });
    }
  }, [currentStep, location.pathname, navigate, isLoading]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-bg-warm flex items-center justify-center">
        <div className="text-center">
          <div className="text-4xl mb-4 animate-bounce">...</div>
          <p className="text-text-light">Carregando...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-bg-warm">
      {/* Header */}
      <header className="bg-text shadow-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-center h-16">
            <span className="flex items-center gap-1 text-2xl font-bold text-white">
              Lang<span className="text-accent">IA</span>
            </span>
          </div>
        </div>
      </header>

      {/* Stepper */}
      <OnboardingStepper
        currentStep={currentStep}
        onStepClick={(step) => {
          // Permite voltar para steps anteriores
          const stepOrder = ['welcome', 'language', 'preferences', 'assessment', 'complete'];
          const currentIndex = stepOrder.indexOf(currentStep);
          const clickedIndex = stepOrder.indexOf(step);
          if (clickedIndex <= currentIndex) {
            goToStep(step);
          }
        }}
      />

      {/* Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
    </div>
  );
};

export const OnboardingPage = () => {
  return (
    <OnboardingProvider>
      <OnboardingContent />
    </OnboardingProvider>
  );
};

export default OnboardingPage;
