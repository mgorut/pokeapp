// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

import { Link, useSearchParams } from 'react-router-dom';
import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchPokemonList, toApiError } from '../../lib/api';
import { PAGE_SIZE, queryKeys } from '../../lib/queryKeys';
import { CardSkeleton } from '../../components/ui/Skeleton';
import { Button } from '../../components/ui/Button';
import { PokemonCard } from './PokemonCard';

const LIST_PAGE_KEY = 'pokeapp:lastListPage';

/**
 * US01 – paginated grid of Pokémon summaries.
 * Page is stored in URL query param (?page=N) so it survives navigation
 * and works with browser back/forward buttons.
 * If user enters an invalid page number (e.g., via direct URL), redirect to last valid page.
 * Current page is also saved to sessionStorage for reliable "Back to Pokédex" navigation.
 */
export function PokemonListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get('page') ?? '0');

  const { data, isPending, error } = useQuery({
    queryKey: queryKeys.pokemonList(page, PAGE_SIZE),
    queryFn: () => fetchPokemonList(page, PAGE_SIZE),
    placeholderData: (prev) => prev, // keep previous page visible while fetching → no flicker
  });
  useEffect(() => {
    sessionStorage.setItem(LIST_PAGE_KEY, String(page));
  }, [page]);
  useEffect(() => {
    if (data && page >= data.totalPages) {
      const lastValidPage = data.totalPages - 1;
      setSearchParams({ page: String(lastValidPage) }, { replace: true });
    }
  }, [data, page, setSearchParams]);

  const goToPage = (newPage: number) => {
    if (newPage < 0) return;
    if (data && newPage >= data.totalPages) return;
    setSearchParams({ page: String(newPage) }, { replace: true });
  };

  if (isPending) {
    return (
      <main className="mx-auto max-w-6xl px-4 py-8">
        <h1 className="mb-6 text-2xl font-extrabold">Pokédex</h1>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4" aria-label="Loading Pokémon">
          {Array.from({ length: PAGE_SIZE }).map((_, i) => (
            <CardSkeleton key={i} />
          ))}
        </div>
      </main>
    );
  }

  if (error) {
    const apiErr = toApiError(error);
    return (
      <main className="mx-auto max-w-6xl px-4 py-8">
        <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-6 text-rose-700">
          <h2 className="mb-1 font-bold">Could not load the Pokédex ({apiErr.status || 'network'})</h2>
          <p className="text-sm">{apiErr.message}</p>
          {apiErr.status === 503 && (
            <p className="mt-2 text-sm">PokeAPI seems unavailable – the list will retry automatically.</p>
          )}
        </div>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-6xl px-4 py-8">
      <div className="mb-6 flex items-end justify-between">
        <h1 className="text-2xl font-extrabold">Pokédex</h1>
        <p className="text-sm text-slate-500">{data.totalElements} Pokémon · page {data.page + 1}/{Math.max(data.totalPages, 1)}</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {data.content.map((p) => (
          <Link key={p.id} to={`/pokemon/${p.id}`} className="focus:outline-none">
            <PokemonCard pokemon={p} />
          </Link>
        ))}
      </div>

      <div className="mt-8 flex items-center justify-between">
        <Button variant="secondary" disabled={page === 0} onClick={() => goToPage(page - 1)}>
          ← Previous
        </Button>
        <Button disabled={data && page + 1 >= data.totalPages} onClick={() => goToPage(page + 1)}>
          Next →
        </Button>
      </div>
    </main>
  );
}
