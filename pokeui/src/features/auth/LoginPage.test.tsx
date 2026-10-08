// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

import { describe, expect, it, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../test/test-utils';
import { LoginPage } from './LoginPage';
import * as api from '../../lib/api';

vi.mock('../../lib/api', async (importOriginal) => {
  const mod = await importOriginal<typeof import('../../lib/api')>();
  return { ...mod, apiLogin: vi.fn() };
});

describe('LoginPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders email/password fields and submit button', () => {
    renderWithProviders(<LoginPage />);
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeEnabled();
  });

  it('blocks submission with invalid data and shows Zod errors', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);
    await user.type(screen.getByLabelText(/email/i), 'not-an-email');
    await user.type(screen.getByLabelText(/password/i), 'weak');
    await user.click(screen.getByRole('button', { name: /login/i }));

    expect(await screen.findByText(/valid email/i)).toBeInTheDocument();
    expect(screen.getByText(/at least 8 characters/i)).toBeInTheDocument();
    expect(api.apiLogin).not.toHaveBeenCalled();
  });

  it('calls the API and stores the token on success', async () => {
    vi.mocked(api.apiLogin).mockResolvedValue({ token: 'jwt-token', email: 'demo@bla.com' });
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'demo@bla.com');
    await user.type(screen.getByLabelText(/password/i), 'Demo123!');
    await user.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => expect(api.apiLogin).toHaveBeenCalledWith('demo@bla.com', 'Demo123!'));
    await waitFor(() => expect(localStorage.getItem('pokemanager.jwt')).toBe('jwt-token'));
  });

  it('surfaces server error messages', async () => {
    const axiosErr = Object.assign(new Error('Request failed'), {
      isAxiosError: true,
      response: { status: 401, data: { status: 401, error: 'UNAUTHORIZED', message: 'Invalid credentials' } },
    });
    vi.mocked(api.apiLogin).mockRejectedValue(axiosErr);
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'demo@bla.com');
    await user.type(screen.getByLabelText(/password/i), 'WrongPass1');
    await user.click(screen.getByRole('button', { name: /login/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid credentials');
  });
});
