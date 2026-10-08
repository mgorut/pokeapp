import { useContext } from 'react';
import { AuthContext, type AuthState } from './authContextCore';

/** Hook in its own module so AuthContext.tsx only exports the provider component. */
export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
