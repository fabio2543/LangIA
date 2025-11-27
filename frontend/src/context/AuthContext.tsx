import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { AuthUser, LoginRequest, LoginResponse, RegisterRequest } from '../types';
import { authService, handleApiError, AUTH_ERROR_EVENT, type ApiError } from '../services/api';

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  error: ApiError | null;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_KEYS = {
  // Token agora é gerenciado via cookie HttpOnly (mais seguro contra XSS)
  USER: 'langia-user',
} as const;

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  // Inicializa o estado de auth a partir do localStorage (dados do usuário para UI)
  // O token JWT é gerenciado via cookie HttpOnly pelo backend
  useEffect(() => {
    const initAuth = () => {
      const storedUser = localStorage.getItem(STORAGE_KEYS.USER);

      if (storedUser) {
        try {
          const parsedUser = JSON.parse(storedUser) as AuthUser;
          setUser(parsedUser);
        } catch {
          // Dados corrompidos, limpa o storage
          localStorage.removeItem(STORAGE_KEYS.USER);
        }
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
    };

    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(authUser));
    setUser(authUser);
  };

  // Limpa dados locais (cookie é limpo pelo backend no logout)
  const clearAuthData = () => {
    localStorage.removeItem(STORAGE_KEYS.USER);
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

  const register = useCallback(async (data: RegisterRequest) => {
    setIsLoading(true);
    setError(null);

    try {
      await authService.register(data);
      // Após registro bem-sucedido, faz login automaticamente
      await login({ email: data.email, password: data.password });
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError);
      throw apiError;
    } finally {
      setIsLoading(false);
    }
  }, [login]);

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
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
