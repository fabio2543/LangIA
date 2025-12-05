import { useState, useCallback, useEffect, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { onboardingService } from '../services/onboardingService';
import { OnboardingContext } from './OnboardingContextDef';
import { logger } from '../utils/logger';
import type { OnboardingStatus, OnboardingStep, OnboardingCompleteResponse } from '../types';

const STEPS_ORDER: OnboardingStep[] = ['welcome', 'language', 'preferences', 'assessment', 'complete'];

export const OnboardingProvider = ({ children }: { children: ReactNode }) => {
  const navigate = useNavigate();
  const { updateOnboardingCompleted } = useAuth();
  const [status, setStatus] = useState<OnboardingStatus | null>(null);
  const [currentStep, setCurrentStep] = useState<OnboardingStep>('welcome');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadStatus = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const onboardingStatus = await onboardingService.getStatus();
      setStatus(onboardingStatus);

      // Define step inicial baseado no status
      if (onboardingStatus.nextStep) {
        setCurrentStep(onboardingStatus.nextStep);
      }
    } catch (err) {
      setError('Erro ao carregar status do onboarding');
      logger.error('Erro ao carregar status:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStatus();
  }, [loadStatus]);

  const goToStep = useCallback((step: OnboardingStep) => {
    setCurrentStep(step);
    navigate(`/onboarding/${step}`);
  }, [navigate]);

  const nextStep = useCallback(() => {
    const currentIndex = STEPS_ORDER.indexOf(currentStep);
    if (currentIndex < STEPS_ORDER.length - 1) {
      const next = STEPS_ORDER[currentIndex + 1];
      goToStep(next);
    }
  }, [currentStep, goToStep]);

  const prevStep = useCallback(() => {
    const currentIndex = STEPS_ORDER.indexOf(currentStep);
    if (currentIndex > 0) {
      const prev = STEPS_ORDER[currentIndex - 1];
      goToStep(prev);
    }
  }, [currentStep, goToStep]);

  const completeOnboarding = useCallback(async (): Promise<OnboardingCompleteResponse> => {
    logger.log('[OnboardingContext] completeOnboarding called');
    try {
      setIsLoading(true);
      setError(null);
      logger.log('[OnboardingContext] Calling onboardingService.complete...');
      const response = await onboardingService.complete();
      logger.log('[OnboardingContext] Response:', response);

      if (response.success) {
        // Atualiza o contexto de auth
        updateOnboardingCompleted(true);

        // Navega para a trilha ou dashboard
        if (response.redirectUrl) {
          navigate(response.redirectUrl);
        } else {
          navigate('/trails');
        }
      }

      return response;
    } catch (err) {
      const message = 'Erro ao completar onboarding';
      setError(message);
      logger.error(message, err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [navigate, updateOnboardingCompleted]);

  return (
    <OnboardingContext.Provider
      value={{
        status,
        currentStep,
        isLoading,
        error,
        loadStatus,
        goToStep,
        nextStep,
        prevStep,
        completeOnboarding,
      }}
    >
      {children}
    </OnboardingContext.Provider>
  );
};
