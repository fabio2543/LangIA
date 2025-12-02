import { useContext } from 'react';
import { AuthContext, type AuthContextType } from '../context/AuthContextDef';

/**
 * Hook para acessar o contexto de autenticação.
 * Separado do AuthContext.tsx para evitar warning de fast refresh.
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
