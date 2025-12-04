import { createContext } from 'react';
import type { OnboardingStatus, OnboardingStep, OnboardingCompleteResponse } from '../types';

export interface OnboardingContextType {
  status: OnboardingStatus | null;
  currentStep: OnboardingStep;
  isLoading: boolean;
  error: string | null;
  loadStatus: () => Promise<void>;
  goToStep: (step: OnboardingStep) => void;
  nextStep: () => void;
  prevStep: () => void;
  completeOnboarding: () => Promise<OnboardingCompleteResponse>;
}

export const OnboardingContext = createContext<OnboardingContextType | undefined>(undefined);
