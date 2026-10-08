import { useCallback, useMemo, useState, type ReactNode } from 'react';
import { apiLogin, apiRegister, getStoredToken, storeToken } from '../lib/api';
import { AuthContext, type AuthState } from './authContextCore';

/**
 * Auth state lives in a small Context (not React Query) because the JWT is
 * not server data – it is a client-side session concern. Server state stays
 * exclusively in React Query caches.
 */
/** Decode only the `email` claim for display purposes – never trust contents for authz. */
function emailFromJwt(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return (payload.sub as string) ?? null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken());

  const persist = useCallback((t: string | null) => {
    storeToken(t);
    setToken(t);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const res = await apiLogin(email, password);
      persist(res.token);
    },
    [persist],
  );

  const register = useCallback(
    async (username: string, email: string, password: string) => {
      const res = await apiRegister(username, email, password);
      persist(res.token);
    },
    [persist],
  );

  const logout = useCallback(() => persist(null), [persist]);

  const value = useMemo<AuthState>(
    () => ({
      token,
      email: token ? emailFromJwt(token) : null,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [token, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
