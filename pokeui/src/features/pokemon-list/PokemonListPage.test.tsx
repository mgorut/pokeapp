import { describe, expect, it, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders } from '../../test/test-utils';
import { PokemonListPage } from './PokemonListPage';
import * as api from '../../lib/api';
import type { PageResult, PokemonSummary } from '../../lib/types';

vi.mock('../../lib/api', async (importOriginal) => {
  const mod = await importOriginal<typeof import('../../lib/api')>();
  return { ...mod, fetchPokemonList: vi.fn() };
});

const summary: PokemonSummary = {
  id: 1, uuid: 'abc-uuid', name: 'bulbasaur', spriteUrl: 'https://x/b.png',
  category: 'seed', massKg: 6.9, skills: ['overgrow'],
};
const page: PageResult<PokemonSummary> = {
  page: 0, size: 12, totalElements: 1, totalPages: 1, items: [summary],
};

describe('PokemonListPage', () => {
  it('shows skeletons while loading', () => {
    vi.mocked(api.fetchPokemonList).mockReturnValue(new Promise(() => {})); // never resolves
    renderWithProviders(<PokemonListPage />);
    expect(screen.getByLabelText('Loading Pokémon')).toBeInTheDocument();
  });

  it('renders cards with name, category, mass and skills', async () => {
    vi.mocked(api.fetchPokemonList).mockResolvedValue(page);
    renderWithProviders(<PokemonListPage />);

    await waitFor(() => expect(screen.getByText('bulbasaur')).toBeInTheDocument());
    expect(screen.getByText('6.9 kg')).toBeInTheDocument();
    expect(screen.getByText('overgrow')).toBeInTheDocument();
    expect(screen.getByText('Synced locally')).toBeInTheDocument();
    expect(screen.getByText(/1 Pokémon/)).toBeInTheDocument();
  });

  it('shows an error state when the API fails', async () => {
    vi.mocked(api.fetchPokemonList).mockRejectedValue(
      Object.assign(new Error('down'), {
        isAxiosError: true,
        response: { status: 503, data: { status: 503, error: 'UPSTREAM', message: 'PokeAPI unavailable' } },
      }),
    );
    renderWithProviders(<PokemonListPage />);

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('PokeAPI unavailable');
  });

  it('disables Previous on first page and Next on last page', async () => {
    vi.mocked(api.fetchPokemonList).mockResolvedValue(page);
    renderWithProviders(<PokemonListPage />);
    await screen.findByText('bulbasaur');
    expect(screen.getByRole('button', { name: /previous/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /next/i })).toBeDisabled();
  });
});
