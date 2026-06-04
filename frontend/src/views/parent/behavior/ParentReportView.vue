<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useBehaviorStore } from '../../../stores/behavior'
import ParentNav from '../../../components/ParentNav.vue'

const store = useBehaviorStore()
const shareLink = ref('')
const shareExpires = ref('')
const creatingShare = ref(false)
const shareError = ref('')

onMounted(async () => {
  await store.fetchWeeklyReport()
})

const points = computed(() => {
  const cd = store.report?.chartData ?? []
  // pad to 7 days even if backend omitted a date
  return cd.map(p => ({
    date: p.date,
    value: p.avg_emotion,
    day: new Date(p.date).toLocaleDateString(undefined, { weekday: 'short' }),
  }))
})

// SVG line-chart geometry
const W = 520
const H = 200
const PAD = { l: 36, r: 12, t: 12, b: 28 }
const maxY = 5

const linePath = computed(() => {
  const xs = points.value
  if (xs.length === 0) return ''
  const stepX = (W - PAD.l - PAD.r) / Math.max(xs.length - 1, 1)
  return xs.map((p, i) => {
    const x = PAD.l + i * stepX
    const y = PAD.t + (H - PAD.t - PAD.b) * (1 - p.value / maxY)
    return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`
  }).join(' ')
})

function chartX(i: number) {
  const xs = points.value
  const stepX = (W - PAD.l - PAD.r) / Math.max(xs.length - 1, 1)
  return PAD.l + i * stepX
}
function chartY(v: number) {
  return PAD.t + (H - PAD.t - PAD.b) * (1 - v / maxY)
}

async function makeShareLink() {
  creatingShare.value = true
  shareError.value = ''
  try {
    const res = await store.createShareLink()
    shareLink.value = `${window.location.origin}/share/reports/${res.token}`
    shareExpires.value = new Date(res.expiresAt).toLocaleString()
  } catch (e) {
    shareError.value = e instanceof Error ? e.message : 'Failed to create share link'
  } finally {
    creatingShare.value = false
  }
}

function downloadPdf() {
  // C-4: browser-driven PDF via print dialog (no extra dep). The @media print rules
  // in this file hide nav/buttons so the printed PDF only contains the report.
  window.print()
}

function copyShareLink() {
  navigator.clipboard.writeText(shareLink.value).catch(() => {})
}
</script>

<template>
  <ParentNav class="no-print" />
  <div class="min-h-screen bg-slate-900 text-slate-100 px-4 sm:px-8 py-6 report-page">
    <header class="mb-6 no-print">
      <h1 class="text-2xl font-bold">Weekly Report</h1>
    </header>

    <div v-if="!store.report" class="text-slate-400">Loading report…</div>

    <div v-else-if="store.report.status === 'insufficient'"
      class="max-w-xl mx-auto bg-amber-900/40 border border-amber-700 rounded-lg p-6 text-center">
      <p class="text-amber-300 font-semibold">{{ store.report.message }}</p>
      <p class="text-sm text-amber-200/70 mt-2">
        Week of {{ store.report.weekStartDate }} – {{ store.report.weekEndDate }}
      </p>
    </div>

    <div v-else class="max-w-3xl mx-auto space-y-6">
      <!-- Header -->
      <div class="bg-slate-800 rounded-lg p-5 border border-slate-700 print-block">
        <div class="flex items-baseline justify-between">
          <h2 class="text-lg font-semibold">
            Week of {{ store.report.weekStartDate }} – {{ store.report.weekEndDate }}
          </h2>
          <span class="text-xs text-slate-400">
            generated {{ new Date().toLocaleString() }}
          </span>
        </div>
      </div>

      <!-- Chart -->
      <div class="bg-slate-800 rounded-lg p-5 border border-slate-700 print-block">
        <h3 class="font-semibold mb-3">Emotion intensity (daily average)</h3>
        <svg :viewBox="`0 0 ${W} ${H}`" class="w-full h-auto bg-slate-900 rounded">
          <!-- Y-axis grid -->
          <g v-for="y in [1,2,3,4,5]" :key="y">
            <line :x1="PAD.l" :x2="W - PAD.r" :y1="chartY(y)" :y2="chartY(y)"
              stroke="#1e293b" stroke-width="1" />
            <text :x="PAD.l - 6" :y="chartY(y) + 4" font-size="10" fill="#64748b" text-anchor="end">
              {{ y }}
            </text>
          </g>
          <!-- X-axis labels -->
          <g v-for="(p, i) in points" :key="p.date">
            <text :x="chartX(i)" :y="H - 8" font-size="10" fill="#64748b" text-anchor="middle">
              {{ p.day }}
            </text>
          </g>
          <!-- Line -->
          <path :d="linePath" stroke="#34d399" stroke-width="2" fill="none" />
          <!-- Dots -->
          <g v-for="(p, i) in points" :key="`d-${p.date}`">
            <circle v-if="p.value > 0" :cx="chartX(i)" :cy="chartY(p.value)" r="3.5" fill="#34d399" />
          </g>
        </svg>
      </div>

      <!-- Top 3 triggers -->
      <div class="bg-slate-800 rounded-lg p-5 border border-slate-700 print-block">
        <h3 class="font-semibold mb-3">Top 3 triggers this week</h3>
        <ol class="space-y-2">
          <li v-for="(t, i) in store.report.top3Triggers" :key="t"
            class="flex items-center gap-3">
            <span class="w-6 h-6 rounded-full bg-emerald-700 text-white text-xs font-bold flex items-center justify-center">
              {{ i + 1 }}
            </span>
            <span class="capitalize">{{ t.replace(/_/g, ' ') }}</span>
          </li>
          <li v-if="store.report.top3Triggers.length === 0" class="text-sm text-slate-500">
            (no tagged triggers)
          </li>
        </ol>
      </div>

      <!-- C-4: Download + share -->
      <div class="bg-slate-800 rounded-lg p-5 border border-slate-700 no-print space-y-3">
        <h3 class="font-semibold">Export</h3>
        <div class="flex gap-2">
          <button @click="downloadPdf"
            class="px-4 py-2 rounded-md bg-emerald-600 hover:bg-emerald-500 font-semibold text-sm">
            Download PDF
          </button>
          <button @click="makeShareLink" :disabled="creatingShare"
            class="px-4 py-2 rounded-md bg-blue-600 hover:bg-blue-500 font-semibold text-sm disabled:opacity-50">
            {{ creatingShare ? 'Creating…' : 'Create share link' }}
          </button>
        </div>
        <p v-if="shareError" class="text-sm text-red-400">{{ shareError }}</p>
        <div v-if="shareLink" class="bg-slate-900 rounded-md p-3 text-xs space-y-2 border border-slate-700">
          <div class="flex items-center gap-2">
            <input :value="shareLink" readonly
              class="flex-1 bg-transparent text-slate-200 font-mono outline-none" />
            <button @click="copyShareLink"
              class="px-2 py-1 rounded bg-slate-700 hover:bg-slate-600">Copy</button>
          </div>
          <p class="text-slate-500">
            Expires {{ shareExpires }} · anyone with the link can view (read-only)
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@media print {
  .no-print { display: none !important; }
  .report-page { background: white; color: black; }
  .print-block {
    background: white !important;
    color: black !important;
    border-color: #cbd5e1 !important;
    page-break-inside: avoid;
  }
  svg { background: white !important; }
  svg text { fill: #475569 !important; }
  svg line { stroke: #e2e8f0 !important; }
}
</style>
