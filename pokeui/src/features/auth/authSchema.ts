import { z } from 'zod';

/**
 * Zod schemas shared by Login/Register forms.
 * Password policy mirrors the backend @Valid rules (min 8 chars, 1 digit, 1 upper).
 */
export const passwordSchema = z
  .string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain an uppercase letter')
  .regex(/\d/, 'Password must contain a digit');

export const credentialsSchema = z.object({
  email: z.string().email('Enter a valid email address'),
  password: passwordSchema,
});

export type Credentials = z.infer<typeof credentialsSchema>;
