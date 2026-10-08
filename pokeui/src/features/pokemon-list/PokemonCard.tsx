import type { PokemonSummary } from '../../lib/types';
import { Card } from '../../components/ui/Card';

/** Presentational card: sprite, name, category, mass and skill chips (US01). */
export function PokemonCard({ pokemon }: { pokemon: PokemonSummary }) {
  return (
    <Card className="flex h-full flex-col items-center text-center">
      <img
        src={pokemon.sprite}
        alt={pokemon.name}
        loading="lazy"
        width={96}
        height={96}
        className="h-24 w-24 [image-rendering:pixelated]"
      />
      <h3 className="mt-2 font-bold capitalize">{pokemon.name}</h3>
      {pokemon.category && <p className="text-xs uppercase tracking-wide text-slate-500">{pokemon.category}</p>}
      {pokemon.mass != null && <p className="mt-1 text-xs text-slate-500">{pokemon.mass.toFixed(1)} kg</p>}
      {pokemon.skills.length > 0 && (
        <ul className="mt-3 flex flex-wrap justify-center gap-1" aria-label={`Skills of ${pokemon.name}`}>
          {pokemon.skills.slice(0, 4).map((s) => (
            <li key={s} className="rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-medium capitalize text-slate-600">
              {s}
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}
