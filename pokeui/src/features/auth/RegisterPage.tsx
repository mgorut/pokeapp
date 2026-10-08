import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../context/useAuth';
import { toApiError } from '../../lib/api';
import { useForm } from 'react-hook-form';

// Extend schema to include username validation (3-50 chars, non‑blank)
const passwordSchema = z
  .string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain an uppercase letter')
  .regex(/\d/, 'Password must contain a digit');

const registerSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters').max(50, 'Username too long'),
  email: z.string().email('Enter a valid email address'),
  password: passwordSchema,
});

type RegisterValues = z.infer<typeof registerSchema>;

/** Registration – on success the JWT is stored immediately and we land on the Pokédex. */
export function RegisterPage() {
  const { register: signUp } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterValues>({ resolver: zodResolver(registerSchema) });

  const onSubmit = async (values: RegisterValues) => {
    setServerError(null);
    try {
      await signUp(values.username, values.email, values.password);
      navigate('/');
    } catch (err) {
      const apiErr = toApiError(err);
      // Surface field‑level errors from GlobalExceptionHandler when present
      setServerError(apiErr.fieldErrors?.email ?? apiErr.message);
    }
  };

  return (
    <main className="mx-auto mt-16 max-w-sm px-4">
      <h1 className="mb-6 text-2xl font-extrabold">Create account</h1>
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        {serverError && (
          <p role="alert" className="mb-4 rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700">
            {serverError}
          </p>
        )}
        <Input label="Username" error={errors.username?.message} {...register('username')} />
        <Input label="Email" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} />
        <Input
          label="Password"
          type="password"
          autoComplete="new-password"
          helper="Min 8 chars, one uppercase letter and one digit"
          error={errors.password?.message}
          {...register('password')}
        />
        <Button type="submit" loading={isSubmitting} className="w-full">Register</Button>
        <p className="mt-4 text-center text-sm text-slate-500">
          Already registered?{' '}
          <Link to="/login" className="font-semibold text-poke-red hover:underline">Login</Link>
        </p>
      </form>
    </main>
  );
}
