import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach, vi } from 'vitest';

// Ensure each test starts with a clean DOM and no leaked timers/mocks.
afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
  localStorage.clear();
});
