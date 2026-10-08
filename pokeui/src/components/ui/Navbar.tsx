import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';
import { Button } from './Button';

/** Top navigation: public browse links + auth-aware actions. */
export function Navbar() {
  const { isAuthenticated, email, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to="/" className="flex items-center gap-2 text-lg font-extrabold tracking-tight text-poke-red">
          <span aria-hidden>⬤</span> PokéManager
        </Link>
        <nav className="flex items-center gap-3 text-sm">
          <Link to="/" className="font-medium text-slate-600 hover:text-slate-900">Pokédex</Link>
          {isAuthenticated ? (
            <>
              <span className="hidden text-slate-500 sm:inline">{email}</span>
              <Button
                variant="secondary"
                onClick={() => {
                  logout();
                  navigate('/login');
                }}
              >
                Logout
              </Button>
            </>
          ) : (
            <Link to="/login" className="font-semibold text-poke-red hover:underline">Login</Link>
          )}
        </nav>
      </div>
    </header>
  );
}
