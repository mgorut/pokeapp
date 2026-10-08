import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useAuth } from 'X;
import { toApiError } from '../../lib/api';
import { credentialsSchema, type Credentials } from './authSchema';

/** US03 precondition: login form with client-side Zod validation + server error surfacing. */
export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Credentials>({ resolver: zodResolver(credentialsSchema) });

  const onSubmit = async (values: Credentials) => {
    setServerError(null);
    try {
      await login(values.email, values.password);
      navigate('/');
    } catch (err) {
      setServerError(toApiError(err).message);
    }
  };

  return (
    <main className="mx-auto mt-16 max-w-sm px-4">
      <h1 className="mb-6 text-2xl font-extrabold">Sign in</h1>
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        {serverError && (
          <p role="alert" className="mb-4 rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
            {serverError}
          </p>
        )}
        <Input label="Email" type="email" autoComplete="email" placeholder="demo@bla.com" error={errors.email?.message} {...register('email')} />
        <Input label="Password" type="password" autoComplete="current-password" placeholder="••••••••" error={errors.password?.message} {...register('password')} />
        <Button type="submit" loading={isSubmitting} className="w-full">Login</Button>
        <p className="mt-4 text-center text-sm text-slate-500">
          No account?{' '}
          <Link to="/register" className="font-semibold text-poke-red hover:underline">Register</Link>
        </p>
      </form>
    </main>
  );
}
