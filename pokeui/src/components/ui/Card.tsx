import type { HTMLAttributes } from 'react';

/** Simple elevated surface used by list cards and detail panels. */
export function Card({ className = '', ...rest }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      {...rest}
      className={`rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md ${className}`}
    />
  );
}
