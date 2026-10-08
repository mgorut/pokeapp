// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

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
