import axios, { type AxiosError } from 'axios';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  ResendVerificationRequest,
  ResendVerificationResponse,
  EmailVerificationResponse,
} from '../types';

const API_BASE_URL = '/api';

// Evento customizado para notificar sobre erros de autenticação
// Permite que o AuthContext reaja sem usar window.location
export const AUTH_ERROR_EVENT = 'langia:auth-error';

export const dispatchAuthError = () => {
  window.dispatchEvent(new CustomEvent(AUTH_ERROR_EVENT));
};

/**
 * Extrai o valor de um cookie pelo nome.
 * Usado para ler o token CSRF (XSRF-TOKEN) enviado pelo backend.
 */
const getCookie = (name: string): string | null => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(';').shift() || null;
  }
  return null;
};

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Essencial para envio automático de cookies HttpOnly
  xsrfCookieName: 'XSRF-TOKEN',  // Nome do cookie CSRF definido pelo Spring Security
  xsrfHeaderName: 'X-XSRF-TOKEN', // Header que o backend espera
});

// Interceptor para garantir que o token CSRF seja enviado em requisições mutáveis
api.interceptors.request.use(
  (config) => {
    // Para requisições que modificam dados (POST, PUT, DELETE, PATCH),
    // inclui o token CSRF do cookie no header
    const method = config.method?.toUpperCase();
    if (method && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
      const csrfToken = getCookie('XSRF-TOKEN');
      if (csrfToken) {
        config.headers['X-XSRF-TOKEN'] = csrfToken;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para tratar erros de autenticação
// Token agora é gerenciado via cookie HttpOnly (mais seguro contra XSS)
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Limpa dados locais do usuário (usa sessionStorage para dados sensíveis)
      sessionStorage.removeItem('langia-user');
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

  register: async (data: RegisterRequest): Promise<RegisterResponse> => {
    const response = await api.post<RegisterResponse>('/users/register', data);
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
// Email Verification Endpoints
// ============================================

export const emailVerificationService = {
  confirmEmail: async (token: string): Promise<EmailVerificationResponse> => {
    const response = await api.get<EmailVerificationResponse>(`/auth/email/confirm/${token}`);
    return response.data;
  },

  resendVerification: async (data: ResendVerificationRequest): Promise<ResendVerificationResponse> => {
    const response = await api.post<ResendVerificationResponse>('/auth/email/resend', data);
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
