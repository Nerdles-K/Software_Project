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
  if (token) {
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
