// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

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
      {stages.map((stage) => (
        <li key={stage.stage} className="flex items-center gap-2">
          {/* No minLevel in current API, so omit arrow label */}
          {stage.stage !== currentId && <span className="text-xs text-slate-400">→</span>}
          <Link
            to={`/pokemon/${stage.pokeApiId}`}
            className={`flex flex-col items-center rounded-lg p-2 transition hover:bg-slate-50 ${
              stage.pokeApiId === currentId ? 'bg-red-50 ring-2 ring-red-300' : ''
            }`}
            aria-current={stage.pokeApiId === currentId ? 'page' : undefined}
          >
            <img src={stage.sprite} alt={stage.name} width={56} height={56} className="h-14 w-14 [image-rendering:pixelated]" />
            <span className="mt-1 text-xs font-semibold capitalize">{stage.name}</span>
          </Link>
        </li>
      ))}
    </ol>
  );
}
