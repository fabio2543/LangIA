import { createContext } from 'react';
import type { AuthUser, LoginRequest, RegisterResponse, RegisterRequest } from '../types';
import type { ApiError } from '../services/api';

export interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<RegisterResponse>;
  logout: () => Promise<void>;
  error: ApiError | null;
  clearError: () => void;
  updateOnboardingCompleted: (completed: boolean) => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
