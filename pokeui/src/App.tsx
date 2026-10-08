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

// One shared QueryClient: sensible defaults for a CRUD app (retry twice on
// transient network/5xx errors, refetch when the tab regains focus).
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
                <Route path="/pokemon/:idOrName" element={<PokemonDetailPage />} />
                {/* US04 – editing requires an authenticated session */}
                <Route
                  path="/pokemon/:uuid/edit"
                  element={
                    <ProtectedRoute>
                      <PokemonEditPage />
                    </ProtectedRoute>
                  }
                />
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
