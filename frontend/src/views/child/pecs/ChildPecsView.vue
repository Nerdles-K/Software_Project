<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import { useI18n } from 'vue-i18n'
import ChildSentenceBar from '../../../components/pecs/ChildSentenceBar.vue'
import PictogramCardFace from '../../../components/pecs/PictogramCardFace.vue'
import {
  cloneToSentence,
  fetchCardsByCategory,
  type PecsCategory,
  type PictogramCardDto,
  type SentenceCard,
} from '../../../api/pecs'

const { t } = useI18n()

const selectedCategory = ref<PecsCategory>('Eat')
const cards = ref<PictogramCardDto[]>([])
const sentenceCards = ref<SentenceCard[]>([])
const starterCards = ref<PictogramCardDto[]>([])
const loading = ref(false)
const loadError = ref('')
const pulseInstanceId = ref<string | null>(null)

const activeStarterId = computed(() => sentenceCards.value[0]?.card.id ?? null)

const categories = computed(() => [
  { id: 'Eat' as const, label: t('child.pecs.category_eat'), bg: 'bg-orange-100', ring: 'ring-orange-300' },
  { id: 'Drink' as const, label: t('child.pecs.category_drink'), bg: 'bg-sky-100', ring: 'ring-sky-300' },
  { id: 'Play' as const, label: t('child.pecs.category_play'), bg: 'bg-emerald-100', ring: 'ring-emerald-300' },
  { id: 'Feel' as const, label: t('child.pecs.category_feel'), bg: 'bg-violet-100', ring: 'ring-violet-300' },
])

const poolDragGroup = { name: 'pecs', pull: 'clone' as const, put: false }

const STARTER_LABELS = new Set(['Want', 'Need', 'Like', 'Stop', 'Give'])

async function loadCards() {
  loading.value = true
  loadError.value = ''
  try {
    cards.value = await fetchCardsByCategory(selectedCategory.value)
  } catch (e: unknown) {
    cards.value = []
    loadError.value = e instanceof Error ? e.message : 'Failed to load cards'
  } finally {
    loading.value = false
  }
}

async function loadStarterCards() {
  try {
    const action = await fetchCardsByCategory('Action')
    starterCards.value = action.filter((c) => STARTER_LABELS.has(c.labelI18n ?? ''))
    if (sentenceCards.value.length === 0) {
      const defaultCard =
        starterCards.value.find((c) => c.labelI18n === 'Want') ?? starterCards.value[0]
      if (defaultCard) setStarterInSentence(defaultCard)
    }
  } catch {
    starterCards.value = []
  }
}

function chooseStarter(card: PictogramCardDto) {
  setStarterInSentence(card)
  pulseInstanceId.value = `starter-${card.id}`
  window.setTimeout(() => { pulseInstanceId.value = null }, 200)
}

function setStarterInSentence(card: PictogramCardDto) {
  const entry: SentenceCard = { instanceId: `starter-${card.id}`, card }
  if (sentenceCards.value.length === 0) {
    sentenceCards.value = [entry]
  } else {
    sentenceCards.value[0] = entry
  }
}

function cloneCard(card: PictogramCardDto): SentenceCard {
  return cloneToSentence(card)
}

function onSentenceCardAdded() {
  const last = sentenceCards.value[sentenceCards.value.length - 1]
  if (!last) return
  pulseInstanceId.value = last.instanceId
  window.setTimeout(() => { pulseInstanceId.value = null }, 200)
}

function selectCategory(cat: PecsCategory) {
  selectedCategory.value = cat
}

watch(selectedCategory, () => { loadCards() }, { immediate: true })

onMounted(() => { loadStarterCards() })
</script>

<template>
  <main class="min-h-screen bg-amber-50 p-6">
    <h2 class="sr-only">{{ t('app.title') }} - PECS</h2>
    <p v-if="loadError" class="sr-only" role="alert">{{ loadError }}</p>

    <section class="mx-auto max-w-3xl">
      <ChildSentenceBar
        v-model="sentenceCards"
        :pulse-instance-id="pulseInstanceId"
        @card-added="onSentenceCardAdded"
      />

      <div
        v-if="starterCards.length > 0"
        class="mb-6 flex gap-3 overflow-x-auto pb-1"
        role="group"
        :aria-label="t('child.pecs.starter_phrase')"
      >
        <button
          v-for="starter in starterCards"
          :key="starter.id"
          type="button"
          class="flex min-h-[100px] min-w-[100px] shrink-0 items-center justify-center rounded-2xl bg-white shadow-sm ring-4 transition duration-200 active:scale-[0.98] focus:outline-none focus-visible:ring-amber-400"
          :class="activeStarterId === starter.id ? 'ring-amber-400' : 'ring-black/10'"
          :aria-label="starter.labelI18n ?? undefined"
          :aria-pressed="activeStarterId === starter.id"
          @click="chooseStarter(starter)"
        >
          <PictogramCardFace :card="starter" />
        </button>
      </div>

      <div class="grid grid-cols-2 gap-5">
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          class="relative flex min-h-[150px] w-full items-center justify-center rounded-3xl shadow-sm ring-4 transition active:scale-[0.99] focus:outline-none focus-visible:ring-8"
          :class="[cat.bg, selectedCategory === cat.id ? cat.ring : 'ring-transparent']"
          :aria-label="cat.label"
          :aria-pressed="selectedCategory === cat.id"
          @click="selectCategory(cat.id)"
        >
          <span class="pointer-events-none flex flex-col items-center gap-4">
            <span class="flex h-16 w-16 items-center justify-center rounded-2xl bg-white/70 ring-2 ring-black/10">
              <span v-if="cat.id === 'Eat'" class="text-4xl" aria-hidden="true">🍎</span>
              <span v-else-if="cat.id === 'Drink'" class="text-4xl" aria-hidden="true">🥤</span>
              <span v-else-if="cat.id === 'Play'" class="text-4xl" aria-hidden="true">🧸</span>
              <span v-else class="text-4xl" aria-hidden="true">😊</span>
            </span>
            <span class="sr-only">{{ cat.label }}</span>
          </span>
        </button>
      </div>

      <VueDraggable
        v-model="cards"
        :group="poolDragGroup"
        item-key="id"
        :sort="false"
        :clone="cloneCard"
        :delay="150"
        :delay-on-touch-only="true"
        class="mt-8 grid grid-cols-3 gap-4 sm:grid-cols-4"
        :aria-busy="loading"
      >
        <div
          v-for="card in cards"
          :key="card.id"
          class="flex min-h-[120px] min-w-[100px] cursor-grab items-center justify-center rounded-2xl bg-white shadow-sm ring-2 ring-black/10 transition active:scale-[0.98] active:cursor-grabbing focus:outline-none focus-visible:ring-4 focus-visible:ring-amber-400"
          tabindex="0"
          role="button"
          :aria-label="card.labelI18n ?? undefined"
        >
          <PictogramCardFace :card="card" />
        </div>
      </VueDraggable>

      <p v-if="loading" class="sr-only">Loading cards</p>
    </section>
  </main>
</template>
