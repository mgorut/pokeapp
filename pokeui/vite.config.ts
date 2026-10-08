import { defineConfig, mergeConfig } from 'vite';
import type { PluginOption } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig as defineVitestConfig } from 'vitest/config';

// Vite config: React + Tailwind v4 plugin (no postcss config needed).
// Dev proxy forwards /api to the Spring Boot backend so the browser never
// deals with CORS during local development. Vitest block powers component tests.
const viteConfig = defineConfig({
  plugins: [react(), tailwindcss() as PluginOption],
  envPrefix: ['VITE_'],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});

const vitestConfig = defineVitestConfig({
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    css: false,
  },
});

export default mergeConfig(viteConfig, vitestConfig);
