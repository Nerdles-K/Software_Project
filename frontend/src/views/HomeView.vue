<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const loading = ref(false)
const tab = ref<'login' | 'register'>('login')
const role = ref<'parent' | 'child'>('parent')

async function handleSubmit() {
  loading.value = true
  const ok = tab.value === 'login'
    ? await auth.login(email.value, password.value)
    : await auth.register(email.value, password.value, role.value)
  loading.value = false
  if (ok) {
    router.push(auth.isChild ? '/child/pecs' : '/parent/pecs')
  }
}

function fillDemo(parent: boolean) {
  email.value = parent ? 'parent@test.com' : 'child@test.com'
  password.value = 'password123'
  tab.value = 'login'
}
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-6 bg-amber-50">
    <h1 class="text-5xl font-bold text-gray-900 tracking-tight">VisiTalk</h1>

    <!-- Tab switcher -->
    <div class="flex bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <button
        @click="tab = 'login'"
        :class="tab === 'login' ? 'bg-orange-400 text-white' : 'bg-white text-gray-500'"
        class="px-6 py-2 font-semibold text-lg transition-colors"
      >Login</button>
      <button
        @click="tab = 'register'"
        :class="tab === 'register' ? 'bg-orange-400 text-white' : 'bg-white text-gray-500'"
        class="px-6 py-2 font-semibold text-lg transition-colors"
      >Register</button>
    </div>

    <form @submit.prevent="handleSubmit" class="flex flex-col gap-4 w-80">
      <input
        v-model="email"
        type="email"
        placeholder="Email"
        required
        class="px-4 py-3 rounded-xl border border-gray-300 text-lg focus:outline-none focus:ring-2 focus:ring-orange-400"
      />
      <input
        v-model="password"
        type="password"
        placeholder="Password"
        required minlength="4"
        class="px-4 py-3 rounded-xl border border-gray-300 text-lg focus:outline-none focus:ring-2 focus:ring-orange-400"
      />

      <!-- Role selector (register only) -->
      <div v-if="tab === 'register'" class="flex gap-3">
        <label class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-xl border-2 cursor-pointer transition-colors"
          :class="role === 'parent' ? 'border-blue-500 bg-blue-50' : 'border-gray-200'">
          <input type="radio" v-model="role" value="parent" class="sr-only" />
          <span class="font-semibold text-gray-800">Parent</span>
        </label>
        <label class="flex-1 flex items-center justify-center gap-2 px-4 py-3 rounded-xl border-2 cursor-pointer transition-colors"
          :class="role === 'child' ? 'border-orange-400 bg-orange-50' : 'border-gray-200'">
          <input type="radio" v-model="role" value="child" class="sr-only" />
          <span class="font-semibold text-gray-800">Child</span>
        </label>
      </div>

      <button
        type="submit"
        :disabled="loading"
        class="px-6 py-3 text-xl font-semibold rounded-2xl bg-orange-400 text-white shadow-lg hover:bg-orange-500 active:scale-95 transition-all disabled:opacity-50"
        style="min-height: 56px;"
      >
        {{ loading ? 'Please wait...' : (tab === 'login' ? 'Login' : 'Register') }}
      </button>

      <p v-if="auth.error" class="text-red-500 text-center">{{ auth.error }}</p>

      <div class="flex gap-2 text-sm text-gray-400 justify-center mt-4">
        <button type="button" @click="fillDemo(true)" class="underline hover:text-blue-500">Demo: Parent</button>
        <span>|</span>
        <button type="button" @click="fillDemo(false)" class="underline hover:text-blue-500">Demo: Child</button>
      </div>
    </form>
  </div>
</template>
