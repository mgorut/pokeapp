// Copyright (c) 2026 Manuel Gorut. All Rights Reserved.
// This source code is licensed under the Restricted Use License found in the
// LICENSE.md file in the root directory of this source tree.

import { createContext } from 'react';

export type ToastKind = 'success' | 'error' | 'info';

/** Shared context object – separated from the provider component for lint-friendly Fast Refresh. */
export const ToastContext = createContext<{ push: (kind: ToastKind, message: string) => void } | undefined>(undefined);
