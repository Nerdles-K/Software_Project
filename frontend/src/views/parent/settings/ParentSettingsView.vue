<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useDiaryStore } from '../../../stores/diary'
import { useBehaviorStore } from '../../../stores/behavior'
import ParentNav from '../../../components/ParentNav.vue'

const diary = useDiaryStore()
const behavior = useBehaviorStore()
const saving = ref(false)
const error = ref('')

onMounted(async () => {
  await Promise.all([
    diary.fetchSettings(),
    diary.fetchTodayStatus(),
    behavior.fetchAlerts(),
  ])
})

async function toggleDiary() {
  saving.value = true
  error.value = ''
  try {
    await diary.setDiaryEnabled(!diary.settings?.diaryFeatureEnabled)
    await diary.fetchTodayStatus()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to update'
  } finally {
    saving.value = false
  }
}

async function dismiss(tag: string) {
  await behavior.dismissAlert(tag)
}
</script>

<template>
  <ParentNav />
  <div class="min-h-screen bg-slate-900 text-slate-100 px-4 sm:px-8 py-6">
    <header class="mb-6">
      <h1 class="text-2xl font-bold">Settings</h1>
    </header>

    <!-- C-5: trigger alerts banner -->
    <section v-if="behavior.alerts.length > 0"
      class="max-w-xl mx-auto mb-6 bg-rose-950/50 border border-rose-700 rounded-lg p-4">
      <h2 class="font-semibold text-rose-300 mb-2">
        ⚠ {{ behavior.alerts.length }} trigger{{ behavior.alerts.length > 1 ? 's' : '' }}
        recurring 3 days in a row
      </h2>
      <p class="text-xs text-rose-200/70 mb-3">
        Consider early intervention. Dismiss to stop seeing this until a fresh streak starts.
      </p>
      <ul class="space-y-2">
        <li v-for="t in behavior.alerts" :key="t"
          class="flex items-center justify-between bg-slate-900/50 rounded px-3 py-2">
          <span class="capitalize">{{ t.replace(/_/g, ' ') }}</span>
          <button @click="dismiss(t)"
            class="text-xs px-2 py-1 rounded bg-slate-700 hover:bg-slate-600">Dismiss</button>
        </li>
      </ul>
    </section>

    <!-- C-7: diary toggle -->
    <section class="max-w-xl mx-auto bg-slate-800 rounded-lg p-5 border border-slate-700">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h2 class="font-semibold">Child's private diary</h2>
          <p class="text-sm text-slate-400 mt-1">
            When on, a diary icon appears on the child's main screen. You will only see whether
            your child wrote one today — never the content.
          </p>
        </div>
        <button @click="toggleDiary" :disabled="saving"
          :aria-pressed="diary.settings?.diaryFeatureEnabled"
          :class="diary.settings?.diaryFeatureEnabled ? 'bg-emerald-500' : 'bg-slate-600'"
          class="relative w-14 h-7 rounded-full transition-colors disabled:opacity-50 flex-shrink-0">
          <span
            :class="diary.settings?.diaryFeatureEnabled ? 'translate-x-7' : 'translate-x-0.5'"
            class="absolute top-0.5 w-6 h-6 bg-white rounded-full transition-transform"></span>
        </button>
      </div>
      <p v-if="error" class="text-sm text-red-400 mt-2">{{ error }}</p>

      <!-- Today indicator: bool only, NO content -->
      <div v-if="diary.todayWritten?.enabled"
        class="mt-4 bg-slate-900 rounded-md p-3 border border-slate-700 text-sm">
        <span v-if="diary.todayWritten.writtenToday" class="text-emerald-400">
          ✓ Your child wrote a diary entry today
        </span>
        <span v-else class="text-slate-400">
          Your child hasn't written today (entries are private — only this status is shown)
        </span>
      </div>
    </section>
  </div>
</template>
