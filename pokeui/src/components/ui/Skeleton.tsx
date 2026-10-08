/** Loading skeleton blocks – avoids layout shift while queries are pending. */
export function Skeleton({ className = '' }: { className?: string }) {
  return <div className={`animate-pulse rounded-lg bg-slate-200 ${className}`} aria-hidden />;
}

export function CardSkeleton() {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <Skeleton className="mx-auto h-20 w-20 rounded-full" />
      <Skeleton className="mx-auto mt-3 h-4 w-24" />
      <Skeleton className="mx-auto mt-2 h-3 w-32" />
    </div>
  );
}
