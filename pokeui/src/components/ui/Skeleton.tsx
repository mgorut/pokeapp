// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

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
