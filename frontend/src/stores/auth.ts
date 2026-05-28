import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', () => {
  const mode = ref<'child' | 'parent'>('child')
  const isAuthenticated = ref(false)

  const isChild = computed(() => mode.value === 'child')
  const isParent = computed(() => mode.value === 'parent')

  function switchMode(target: 'child' | 'parent', pin?: string) {
    if (target === 'parent' && pin !== '1234') {
      return false
    }
    mode.value = target
    isAuthenticated.value = true
    return true
  }

  return { mode, isAuthenticated, isChild, isParent, switchMode }
})
