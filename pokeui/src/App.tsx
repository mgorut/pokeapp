// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './components/ui/Toast';
import { Navbar } from './components/ui/Navbar';
import { ProtectedRoute } from './features/auth/ProtectedRoute';
import { LoginPage } from './features/auth/LoginPage';
import { RegisterPage } from './features/auth/RegisterPage';
import { PokemonListPage } from './features/pokemon-list/PokemonListPage';
import { PokemonDetailPage } from './features/pokemon-detail/PokemonDetailPage';
import { PokemonEditPage } from './features/pokemon-edit/PokemonEditPage';
const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 2, staleTime: 60_000, refetchOnWindowFocus: true },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <BrowserRouter>
            <div className="flex min-h-full flex-col">
              <Navbar />
              <Routes>
                <Route path="/" element={<PokemonListPage />} />
                {/* US04 – editing requires an authenticated session (must come before :idOrName) */}
                <Route
                  path="/pokemon/:uuid/edit"
                  element={
                    <ProtectedRoute>
                      <PokemonEditPage />
                    </ProtectedRoute>
                  }
                />
                <Route path="/pokemon/:idOrName" element={<PokemonDetailPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </div>
          </BrowserRouter>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}
