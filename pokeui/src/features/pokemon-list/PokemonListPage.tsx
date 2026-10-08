import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchPokemonList, toApiError } from '../../lib/api';
import { PAGE_SIZE, queryKeys } from '../../lib/queryKeys';
import { CardSkeleton } from '../../components/ui/Skeleton';
import { Button } from '../../components/ui/Button';
import { PokemonCard } from './PokemonCard';

/**
 * US01 – paginated grid of Pokémon summaries.
 * React Query caches each page; the backend also caches (Caffeine 24h) so
 * repeated page loads are fast end-to-end.
 */
export function PokemonListPage() {
  const [page, setPage] = useState(0);

  const { data, isPending, error } = useQuery({
    queryKey: queryKeys.pokemonList(page, PAGE_SIZE),
    queryFn: () => fetchPokemonList(page, PAGE_SIZE),
    placeholderData: (prev) => prev, // keep previous page visible while fetching → no flicker
  });

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
        <Button variant="secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
          ← Previous
        </Button>
        <Button disabled={page + 1 >= data.totalPages} onClick={() => setPage((p) => p + 1)}>
          Next →
        </Button>
      </div>
    </main>
  );
}
