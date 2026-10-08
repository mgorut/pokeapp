import type { Statistic } from '../../lib/types';

const STAT_LABELS: Record<string, string> = {
  hp: 'HP', attack: 'Attack', defense: 'Defense',
  'special-attack': 'Sp. Atk', 'special-defense': 'Sp. Def', speed: 'Speed',
};

/** Horizontal bars for base stats; values are normalised against a sane max of 255. */
export function StatBars({ statistics }: { statistics: Statistic[] }) {
  if (!statistics.length) return <p className="text-sm text-slate-500">No statistics available.</p>;
  return (
    <ul className="space-y-3">
      {statistics.map((s) => (
        <li key={s.name}>
          <div className="mb-1 flex justify-between text-xs font-medium text-slate-600">
            <span>{STAT_LABELS[s.name] ?? s.name}</span>
            <span>{s.value}</span>
          </div>
          <div className="h-2 rounded-full bg-slate-100" role="meter" aria-valuenow={s.value} aria-valuemin={0} aria-valuemax={255} aria-label={s.name}>
            <div
              className="h-2 rounded-full bg-gradient-to-r from-red-500 to-rose-600"
              style={{ width: `${Math.min((s.value / 255) * 100, 100)}%` }}
            />
          </div>
        </li>
      ))}
    </ul>
  );
}
