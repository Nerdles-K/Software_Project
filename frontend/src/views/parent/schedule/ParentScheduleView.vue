<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useScheduleStore, type ScheduleTemplate, MAX_STEPS } from '../../../stores/schedule'
import { useCardStore, BOARD_CATEGORIES, type Card } from '../../../stores/cards'
import { assetUrl } from '../../../api/client'
import ParentNav from '../../../components/ParentNav.vue'

const sched = useScheduleStore()
const cardStore = useCardStore()

const editing = ref<ScheduleTemplate | null>(null)
const draftName = ref('')
const draftSteps = ref<number[]>([])
const showBuilder = ref(false)
const filterCategory = ref<string>('Eat')
const deleteTarget = ref<ScheduleTemplate | null>(null)
const error = ref('')

const categories = BOARD_CATEGORIES.map(c => c.key)

// One-click sample scenario: a picture guide for a supermarket trip. Creates
// the emoji step-cards (category "Shop") then a schedule template from them.
const SAMPLE_SUPERMARKET = {
  name: 'Go to the supermarket',
  steps: [
    { emoji: '👟', label: 'Put on shoes' },
    { emoji: '🚶', label: 'Walk to the shop' },
    { emoji: '🛒', label: 'Get a cart' },
    { emoji: '🍎', label: 'Get apples' },
    { emoji: '🥛', label: 'Get milk' },
    { emoji: '🍞', label: 'Get bread' },
    { emoji: '💳', label: 'Pay at the till' },
    { emoji: '🏠', label: 'Go home' },
  ],
}
const addingSample = ref(false)
const sampleMsg = ref('')

async function addSampleSupermarket() {
  sampleMsg.value = ''
  if (sched.templates.some(t => t.name === SAMPLE_SUPERMARKET.name)) {
    sampleMsg.value = 'You already have this schedule.'
    return
  }
  addingSample.value = true
  try {
    const ids: number[] = []
    for (const step of SAMPLE_SUPERMARKET.steps) {
      const card = await cardStore.createCard({
        category: 'Shop',
        labelI18n: step.label,
        imageUrl: `emoji:${step.emoji}`,
        isCustom: true,
      })
      ids.push(card.id)
    }
    await sched.createTemplate(SAMPLE_SUPERMARKET.name, ids)
    sampleMsg.value = 'Added! Your child can now run it from their 🗓️ schedule.'
  } catch (e) {
    sampleMsg.value = e instanceof Error ? e.message : 'Could not add sample'
  } finally {
    addingSample.value = false
  }
}

onMounted(async () => {
  await Promise.all([sched.fetchTemplates(), cardStore.fetchCards()])
  // Non-critical: a missing /status endpoint must not break the page.
  await sched.fetchTodayStatus().catch(() => {})
})

// Today's completion per template, keyed by template id.
const statusById = computed(() => {
  const m = new Map<number, (typeof sched.todayStatus)[number]>()
  for (const s of sched.todayStatus) m.set(s.templateId, s)
  return m
})

const cardsById = computed(() => {
  const m = new Map<number, Card>()
  for (const c of cardStore.cards) m.set(c.id, c)
  return m
})
const libraryByCategory = computed(() =>
  cardStore.cards.filter(c => c.category === filterCategory.value))

function startNew() {
  editing.value = null
  draftName.value = ''
  draftSteps.value = []
  error.value = ''
  showBuilder.value = true
}

function startEdit(t: ScheduleTemplate) {
  editing.value = t
  draftName.value = t.name
  draftSteps.value = [...t.steps]
  error.value = ''
  showBuilder.value = true
}

function addToSteps(card: Card) {
  if (draftSteps.value.length >= MAX_STEPS) {
    error.value = `Max ${MAX_STEPS} steps reached.`
    return
  }
  draftSteps.value.push(card.id)
  error.value = ''
}

function removeStep(i: number) {
  draftSteps.value.splice(i, 1)
}

function moveStep(i: number, dir: -1 | 1) {
  const j = i + dir
  if (j < 0 || j >= draftSteps.value.length) return
  const tmp = draftSteps.value[i]
  draftSteps.value[i] = draftSteps.value[j]
  draftSteps.value[j] = tmp
}

