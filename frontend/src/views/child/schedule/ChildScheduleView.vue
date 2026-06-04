<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useScheduleStore } from '../../../stores/schedule'
import type { Card } from '../../../stores/cards'
import { assetUrl } from '../../../api/client'

const sched = useScheduleStore()
const router = useRouter()

const selectedTemplateId = ref<number | null>(null)
const loading = ref(true)
const noTemplate = ref(false)

onMounted(async () => {
  await sched.fetchTemplates()
  if (sched.templates.length === 0) {
    noTemplate.value = true
    loading.value = false
    return
  }
  // Default to the most recently created.
  selectedTemplateId.value = sched.templates[0].id
  await sched.fetchToday(selectedTemplateId.value)
  loading.value = false
})

async function switchTemplate(id: number) {
  selectedTemplateId.value = id
  loading.value = true
  await sched.fetchToday(id)
  loading.value = false
}

const view = computed(() => sched.todayView)
const completedSet = computed(() => new Set(view.value?.instance.completedStepIndices ?? []))

// B-2: "current step" = first step not yet completed.
const currentIndex = computed(() => {
  const n = view.value?.template.steps.length ?? 0
  for (let i = 0; i < n; i++) {
    if (!completedSet.value.has(i)) return i
  }
  return n // all done
})

const allDone = computed(() => {
  const n = view.value?.template.steps.length ?? 0
  return n > 0 && currentIndex.value >= n
})

function stateOf(index: number): 'done' | 'current' | 'next' | 'rest' {
  if (completedSet.value.has(index)) return 'done'
  if (index === currentIndex.value) return 'current'
  if (index === currentIndex.value + 1) return 'next'
  return 'rest'
}

function isPhoto(card: Card): boolean {
  return !!card.imageUrl && card.imageUrl.startsWith('/uploads/')
}
function iconChar(card: Card): string {
  const u = card.imageUrl || ''
  if (u.startsWith('emoji:')) return u.slice('emoji:'.length)
  const map: Record<string, string> = { Eat: '🍎', Drink: '🥛', Play: '⚽', Feel: '😊' }
  return map[card.category] || '📌'
}

const toggling = ref<number | null>(null)
async function tick(index: number) {
  if (!view.value) return
  toggling.value = index
  try {
    await sched.toggleStep(view.value.instance.id, index, !completedSet.value.has(index))
  } finally {
    toggling.value = null
  }
}

function restart() {
  // B-3: "All steps complete → celebration." User taps OK → reset by un-ticking all.
  if (!view.value) return
  const ids = [...completedSet.value]
  for (const i of ids) sched.toggleStep(view.value.instance.id, i, false)
}
</script>

<template>
  <div class="min-h-screen bg-amber-50 flex flex-col select-none">
    <header class="flex items-center justify-between px-4 py-4">
      <button @click="router.back()" aria-label="Back"
        class="w-14 h-14 rounded-2xl bg-white shadow flex items-center justify-center text-2xl">
        ←
      </button>
      <h1 class="text-2xl font-bold text-gray-800">{{ view?.template.name || 'Schedule' }}</h1>
      <div class="w-14"></div>
    </header>

    <!-- Template picker if multiple -->
    <div v-if="sched.templates.length > 1 && !allDone"
      class="px-4 pb-2 flex gap-2 overflow-x-auto">
      <button v-for="t in sched.templates" :key="t.id"
        @click="switchTemplate(t.id)"
        :class="t.id === selectedTemplateId ? 'bg-orange-400 text-white' : 'bg-white text-gray-700'"
        class="px-4 py-2 rounded-full font-semibold whitespace-nowrap shadow-sm">
        {{ t.name }}
      </button>
    </div>

    <div v-if="loading" class="m-auto text-xl text-gray-500">Loading…</div>

    <div v-else-if="noTemplate" class="m-auto text-center px-6">
      <p class="text-6xl mb-3">🗓️</p>
      <p class="text-xl text-gray-600">Ask your grown-up to make a schedule for today.</p>
    </div>

    <!-- B-3 celebration -->
    <div v-else-if="allDone" class="m-auto text-center px-6">
      <p class="text-[8rem] leading-none animate-bounce">🎉</p>
      <p class="text-3xl font-bold text-emerald-700 mt-2">All done!</p>
      <p class="text-lg text-gray-700 mt-2">Great job today.</p>
      <button @click="restart"
        class="mt-6 px-6 py-3 rounded-3xl bg-orange-400 text-white font-bold text-xl shadow"
        style="min-height: 56px; min-width: 180px;">
        Start over
      </button>
    </div>

    <!-- B-2 steps display -->
    <div v-else-if="view" class="flex-1 px-4 pb-8">
      <ol class="space-y-3">
        <li v-for="(card, i) in view.cards" :key="`step-${i}-${card.id}`"
          :class="{
            'opacity-100 scale-100 ring-4 ring-orange-500 bg-white': stateOf(i) === 'current',
            'opacity-80 ring-2 ring-orange-200 bg-white': stateOf(i) === 'next',
            'opacity-40 bg-white': stateOf(i) === 'rest',
            'opacity-60 bg-emerald-50': stateOf(i) === 'done',
          }"
          class="flex items-center gap-3 rounded-3xl p-3 shadow-sm transition-all">
          <span class="w-9 h-9 rounded-full flex items-center justify-center font-bold text-white"
            :class="stateOf(i) === 'done' ? 'bg-emerald-500' : 'bg-gray-400'">
            {{ i + 1 }}
          </span>
          <img v-if="isPhoto(card)" :src="assetUrl(card.imageUrl)" alt=""
            class="w-20 h-20 object-cover rounded-2xl" />
          <span v-else class="text-6xl">{{ iconChar(card) }}</span>

          <!-- B-3 big checkbox -->
          <button @click="tick(i)" :disabled="toggling === i"
            :aria-label="completedSet.has(i) ? 'Mark not done' : 'Mark done'"
            :class="completedSet.has(i)
              ? 'bg-emerald-500 text-white border-emerald-600'
              : stateOf(i) === 'current'
                ? 'bg-white border-orange-500'
                : 'bg-white border-gray-300'"
            class="ml-auto w-20 h-20 rounded-2xl border-4 flex items-center justify-center text-5xl transition-transform active:scale-90"
            style="min-width: 80px; min-height: 80px;">
            <span v-if="completedSet.has(i)" class="animate-[pop_0.3s_ease-out]">✓</span>
          </button>
        </li>
      </ol>
    </div>
  </div>
</template>

<style scoped>
@keyframes pop {
  0% { transform: scale(0.3); opacity: 0; }
  60% { transform: scale(1.3); opacity: 1; }
  100% { transform: scale(1); opacity: 1; }
}
</style>
