import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth'

// Builds a JWT-shaped string whose payload decodes to the given claims. Only the
// payload segment matters to the store (it never verifies the signature client-side).
function fakeJwt(claims: Record<string, unknown>): string {
  return `header.${btoa(JSON.stringify(claims))}.signature`
}

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })
  afterEach(() => vi.restoreAllMocks())

  it('hydrates role + familyId from a stored token on creation', () => {
    localStorage.setItem('visitalk_token', fakeJwt({ role: 'child', family_id: 'FAM999' }))
    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(true)
    expect(auth.isChild).toBe(true)
    expect(auth.familyId).toBe('FAM999')
  })

  it('treats a parent token as parent mode', () => {
    localStorage.setItem('visitalk_token', fakeJwt({ role: 'parent', family_id: 'FAM001' }))
    const auth = useAuthStore()
    expect(auth.isParent).toBe(true)
  })

  it('stays logged out for a malformed token', () => {
    localStorage.setItem('visitalk_token', 'not-a-real-jwt')
    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(false)
  })

  it('logout clears token, family and auth flag', () => {
    localStorage.setItem('visitalk_token', fakeJwt({ role: 'parent', family_id: 'FAM001' }))
    const auth = useAuthStore()
    auth.logout()
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.familyId).toBe('')
    expect(localStorage.getItem('visitalk_token')).toBeNull()
  })

  it('login stores the token and sets mode from the API response', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ token: fakeJwt({ role: 'parent', family_id: 'FAMABC' }), role: 'parent', familyId: 'FAMABC' }),
    })
    vi.stubGlobal('fetch', fetchMock)

    const auth = useAuthStore()
    const ok = await auth.login('p@test.com', 'secret')

    expect(ok).toBe(true)
    expect(auth.isParent).toBe(true)
    expect(auth.familyId).toBe('FAMABC')
    expect(localStorage.getItem('visitalk_token')).not.toBeNull()
  })

  it('login surfaces an error message on bad credentials', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({ error: 'Invalid email or password' }),
    })
    vi.stubGlobal('fetch', fetchMock)

    const auth = useAuthStore()
    const ok = await auth.login('p@test.com', 'wrong')

    expect(ok).toBe(false)
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.error).toBe('Invalid email or password')
  })
})
