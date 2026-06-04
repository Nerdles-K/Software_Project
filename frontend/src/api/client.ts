/** Dev + Vite proxy: use '' so requests go to /api on the same host as the UI. */
const BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  (import.meta.env.DEV ? '' : 'http://localhost:8080')

function getToken(): string | null {
  return localStorage.getItem('visitalk_token')
}

export function setToken(token: string | null) {
  if (token) {
    localStorage.setItem('visitalk_token', token)
  } else {
    localStorage.removeItem('visitalk_token')
  }
}

export async function api<T = unknown>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {}),
  }
  // Don't send bearer token to login/register — a stale localStorage token would
  // otherwise be rejected by the JWT filter before the auth controller runs.
  const isPublic = path.startsWith('/api/auth/')
  if (token && !isPublic) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  })

  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error((body as { error?: string }).error || `HTTP ${res.status}`)
  }

  return res.json()
}

export async function uploadFile<T = { url: string }>(
  path: string,
  file: File
): Promise<T> {
  const token = getToken()
  const form = new FormData()
  form.append('file', file)
  const headers: Record<string, string> = {}
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    body: form,
    headers,
  })

  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error((body as { error?: string }).error || `HTTP ${res.status}`)
  }
  return res.json()
}

export function assetUrl(path: string | undefined | null): string {
  if (!path) return ''
  if (/^https?:/i.test(path)) return path
  if (path.startsWith('/uploads/')) return `${BASE_URL}${path}`
  return path
}
