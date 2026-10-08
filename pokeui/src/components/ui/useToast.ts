import { useContext } from 'react';
import { ToastContext } from './toastContext';

/** Hook lives in its own module so Toast.tsx only exports components (Fast Refresh). */
export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
