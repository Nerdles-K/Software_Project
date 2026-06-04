<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useBehaviorStore, TRIGGER_TAGS } from '../../../stores/behavior'
import { useAuthStore } from '../../../stores/auth'
import { useRouter } from 'vue-router'
import ParentNav from '../../../components/ParentNav.vue'

const store = useBehaviorStore()
const auth = useAuthStore()
const router = useRouter()

// C-1: state machine with exactly 3 clicks: intensity → trigger → submit.
type Step = 'intensity' | 'trigger' | 'review'
const step = ref<Step>('intensity')
const intensity = ref<number | null>(null)
const trigger = ref<string | null>(null)
const submitting = ref(false)
const justLogged = ref<string | null>(null)
const startedAt = ref<number>(Date.now())
const elapsed = ref<number | null>(null)

onMounted(() => {
  void store.fetchEvents(20)
})

function pickIntensity(v: number) {
  intensity.value = v
  step.value = 'trigger'
  // click 1
}

function pickTrigger(t: string) {
  trigger.value = t
  step.value = 'review'
  // click 2
}

async function submit() {
  if (intensity.value == null || trigger.value == null) return
  submitting.value = true
  try {
    await store.logEvent(intensity.value, [trigger.value])
    elapsed.value = (Date.now() - startedAt.value) / 1000
    justLogged.value = `${trigger.value} @ intensity ${intensity.value}`
    reset()
  } finally {
    submitting.value = false
  }
}

function reset() {
  step.value = 'intensity'
  intensity.value = null
  trigger.value = null
  startedAt.value = Date.now()
}

// C-2: PIN re-entry required to leave parent mode
const exiting = ref(false)
const pin = ref('')
const pinError = ref('')

function tryExit() {
  exiting.value = true
  pin.value = ''
  pinError.value = ''
}

function confirmExit() {
  if (pin.value !== '1234') {
    pinError.value = 'Wrong PIN'
    return
  }
  auth.logout()
  router.push('/')
}

const intensityLabels = ['', 'Mild', 'Low', 'Med', 'High', 'Severe']
</script>

