import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E config — browser-level tests of the real UI.
 *
 * NOT run in CI yet / not executed in the sandbox: it needs the browser binaries
 * (`npx playwright install`) and a running frontend + backend. To run locally:
 *
 *   1. cd backend && ./gradlew bootRun          # API on :8080
 *   2. cd frontend && npm run dev               # app on :3000
 *   3. npm i -D @playwright/test && npx playwright install
 *   4. npm run test:e2e
 *
 * `webServer` below can auto-start the frontend; point baseURL at a running backend.
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  expect: { timeout: 5_000 },
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:3000',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  webServer: process.env.E2E_NO_SERVER
    ? undefined
    : {
        command: 'npm run dev',
        url: 'http://localhost:3000',
        reuseExistingServer: !process.env.CI,
        timeout: 60_000,
      },
})
