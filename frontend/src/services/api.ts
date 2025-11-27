import axios, { type AxiosError } from 'axios';
import type { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '../types';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar token JWT nas requisições
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('langia-token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para tratar erros de autenticação
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('langia-token');
      localStorage.removeItem('langia-user');
      window.location.href = '/login';
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