<template>
  <ParentNav />
  <!-- C-2: dark, dense, professional UI deliberately UNLIKE child mode -->
  <div class="min-h-screen bg-slate-900 text-slate-100 px-4 sm:px-8 py-6">
    <header class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold tracking-tight">Behavior Logger</h1>
        <p class="text-xs text-slate-400">Parent mode · private to caregivers</p>
      </div>
      <button @click="tryExit"
        class="px-4 py-2 rounded-md bg-slate-800 border border-slate-700 text-sm hover:bg-slate-700">
        Exit (PIN)
      </button>
    </header>

    <!-- C-1: log card -->
    <section class="max-w-xl mx-auto bg-slate-800 rounded-lg shadow-lg p-5 border border-slate-700">
      <div class="flex items-center justify-between mb-4">
        <h2 class="font-semibold text-base">Log event</h2>
        <span class="text-xs text-slate-500">
          step {{ step === 'intensity' ? 1 : step === 'trigger' ? 2 : 3 }} of 3
        </span>
      </div>

      <!-- Step 1 — Intensity -->
      <div v-if="step === 'intensity'">
        <p class="text-sm text-slate-300 mb-3">Tap intensity (1 click)</p>
        <div class="grid grid-cols-5 gap-2">
          <button v-for="n in 5" :key="n" @click="pickIntensity(n)"
            class="py-4 rounded-md font-bold text-lg border border-slate-600 hover:bg-slate-700 transition-colors">
            <div>{{ n }}</div>
            <div class="text-[10px] font-normal text-slate-400 mt-1">{{ intensityLabels[n] }}</div>
          </button>
        </div>
      </div>

      <!-- Step 2 — Trigger -->
      <div v-else-if="step === 'trigger'">
        <p class="text-sm text-slate-300 mb-3">Tap trigger tag (1 click)</p>
        <div class="grid grid-cols-2 gap-2">
          <button v-for="tag in TRIGGER_TAGS" :key="tag" @click="pickTrigger(tag)"
            class="px-3 py-3 rounded-md text-sm border border-slate-600 hover:bg-slate-700 text-left">
            {{ tag.replace(/_/g, ' ') }}
          </button>
        </div>
        <button @click="step = 'intensity'"
          class="mt-3 text-xs text-slate-400 hover:text-slate-200">← back</button>
      </div>

      <!-- Step 3 — Review + submit -->
      <div v-else>
        <p class="text-sm text-slate-300 mb-3">Confirm (1 click)</p>
        <div class="bg-slate-900 rounded-md p-4 mb-3 border border-slate-700">
          <div class="flex justify-between text-sm">
            <span class="text-slate-400">Intensity</span>
            <span class="font-bold">{{ intensity }} / 5</span>
          </div>
          <div class="flex justify-between text-sm mt-1">
            <span class="text-slate-400">Trigger</span>
            <span class="font-bold">{{ trigger?.replace(/_/g, ' ') }}</span>
          </div>
          <div class="flex justify-between text-sm mt-1">
            <span class="text-slate-400">When</span>
            <span class="font-mono text-xs">{{ new Date().toLocaleString() }}</span>
          </div>
        </div>
        <div class="flex gap-2">
          <button @click="reset" :disabled="submitting"
            class="flex-1 py-2 rounded-md bg-slate-700 hover:bg-slate-600 disabled:opacity-50">
            Cancel
          </button>
          <button @click="submit" :disabled="submitting"
            class="flex-1 py-2 rounded-md bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 font-semibold">
            {{ submitting ? 'Saving…' : 'Submit' }}
          </button>
        </div>
      </div>

      <p v-if="justLogged" class="mt-3 text-xs text-emerald-400">
        ✓ Logged: {{ justLogged }}<span v-if="elapsed"> · {{ elapsed.toFixed(1) }}s</span>
      </p>
    </section>

    <!-- Recent events -->
    <section class="max-w-xl mx-auto mt-6 bg-slate-800 rounded-lg p-5 border border-slate-700">
      <h2 class="font-semibold text-base mb-3">Recent events</h2>
      <div v-if="store.events.length === 0" class="text-sm text-slate-500">No events yet.</div>
      <ul class="space-y-2 text-sm">
        <li v-for="e in store.events" :key="e.id"
          class="flex items-center justify-between border-b border-slate-700 last:border-0 pb-1">
          <div class="flex items-center gap-3">
            <span class="font-bold text-lg w-6 text-emerald-400">{{ e.intensity }}</span>
            <span>{{ (e.triggerTags || []).join(', ').replace(/_/g, ' ') }}</span>
          </div>
          <span class="font-mono text-xs text-slate-500">
            {{ new Date(e.occurredAt).toLocaleString() }}
          </span>
        </li>
      </ul>
    </section>

    <!-- C-2: PIN exit modal -->
    <div v-if="exiting" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
      <div class="bg-slate-800 border border-slate-600 rounded-lg p-6 max-w-xs w-full">
        <h3 class="font-semibold mb-2">Re-enter PIN to exit</h3>
        <input v-model="pin" type="password" placeholder="PIN"
          class="w-full px-3 py-2 rounded bg-slate-900 border border-slate-700 text-slate-100"
          @keydown.enter="confirmExit" />
        <p v-if="pinError" class="text-xs text-red-400 mt-1">{{ pinError }}</p>
        <div class="flex gap-2 mt-4">
          <button @click="exiting = false"
            class="flex-1 py-2 rounded bg-slate-700 hover:bg-slate-600 text-sm">Cancel</button>
          <button @click="confirmExit"
            class="flex-1 py-2 rounded bg-rose-600 hover:bg-rose-500 text-sm font-semibold">Exit</button>
        </div>
      </div>
    </div>
  </div>
</template>
