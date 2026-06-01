<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function enterChildMode() {
  auth.switchMode('child')
  router.push('/child/pecs')
}

function enterParentMode() {
  const pin = prompt('Enter PIN:')
  if (auth.switchMode('parent', pin ?? undefined)) {
    router.push('/parent/pecs')
  }
}
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-8 bg-amber-50">
    <h1 class="text-5xl font-bold text-gray-900 tracking-tight">VisiTalk</h1>
    <p class="text-xl text-gray-600">Hello VisiTalk — Sprint 0</p>
    <div class="flex gap-6">
      <button
        @click="enterChildMode"
        class="px-10 py-6 text-2xl font-semibold rounded-2xl bg-orange-400 text-white shadow-lg hover:bg-orange-500 active:scale-95 transition-all"
        style="min-width: 160px; min-height: 100px;"
      >
        Child
      </button>
      <button
        @click="enterParentMode"
        class="px-10 py-6 text-2xl font-semibold rounded-2xl bg-blue-500 text-white shadow-lg hover:bg-blue-600 active:scale-95 transition-all"
        style="min-width: 160px; min-height: 100px;"
      >
        Parent
      </button>
    </div>
  </div>
</template>
