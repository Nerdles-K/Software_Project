import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { api, setToken } from '../api/client'

interface LoginResponse {
  token: string
  role: 'child' | 'parent'
  familyId: string
}

export const useAuthStore = defineStore('auth', () => {
  const mode = ref<'child' | 'parent'>('child')
  const isAuthenticated = ref(false)
  const familyId = ref('')
  const error = ref('')

  const isChild = computed(() => mode.value === 'child')
  const isParent = computed(() => mode.value === 'parent')

  async function login(email: string, password: string) {
    error.value = ''
    try {
      const data = await api<LoginResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      })
      setToken(data.token)
      mode.value = data.role
      familyId.value = data.familyId
      isAuthenticated.value = true
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Login failed'
      return false
    }
  }

  async function register(email: string, password: string, role: 'child' | 'parent') {
    error.value = ''
    try {
      const data = await api<LoginResponse>('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ email, password, role }),
      })
      setToken(data.token)
      mode.value = data.role
      familyId.value = data.familyId
      isAuthenticated.value = true
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Registration failed'
      return false
    }
  }

  function logout() {
    setToken(null)
    mode.value = 'child'
    isAuthenticated.value = false
    familyId.value = ''
  }

  return { mode, isAuthenticated, isChild, isParent, familyId, error, login, register, logout }
})
