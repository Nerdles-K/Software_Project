import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// Vitest config kept separate from vite.config.ts so the Tailwind plugin (which
// scans CSS at build time) doesn't run during unit tests. jsdom gives the store
// tests a localStorage + atob; pure helper tests run fine in it too.
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.{test,spec}.ts'],
  },
})
