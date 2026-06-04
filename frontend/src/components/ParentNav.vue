<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const tabs = [
  { to: '/parent/pecs', label: 'Cards' },
  { to: '/parent/chat', label: 'Chat' },
  { to: '/parent/schedule', label: 'Schedule' },
  { to: '/parent/behavior', label: 'Log' },
  { to: '/parent/report', label: 'Report' },
  { to: '/parent/settings', label: 'Settings' },
]

const isDark = computed(() =>
  route.path.startsWith('/parent/chat')
  || route.path.startsWith('/parent/behavior')
  || route.path.startsWith('/parent/report')
  || route.path.startsWith('/parent/settings'))

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <nav
    :class="isDark
      ? 'bg-slate-950 border-slate-800 text-slate-100'
      : 'bg-white border-gray-200 text-gray-800'"
    class="border-b sticky top-0 z-20">
    <div class="max-w-3xl mx-auto flex items-center justify-between px-4">
      <div class="flex">
        <router-link v-for="t in tabs" :key="t.to" :to="t.to"
          class="px-4 py-3 text-sm font-semibold border-b-2 transition-colors"
          :class="route.path === t.to || route.path.startsWith(t.to + '/')
            ? (isDark ? 'border-emerald-400 text-emerald-300' : 'border-orange-500 text-orange-600')
            : (isDark ? 'border-transparent text-slate-400 hover:text-slate-200' : 'border-transparent text-gray-500 hover:text-gray-800')">
          {{ t.label }}
        </router-link>
      </div>
      <button @click="logout"
        class="text-xs px-3 py-1.5 rounded"
        :class="isDark ? 'bg-slate-800 hover:bg-slate-700 text-slate-300' : 'bg-gray-100 hover:bg-gray-200 text-gray-600'">
        Logout
      </button>
    </div>
  </nav>
</template>
