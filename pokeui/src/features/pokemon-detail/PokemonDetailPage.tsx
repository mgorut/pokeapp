import { Link, useNavigate, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchPokemonDetail, syncPokemon, toApiError } from '../../lib/api';
import { queryKeys } from '../../lib/queryKeys';
import type { PokemonDetail } from '../../lib/types';
import { useAuth } from '../../context/useAuth';
import { useToast } from '../../components/ui/useToast';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Skeleton } from '../../components/ui/Skeleton';
import { EvolutionTimeline } from './EvolutionTimeline';
import { StatBars } from './StatBars';

/**
 * US02 – detail view: hero sprite, narrative description, stats visualisation,
 * evolution lineage and the "Sync to Local" action (US03) for authenticated users.
 */
export function PokemonDetailPage() {
  const { idOrName = '' } = useParams();
  const { isAuthenticated } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data, isPending, error } = useQuery({
    queryKey: queryKeys.pokemonDetail(idOrName),
    queryFn: () => fetchPokemonDetail(idOrName),
  });

  const sync = useMutation({
    mutationFn: () => syncPokemon((data as PokemonDetail).id),
    onSuccess: (local) => {
      toast.push('success', `${local.name} synced to your local Pokédex.`);
      // Invalidate so the list shows the "Synced locally" badge & detail flips state.
      queryClient.invalidateQueries({ queryKey: ['pokemon'] });
      queryClient.setQueryData(queryKeys.localPokemon(local.uuid), local);
      navigate(`/pokemon/${local.uuid}/edit`, { state: { justSynced: true } });
    },
    onError: (err) => {
      const apiErr = toApiError(err);
      // 409 = already synced → friendly redirect instead of an error toast
      if (apiErr.status === 409) {
        toast.push('info', 'Already in your local Pokédex – opening it.');
        navigate('/');
      } else {
        toast.push('error', apiErr.message);
      }
    },
  });

  if (isPending) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <Skeleton className="h-64 w-full rounded-xl" />
        <Skeleton className="mt-4 h-6 w-48" />
        <Skeleton className="mt-2 h-4 w-full" />
      </main>
    );
  }

  if (error) {
    const apiErr = toApiError(error);
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-6 text-rose-700">
          <h2 className="font-bold">{apiErr.status === 404 ? 'Pokémon not found' : 'Failed to load detail'}</h2>
          <p className="mt-1 text-sm">{apiErr.message}</p>
          <Link to="/" className="mt-3 inline-block text-sm font-semibold underline">← Back to Pokédex</Link>
        </div>
      </main>
    );
  }


  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      {/* Hero */}
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-red-600 to-rose-800 p-6 text-white shadow-lg">
        <div className="flex flex-col items-center gap-6 sm:flex-row sm:items-end">
          <img src={data.image} alt={data.name} width={160} height={160} className="h-40 w-40 [image-rendering:pixelated] drop-shadow" />
          <div className="text-center sm:text-left">
            <p className="text-xs font-semibold uppercase tracking-widest text-red-200">#{String(data.id).padStart(4, '0')}</p>
            <h1 className="text-3xl font-extrabold capitalize">{data.name}</h1>
          </div>
          <div className="sm:ml-auto">
            {data.syncedLocally ? (
              <Link to={`/pokemon/${data.localUuid}/edit`}>
                <Button variant="secondary">Edit local entry</Button>
              </Link>
            ) : isAuthenticated ? (
              <Button variant="secondary" loading={sync.isPending} onClick={() => sync.mutate()}>
                Sync to Local
              </Button>
            ) : (
              <Link to="/login">
                <Button variant="secondary">Login to sync</Button>
              </Link>
            )}
          </div>
        </div>
      </section>

      {/* Narrative */}
      <Card className="mt-6">
        <h2 className="mb-2 text-sm font-bold uppercase tracking-wide text-slate-500">Field notes</h2>
        <p className="leading-relaxed text-slate-700">{data.narrativeDescription ?? 'No narrative available for this species yet.'}</p>
              </Card>

      {/* Stats + Evolution */}
      <div className="mt-6 grid grid-cols-1 gap-6 md:grid-cols-2">
        <Card>
          <h2 className="mb-4 text-sm font-bold uppercase tracking-wide text-slate-500">Base statistics</h2>
          <StatBars statistics={data.statistics} />
        </Card>
        <Card>
          <h2 className="mb-4 text-sm font-bold uppercase tracking-wide text-slate-500">Evolutionary lineage</h2>
          <EvolutionTimeline stages={data.evolutionaryLineage} currentId={data.id} />
        </Card>
      </div>

      <div className="mt-8">
        <Link to="/" className="text-sm font-semibold text-poke-red hover:underline">← Back to Pokédex</Link>
      </div>
    </main>
  );
}
