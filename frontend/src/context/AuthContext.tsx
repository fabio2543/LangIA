import { useState, useEffect, useCallback, type ReactNode } from 'react';
import type { AuthUser, LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../types';
import { authService, handleApiError, AUTH_ERROR_EVENT, type ApiError } from '../services/api';
import { AuthContext } from './AuthContextDef';

const STORAGE_KEYS = {
  // Dados do usuário em sessionStorage (não persiste entre abas/sessões)
  // Mais seguro que localStorage pois limpa ao fechar aba
  // Token JWT é gerenciado via cookie HttpOnly pelo backend
  USER: 'langia-user',
} as const;

// Usa sessionStorage em vez de localStorage para dados sensíveis
// sessionStorage é limpo quando a aba é fechada, reduzindo exposição a XSS
const secureStorage = {
  getItem: (key: string) => sessionStorage.getItem(key),
  setItem: (key: string, value: string) => sessionStorage.setItem(key, value),
  removeItem: (key: string) => sessionStorage.removeItem(key),
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  // Inicializa o estado de auth validando com o backend
  // O token JWT é gerenciado via cookie HttpOnly pelo backend
  // IMPORTANTE: Sempre valida com o backend, mesmo sem cache local,
  // pois o usuário pode ter um cookie HttpOnly válido de outra aba/sessão
  useEffect(() => {
    const initAuth = async () => {
      try {
        // Sempre valida sessão com o backend (cookie HttpOnly)
        const validation = await authService.validateSession();

        if (validation.valid && validation.session) {
          // Sessão válida - atualiza com dados frescos do backend
          const authUser: AuthUser = {
            id: validation.session.userId,
            name: validation.session.name,
            email: validation.session.email,
            profile: validation.session.profile,
            permissions: validation.session.permissions,
            onboardingCompleted: validation.session.onboardingCompleted ?? false,
          };
          secureStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authUser));
          setUser(authUser);
        } else {
          // Sessão inválida - limpa dados locais
          secureStorage.removeItem(STORAGE_KEYS.USER);
          setUser(null);
        }
      } catch {
        // Erro na validação (ex: backend indisponível) - limpa dados locais
        secureStorage.removeItem(STORAGE_KEYS.USER);
        setUser(null);
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  // Escuta eventos de erro de autenticação (401) do interceptor da API
  // Isso permite redirecionamento via React Router em vez de window.location
  useEffect(() => {
    const handleAuthError = () => {
      setUser(null);
      setError({ message: 'Sessão expirada. Faça login novamente.' });
    };

    window.addEventListener(AUTH_ERROR_EVENT, handleAuthError);
    return () => window.removeEventListener(AUTH_ERROR_EVENT, handleAuthError);
  }, []);

  // Salva apenas dados do usuário para UI (token está no cookie HttpOnly)
  const saveAuthData = (response: LoginResponse) => {
    const authUser: AuthUser = {
      id: response.userId,
      name: response.name,
      email: response.email,
      profile: response.profile,
      permissions: response.permissions,
      onboardingCompleted: response.onboardingCompleted ?? false,
    };

    secureStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authUser));
    setUser(authUser);
  };

  // Atualiza flag de onboarding
  const updateOnboardingCompleted = (completed: boolean) => {
    if (user) {
      const updatedUser = { ...user, onboardingCompleted: completed };
      secureStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(updatedUser));
      setUser(updatedUser);
    }
  };

  // Limpa dados locais (cookie é limpo pelo backend no logout)
  const clearAuthData = () => {
    secureStorage.removeItem(STORAGE_KEYS.USER);
    setUser(null);
  };

  const login = useCallback(async (data: LoginRequest) => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await authService.login(data);
      saveAuthData(response);
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError);
      throw apiError;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest): Promise<RegisterResponse> => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await authService.register(data);
      // Retorna a resposta para que o componente possa redirecionar
      // para a página de verificação de e-mail
      return response;
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError);
      throw apiError;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setIsLoading(true);

    try {
      await authService.logout();
    } catch {
      // Mesmo se a API falhar, faz logout local
    } finally {
      clearAuthData();
      setIsLoading(false);
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        error,
        clearError,
        updateOnboardingCompleted,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

