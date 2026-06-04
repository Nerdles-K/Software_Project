<script setup lang="ts">
import { ref, computed, onMounted, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { useDiaryStore, EMOTION_CARDS } from '../../../stores/diary'

const store = useDiaryStore()
const router = useRouter()
const picked = ref<number | null>(null)
const saving = ref(false)
const saved = ref(false)
const error = ref('')

const canvasRef = useTemplateRef<HTMLCanvasElement>('canvasRef')

// Color palette for doodle
const COLORS = ['#ef4444', '#f59e0b', '#10b981', '#3b82f6', '#a855f7', '#1f2937']
const activeColor = ref(COLORS[0])

onMounted(async () => {
  // C-7: if disabled, redirect to home (no diary icon should show in the first place).
  await store.fetchSettings()
  if (!store.settings?.diaryFeatureEnabled) {
    router.replace('/')
  }
})

let drawing = false
function ctx() { return canvasRef.value?.getContext('2d') ?? null }

function pointerPos(e: PointerEvent) {
  const c = canvasRef.value
  if (!c) return { x: 0, y: 0 }
  const r = c.getBoundingClientRect()
  return { x: (e.clientX - r.left) * (c.width / r.width), y: (e.clientY - r.top) * (c.height / r.height) }
}

function startDraw(e: PointerEvent) {
  if (picked.value == null) return
  const g = ctx(); if (!g) return
  drawing = true
  const p = pointerPos(e)
  g.beginPath(); g.moveTo(p.x, p.y)
  g.strokeStyle = activeColor.value
  g.lineWidth = 6
  g.lineCap = 'round'
  g.lineJoin = 'round'
}

function moveDraw(e: PointerEvent) {
  if (!drawing) return
  const g = ctx(); if (!g) return
  const p = pointerPos(e)
  g.lineTo(p.x, p.y); g.stroke()
}

function endDraw() { drawing = false }

function clearCanvas() {
  const c = canvasRef.value; const g = ctx()
  if (c && g) g.clearRect(0, 0, c.width, c.height)
}

const hasDoodle = computed(() => {
  const c = canvasRef.value
  if (!c) return false
  // Sample a few pixels to detect non-empty doodle. Cheaper than reading entire canvas.
  const g = c.getContext('2d'); if (!g) return false
  try {
    const data = g.getImageData(0, 0, c.width, c.height).data
    for (let i = 3; i < data.length; i += 4 * 64) {
      if (data[i] !== 0) return true
    }
  } catch { /* SecurityError if tainted; skip */ }
  return false
})

async function save() {
  if (picked.value == null) return
  saving.value = true
  error.value = ''
  try {
    const doodleUrl = hasDoodle.value ? canvasRef.value!.toDataURL('image/png') : null
    await store.createEntry(picked.value, doodleUrl)
    saved.value = true
    setTimeout(() => router.replace('/child/pecs'), 1500)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Could not save'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-amber-50 px-4 py-6 flex flex-col">
    <header class="flex items-center justify-between mb-6">
      <button @click="router.back()" aria-label="Back"
        class="w-14 h-14 rounded-2xl bg-white shadow flex items-center justify-center text-2xl">
        ←
      </button>
      <h1 class="text-2xl font-bold text-gray-800">My feelings</h1>
      <div class="w-14"></div>
    </header>

    <!-- Saved celebration -->
    <div v-if="saved"
      class="m-auto bg-green-200 rounded-3xl p-10 flex flex-col items-center gap-3">
      <span class="text-7xl">🎉</span>
      <p class="text-2xl font-bold text-green-800">Saved!</p>
    </div>

    <template v-else>
      <!-- C-6: 5 emotion cards, no text label, ≥ 100px touch targets -->
      <section class="grid grid-cols-3 sm:grid-cols-5 gap-3 mb-6">
        <button v-for="card in EMOTION_CARDS" :key="card.id"
          @click="picked = card.id"
          :aria-label="card.key"
          :class="[card.color, picked === card.id ? 'ring-4 ring-orange-500 scale-110' : '']"
          class="rounded-3xl shadow flex items-center justify-center transition-all"
          style="min-height: 120px; min-width: 120px;">
          <span class="text-6xl">{{ card.emoji }}</span>
        </button>
      </section>

      <!-- Doodle pad (optional) -->
      <section class="bg-white rounded-2xl shadow p-4 flex-1 flex flex-col">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-semibold text-gray-700">Draw if you want</span>
          <button @click="clearCanvas"
            class="text-sm text-gray-500 hover:text-gray-800">clear</button>
        </div>
        <div class="flex gap-2 mb-2">
          <button v-for="c in COLORS" :key="c"
            @click="activeColor = c"
            :aria-label="`color ${c}`"
            :style="{ backgroundColor: c }"
            :class="activeColor === c ? 'ring-4 ring-orange-500' : ''"
            class="w-10 h-10 rounded-full border border-gray-300"></button>
        </div>
        <canvas ref="canvasRef" width="600" height="320"
          class="w-full flex-1 bg-amber-50 rounded-xl border border-gray-200 touch-none"
          @pointerdown="startDraw" @pointermove="moveDraw"
          @pointerup="endDraw" @pointercancel="endDraw" @pointerleave="endDraw" />
      </section>

      <p v-if="error" class="text-red-500 text-sm mt-2 text-center">{{ error }}</p>

      <!-- Save button -->
      <button @click="save" :disabled="picked == null || saving"
        class="mt-4 mx-auto px-8 py-4 rounded-3xl bg-orange-400 text-white text-2xl font-bold shadow disabled:opacity-30"
        style="min-height: 64px; min-width: 200px;">
        {{ saving ? '…' : 'Done ✓' }}
      </button>
    </template>
  </div>
</template>
