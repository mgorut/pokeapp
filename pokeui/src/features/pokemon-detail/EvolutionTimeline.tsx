import { Link } from 'react-router-dom';
import type { EvolutionStage } from '../../lib/types';

interface Props {
  stages: EvolutionStage[];
  currentId: number;
}

/** Horizontal timeline of the evolutionary chain with sprites and min-level hints. */
export function EvolutionTimeline({ stages, currentId }: Props) {
  if (!stages.length) return <p className="text-sm text-slate-500">This Pokémon does not evolve.</p>;
  return (
    <ol className="flex items-center gap-2 overflow-x-auto pb-2">
      {stages.map((stage, i) => (
        <li key={stage.id} className="flex items-center gap-2">
          {i > 0 && (
            <span className="text-xs text-slate-400" title={stage.minLevel ? `Lv. ${stage.minLevel}` : 'Item/other'}>
              →{stage.minLevel ? ` ${stage.minLevel}` : ''}
            </span>
          )}
          <Link
            to={`/pokemon/${stage.id}`}
            className={`flex flex-col items-center rounded-lg p-2 transition hover:bg-slate-50 ${
              stage.id === currentId ? 'bg-red-50 ring-2 ring-red-300' : ''
            }`}
            aria-current={stage.id === currentId ? 'page' : undefined}
          >
            <img src={stage.spriteUrl} alt={stage.name} width={56} height={56} className="h-14 w-14 [image-rendering:pixelated]" />
            <span className="mt-1 text-xs font-semibold capitalize">{stage.name}</span>
          </Link>
        </li>
      ))}
    </ol>
  );
}
