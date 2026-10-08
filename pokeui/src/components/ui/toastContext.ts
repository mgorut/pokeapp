import { createContext } from 'react';

export type ToastKind = 'success' | 'error' | 'info';

/** Shared context object – separated from the provider component for lint-friendly Fast Refresh. */
export const ToastContext = createContext<{ push: (kind: ToastKind, message: string) => void } | undefined>(undefined);
