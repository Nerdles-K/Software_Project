import { test, expect } from '@playwright/test'

/**
 * Browser-level E2E for the auth/route boundary. SCAFFOLD — see playwright.config.ts:
 * needs `npx playwright install` plus a running frontend (:3000) and backend (:8080).
 * Not executed in the sandbox / not yet wired into CI.
 *
 * This is the seed of the Workflow §9.3 E2E suite (drag-to-build sentence, schedule
 * run, behavior logging) — start here, then add one spec per core Story.
 */
test.describe('auth & routing', () => {
  test('landing page loads with a mode choice', async ({ page }) => {
    await page.goto('/')
    // The home view offers Child / Parent entry; assert the page rendered at all.
    await expect(page).toHaveURL(/\/$/)
    await expect(page.locator('body')).toBeVisible()
  })

  test('a protected route redirects to home when not authenticated', async ({ page }) => {
    await page.goto('/parent/behavior')
    // The router guard (meta.requiresAuth) should bounce an anonymous user back home.
    await expect(page).toHaveURL(/\/$/)
  })
})

/**
 * TODO (per Story, once selectors/test-ids are added to the views):
 *  - A-1/A-2: child taps cards → sentence builds in the strip → speak/clear works
 *  - B-2/B-3: child opens a schedule → checks off steps → progress persists
 *  - C-1:     parent logs a behavior event in ≤ 3 clicks
 *  - C-6/C-7: child writes a diary; parent dashboard shows only "written today", no content
 */
