import axios, { type AxiosError } from 'axios';
import type { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '../types';

const API_BASE_URL = '/api';

// Evento customizado para notificar sobre erros de autenticação
// Permite que o AuthContext reaja sem usar window.location
export const AUTH_ERROR_EVENT = 'langia:auth-error';

export const dispatchAuthError = () => {
  window.dispatchEvent(new CustomEvent(AUTH_ERROR_EVENT));
};

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Essencial para envio automático de cookies HttpOnly
});

// Interceptor para tratar erros de autenticação
// Token agora é gerenciado via cookie HttpOnly (mais seguro contra XSS)
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Limpa dados locais do usuário (token não está mais no localStorage)
      localStorage.removeItem('langia-user');
      // Dispara evento para o AuthContext redirecionar via React Router
      dispatchAuthError();
    }
    return Promise.reject(error);
  }
);

// ============================================
// Auth Endpoints
// ============================================

export const authService = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<UserResponse> => {
    const response = await api.post<UserResponse>('/users/register', data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  validateSession: async (): Promise<{ valid: boolean; session: LoginResponse | null }> => {
    const response = await api.get('/auth/validate');
    return response.data;
  },

  renewSession: async (): Promise<{ message: string }> => {
    const response = await api.post('/auth/renew');
    return response.data;
  },
};

// ============================================
// Password Reset Endpoints
// ============================================

export interface ValidateTokenResponse {
  valid: boolean;
  email?: string;  // Masked email from backend
  error?: string;
  message?: string;
}

export interface ResetPasswordResponse {
  success: boolean;
  error?: string;
  message?: string;
}

export const passwordService = {
  forgotPassword: async (email: string): Promise<{ message: string }> => {
    const response = await api.post('/auth/password/forgot', { email });
    return response.data;
  },

  validateToken: async (token: string): Promise<ValidateTokenResponse> => {
    const response = await api.get(`/auth/password/reset/${token}`);
    return response.data;
  },

  resetPassword: async (token: string, password: string, passwordConfirmation: string): Promise<ResetPasswordResponse> => {
    const response = await api.post('/auth/password/reset', { token, password, passwordConfirmation });
    return response.data;
  },
};

// ============================================
// Error Handler
// ============================================

export interface ApiError {
  message: string;
  status?: number;
}

export const handleApiError = (error: unknown): ApiError => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string }>;
    return {
      message: axiosError.response?.data?.message || 'An unexpected error occurred',
      status: axiosError.response?.status,
    };
  }
  return { message: 'An unexpected error occurred' };
};

export default api;