async function save() {
  if (!draftName.value.trim()) { error.value = 'Name is required'; return }
  if (draftSteps.value.length === 0) { error.value = 'Add at least one step'; return }
  try {
    if (editing.value) {
      await sched.updateTemplate(editing.value.id, draftName.value.trim(), draftSteps.value)
    } else {
      await sched.createTemplate(draftName.value.trim(), draftSteps.value)
    }
    showBuilder.value = false
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Save failed'
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  await sched.deleteTemplate(deleteTarget.value.id)
  deleteTarget.value = null
}

function iconChar(card: Card | undefined): string {
  if (!card) return '📌'
  const u = card.imageUrl || ''
  if (u.startsWith('emoji:')) return u.slice('emoji:'.length)
  const map: Record<string, string> = { Eat: '🍎', Drink: '🥛', Play: '⚽', Feel: '😊' }
  return map[card.category] || '📌'
}
function isPhoto(card: Card | undefined): boolean {
  return !!card && !!card.imageUrl && card.imageUrl.startsWith('/uploads/')
}
</script>

<template>
  <ParentNav />
  <div class="min-h-screen bg-gray-100 p-6">
    <div class="max-w-3xl mx-auto">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-3xl font-bold text-gray-900">Schedules</h2>
        <button @click="startNew"
          class="px-5 py-3 rounded-2xl bg-blue-500 text-white font-semibold text-lg hover:bg-blue-600 shadow">
          + New Schedule
        </button>
      </div>

      <!-- Builder -->
      <div v-if="showBuilder" class="bg-white rounded-2xl shadow p-5 mb-6 space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="font-bold text-lg">{{ editing ? 'Edit schedule' : 'New schedule' }}</h3>
          <button @click="showBuilder = false" class="text-sm text-gray-500 hover:text-gray-800">close</button>
        </div>

        <input v-model="draftName" type="text" placeholder="Name e.g. School day / Bedtime"
          class="w-full px-4 py-3 rounded-xl border border-gray-300 text-lg" />

        <!-- Steps in order (B-1) -->
        <div>
          <div class="flex items-baseline justify-between mb-2">
            <p class="text-sm font-semibold text-gray-700">Steps in order ({{ draftSteps.length }}/{{ MAX_STEPS }})</p>
            <p v-if="draftSteps.length >= MAX_STEPS" class="text-xs text-orange-600">Maximum reached</p>
          </div>
          <ol v-if="draftSteps.length > 0" class="space-y-2">
            <li v-for="(id, i) in draftSteps" :key="`step-${i}-${id}`"
              class="flex items-center gap-2 bg-gray-50 rounded-xl p-3 border border-gray-200">
              <span class="w-6 text-center font-bold text-gray-500">{{ i + 1 }}</span>
              <img v-if="isPhoto(cardsById.get(id))"
                :src="assetUrl(cardsById.get(id)?.imageUrl)" class="w-10 h-10 object-cover rounded" alt="" />
              <span v-else class="text-2xl">{{ iconChar(cardsById.get(id)) }}</span>
              <span class="flex-1 font-semibold text-gray-800 truncate">
                {{ cardsById.get(id)?.labelI18n || `card #${id}` }}
              </span>
              <div class="flex gap-1">
                <button @click="moveStep(i, -1)" :disabled="i === 0"
                  class="w-8 h-8 rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-30">↑</button>
                <button @click="moveStep(i, 1)" :disabled="i === draftSteps.length - 1"
                  class="w-8 h-8 rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-30">↓</button>
                <button @click="removeStep(i)"
                  class="w-8 h-8 rounded bg-red-100 text-red-500 hover:bg-red-200">×</button>
              </div>
            </li>
          </ol>
          <p v-else class="text-sm text-gray-400 italic">Tap cards below to add steps.</p>
        </div>

        <!-- Card library -->
        <div class="border-t pt-4">
          <p class="text-sm font-semibold text-gray-700 mb-2">Card library</p>
          <div class="flex gap-2 mb-3 flex-wrap">
            <button v-for="cat in categories" :key="cat" @click="filterCategory = cat"
              :class="filterCategory === cat ? 'bg-orange-400 text-white' : 'bg-white text-gray-700 border border-gray-300'"
              class="px-3 py-1.5 rounded-full text-sm font-semibold">
              {{ cat }}
            </button>
          </div>
          <div class="grid grid-cols-3 sm:grid-cols-4 gap-2">
            <button v-for="card in libraryByCategory" :key="card.id"
              @click="addToSteps(card)"
              :disabled="draftSteps.length >= MAX_STEPS"
              class="bg-white rounded-xl border border-gray-200 p-2 flex flex-col items-center gap-1 hover:border-blue-400 hover:bg-blue-50 transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
              <img v-if="isPhoto(card)" :src="assetUrl(card.imageUrl)" class="w-12 h-12 object-cover rounded" alt="" />
              <span v-else class="text-3xl">{{ iconChar(card) }}</span>
              <span class="text-xs text-gray-700 truncate w-full text-center">{{ card.labelI18n }}</span>
            </button>
          </div>
        </div>

        <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
        <div class="flex gap-2 pt-2">
          <button @click="showBuilder = false"
            class="flex-1 py-3 rounded-2xl bg-gray-200 text-gray-700 font-semibold">Cancel</button>
          <button @click="save"
            class="flex-1 py-3 rounded-2xl bg-blue-500 text-white font-semibold hover:bg-blue-600">
            {{ editing ? 'Save changes' : 'Create schedule' }}
          </button>
        </div>
      </div>

      <!-- One-click sample scenario -->
      <div v-if="!showBuilder" class="bg-white rounded-2xl shadow p-4 mb-4">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="font-semibold text-gray-800">🛒 Sample: Go to the supermarket</p>
            <p class="text-xs text-gray-500">An 8-step picture guide for a shopping trip — add it, then Edit to tweak.</p>
          </div>
          <button @click="addSampleSupermarket" :disabled="addingSample"
            class="px-4 py-2 rounded-xl bg-emerald-500 text-white font-semibold hover:bg-emerald-600 disabled:opacity-50 whitespace-nowrap">
            {{ addingSample ? 'Adding…' : 'Add' }}
          </button>
        </div>
        <p v-if="sampleMsg" class="text-sm mt-2"
          :class="sampleMsg.startsWith('Added') ? 'text-emerald-600' : 'text-gray-500'">{{ sampleMsg }}</p>
      </div>

      <!-- Template list (B-4) -->
      <div class="space-y-3">
        <div v-if="sched.templates.length === 0 && !showBuilder"
          class="bg-white rounded-2xl p-8 text-center text-gray-500">
          No schedules yet. Tap <b>+ New Schedule</b> to build one.
        </div>
        <div v-for="t in sched.templates" :key="t.id"
          class="bg-white rounded-2xl shadow p-4 flex items-center justify-between gap-3">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <h4 class="font-bold text-lg text-gray-900">{{ t.name }}</h4>
              <!-- Today's completion (B-2/B-3): green when the child finished it today -->
              <span v-if="statusById.get(t.id)?.completed"
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 text-xs font-semibold">
                ✓ Completed today
              </span>
              <span v-else-if="(statusById.get(t.id)?.completedCount ?? 0) > 0"
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 text-xs font-semibold">
                {{ statusById.get(t.id)?.completedCount }}/{{ statusById.get(t.id)?.totalSteps }} done today
              </span>
              <span v-else
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-gray-100 text-gray-500 text-xs font-semibold">
                Not started today
              </span>
            </div>
            <p class="text-xs text-gray-500 mb-2">{{ t.steps.length }} steps</p>
            <div class="flex gap-1 flex-wrap">
              <span v-for="(id, i) in t.steps" :key="`prev-${t.id}-${i}`"
                class="w-9 h-9 rounded-lg bg-gray-100 flex items-center justify-center text-lg"
                :title="cardsById.get(id)?.labelI18n">
                <img v-if="isPhoto(cardsById.get(id))"
                  :src="assetUrl(cardsById.get(id)?.imageUrl)" class="w-7 h-7 object-cover rounded" alt="" />
                <span v-else>{{ iconChar(cardsById.get(id)) }}</span>
              </span>
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <button @click="startEdit(t)"
              class="px-3 py-1.5 rounded bg-gray-100 hover:bg-gray-200 text-sm font-semibold">Edit</button>
            <button @click="deleteTarget = t"
              class="px-3 py-1.5 rounded bg-red-50 text-red-500 hover:bg-red-100 text-sm font-semibold">Delete</button>
          </div>
        </div>
      </div>

      <!-- Delete confirm -->
      <div v-if="deleteTarget" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div class="bg-white rounded-2xl p-6 max-w-sm w-full mx-4">
          <h3 class="font-bold text-xl mb-2">Delete schedule?</h3>
          <p class="text-gray-600 mb-4">
            "{{ deleteTarget.name }}" and its daily progress will be permanently removed.
          </p>
          <div class="flex gap-3">
            <button @click="deleteTarget = null"
              class="flex-1 py-3 rounded-xl bg-gray-200 font-semibold">Cancel</button>
            <button @click="confirmDelete"
              class="flex-1 py-3 rounded-xl bg-red-500 text-white font-semibold hover:bg-red-600">Delete</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
