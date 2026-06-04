<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const BASE = 'http://localhost:8080'

interface WeeklyReport {
  status: string
  weekStartDate: string
  weekEndDate: string
  chartData: Array<{ date: string; avg_emotion: number }>
  top3Triggers: string[]
}

const route = useRoute()
const token = (route.params.token as string) ?? ''
const report = ref<WeeklyReport | null>(null)
const error = ref('')

onMounted(async () => {
  try {
    const res = await fetch(`${BASE}/share/reports/${token}`)
    if (res.status === 410) {
      error.value = 'This share link has expired.'
      return
    }
    if (res.status === 404) {
      error.value = 'Share link not found.'
      return
    }
    if (!res.ok) {
      error.value = `Could not load (HTTP ${res.status})`
      return
    }
    report.value = await res.json()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Network error'
  }
})

const points = computed(() => report.value?.chartData ?? [])
const W = 520, H = 200
const PAD = { l: 36, r: 12, t: 12, b: 28 }
function chartX(i: number) {
  const stepX = (W - PAD.l - PAD.r) / Math.max(points.value.length - 1, 1)
  return PAD.l + i * stepX
}
function chartY(v: number) {
  return PAD.t + (H - PAD.t - PAD.b) * (1 - v / 5)
}
const linePath = computed(() => {
  const xs = points.value
  return xs.map((p, i) => `${i === 0 ? 'M' : 'L'} ${chartX(i).toFixed(1)} ${chartY(p.avg_emotion).toFixed(1)}`).join(' ')
})
</script>

<template>
  <div class="min-h-screen bg-white text-slate-900 p-6">
    <header class="max-w-3xl mx-auto border-b border-slate-200 pb-4 mb-6">
      <h1 class="text-2xl font-bold">VisiTalk — Shared Weekly Report</h1>
      <p class="text-xs text-slate-500 mt-1">Read-only view · no login required</p>
    </header>

    <div v-if="error" class="max-w-3xl mx-auto bg-red-50 border border-red-200 rounded p-6 text-red-800">
      {{ error }}
    </div>

    <div v-else-if="!report" class="max-w-3xl mx-auto text-slate-500">Loading…</div>

    <div v-else class="max-w-3xl mx-auto space-y-6">
      <div class="border border-slate-200 rounded p-5">
        <h2 class="font-semibold">Week of {{ report.weekStartDate }} – {{ report.weekEndDate }}</h2>
      </div>

      <div class="border border-slate-200 rounded p-5">
        <h3 class="font-semibold mb-3">Emotion intensity (daily average)</h3>
        <svg :viewBox="`0 0 ${W} ${H}`" class="w-full bg-slate-50 rounded">
          <g v-for="y in [1,2,3,4,5]" :key="y">
            <line :x1="PAD.l" :x2="W - PAD.r" :y1="chartY(y)" :y2="chartY(y)" stroke="#e2e8f0" />
            <text :x="PAD.l - 6" :y="chartY(y) + 4" font-size="10" fill="#64748b" text-anchor="end">{{ y }}</text>
          </g>
          <g v-for="(p, i) in points" :key="p.date">
            <text :x="chartX(i)" :y="H - 8" font-size="10" fill="#64748b" text-anchor="middle">
              {{ new Date(p.date).toLocaleDateString(undefined, { weekday: 'short' }) }}
            </text>
          </g>
          <path :d="linePath" stroke="#059669" stroke-width="2" fill="none" />
          <g v-for="(p, i) in points" :key="`d-${p.date}`">
            <circle v-if="p.avg_emotion > 0" :cx="chartX(i)" :cy="chartY(p.avg_emotion)" r="3.5" fill="#059669" />
          </g>
        </svg>
      </div>

      <div class="border border-slate-200 rounded p-5">
        <h3 class="font-semibold mb-3">Top 3 triggers</h3>
        <ol class="list-decimal pl-5 space-y-1">
          <li v-for="t in report.top3Triggers" :key="t" class="capitalize">{{ t.replace(/_/g, ' ') }}</li>
          <li v-if="report.top3Triggers.length === 0" class="list-none text-slate-500">(no tagged triggers)</li>
        </ol>
      </div>
    </div>
  </div>
</template>
