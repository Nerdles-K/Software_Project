<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCardStore, type Card } from '../../../stores/cards'
import { useDiaryStore } from '../../../stores/diary'
import { assetUrl } from '../../../api/client'

const store = useCardStore()
const diary = useDiaryStore()
const router = useRouter()
const activeCategory = ref('Eat')
const dragging = ref(false)
const feedbackCard = ref<Card | null>(null)
const draggedCard = ref<Card | null>(null)

const categories = [
  { key: 'Eat', emoji: '🍎' },
  { key: 'Drink', emoji: '🥛' },
  { key: 'Play', emoji: '⚽' },
  { key: 'Feel', emoji: '😊' },
]

function isPhoto(card: Card): boolean {
  return !!card.imageUrl && card.imageUrl.startsWith('/uploads/')
}

// Per-card emoji override (e.g. "emoji:🍎"); falls back to the category icon
// for legacy seed rows whose imageUrl is just "apple.png" etc.
function cardIconChar(card: Card): string {
  const u = card.imageUrl || ''
  if (u.startsWith('emoji:')) return u.slice('emoji:'.length)
  return categories.find(c => c.key === card.category)?.emoji || '📌'
}

onMounted(async () => {
  store.fetchCards(activeCategory.value)
  // C-7: read family setting to decide whether to render the diary entry icon.
  await diary.fetchSettings().catch(() => null)
})

function openDiary() {
  router.push('/child/diary')
}

function selectCategory(cat: string) {
  activeCategory.value = cat
  store.fetchCards(cat)
}

function onDragStart(card: Card) {
  dragging.value = true
  draggedCard.value = card
}

function onDragEnd() {
  dragging.value = false
  draggedCard.value = null
}

function onDropOnBar(card?: Card) {
  const target = card ?? draggedCard.value
  if (!target) return
  store.addToSentence(target)
  feedbackCard.value = target
  setTimeout(() => { feedbackCard.value = null }, 200)
  dragging.value = false
  draggedCard.value = null
}

function onRemoveFromBar(index: number) {
  store.removeFromSentence(index)
}

async function onSaveSentence() {
  await store.saveSentence()
}
</script>

<template>
  <div class="min-h-screen bg-amber-50 flex flex-col select-none">

    <!-- Sentence Bar (A-2) -->
    <div
      class="mx-4 mt-4 p-4 rounded-2xl border-2 border-dashed min-h-[80px] flex items-center gap-3 flex-wrap transition-colors"
      :class="dragging ? 'border-orange-400 bg-orange-100' : 'border-gray-300 bg-white'"
      @dragover.prevent
      @drop.prevent="onDropOnBar()"
    >
      <div v-if="store.sentenceCards.length === 0"
        class="text-gray-400 text-xl font-medium px-2">
        Drop cards here
      </div>
      <div
        v-for="(card, i) in store.sentenceCards" :key="card.id"
        class="relative flex items-center gap-2 bg-orange-100 rounded-xl px-3 py-2 shadow-sm transition-transform duration-200"
        :class="{ 'scale-110': feedbackCard?.id === card.id }"
      >
        <img v-if="isPhoto(card)" :src="assetUrl(card.imageUrl)" :alt="card.labelI18n"
          class="w-10 h-10 object-cover rounded-lg" />
        <span v-else class="text-3xl">{{ cardIconChar(card) }}</span>
        <button
          @click="onRemoveFromBar(i)"
          class="ml-1 w-8 h-8 rounded-full bg-red-100 text-red-500 flex items-center justify-center text-lg font-bold hover:bg-red-200"
          aria-label="Remove"
        >&times;</button>
      </div>
      <!-- Clear + Save buttons -->
      <div v-if="store.sentenceCards.length > 0" class="ml-auto flex gap-2">
        <button @click="store.clearSentence()"
          class="px-4 py-2 rounded-xl bg-gray-200 text-gray-700 font-semibold text-sm hover:bg-gray-300">
          Clear
        </button>
        <button @click="onSaveSentence"
          class="px-4 py-2 rounded-xl bg-green-400 text-white font-semibold text-sm hover:bg-green-500">
          Save
        </button>
      </div>
    </div>

    <!-- C-7: diary entry, icon-only, only shown when parent enabled the feature -->
    <button v-if="diary.settings?.diaryFeatureEnabled"
      @click="openDiary" aria-label="My feelings"
      class="fixed bottom-6 right-6 w-20 h-20 rounded-full bg-pink-300 shadow-xl flex items-center justify-center hover:scale-110 active:scale-95 transition-transform z-30">
      <span class="text-5xl">💗</span>
    </button>

    <!-- B-1/B-2 entry: jump to today's schedule -->
    <button @click="$router.push('/child/schedule')" aria-label="My schedule"
      class="fixed bottom-6 left-6 w-20 h-20 rounded-full bg-sky-300 shadow-xl flex items-center justify-center hover:scale-110 active:scale-95 transition-transform z-30">
      <span class="text-5xl">🗓️</span>
    </button>

    <!-- A-2 conversation entry -->
    <button @click="$router.push('/child/chat')" aria-label="Talk to family"
      class="fixed bottom-28 right-6 w-20 h-20 rounded-full bg-emerald-300 shadow-xl flex items-center justify-center hover:scale-110 active:scale-95 transition-transform z-30">
      <span class="text-5xl">💬</span>
    </button>

    <!-- Category Tabs (A-1) -->
    <div class="flex justify-center gap-3 px-4 py-4">
      <button
        v-for="cat in categories" :key="cat.key"
        @click="selectCategory(cat.key)"
        class="flex flex-col items-center gap-1 px-5 py-3 rounded-2xl font-semibold transition-all"
        :class="activeCategory === cat.key
          ? 'bg-orange-400 text-white shadow-lg scale-105'
          : 'bg-white text-gray-700 shadow'"
        style="min-width: 80px; min-height: 80px;"
      >
        <span class="text-3xl">{{ cat.emoji }}</span>
        <span class="text-sm">{{ cat.key }}</span>
      </button>
    </div>

    <!-- Card Grid (A-1: min 100x100, contrast >= 4.5:1) -->
    <div class="px-4 pb-8 flex-1">
      <div v-if="store.loading" class="text-center text-gray-400 py-12 text-xl">Loading...</div>
      <div v-else class="grid grid-cols-3 gap-3">
        <div
          v-for="card in store.cards" :key="card.id"
          draggable="true"
          @dragstart="onDragStart(card)"
          @dragend="onDragEnd"
          @click="onDropOnBar(card)"
          class="bg-white rounded-2xl shadow flex flex-col items-center justify-center p-3 cursor-pointer
            hover:shadow-md hover:scale-105 active:scale-95 transition-all border-2 border-transparent
            hover:border-orange-300"
          style="min-height: 120px; min-width: 120px;"
          :aria-label="card.labelI18n"
        >
          <img v-if="isPhoto(card)" :src="assetUrl(card.imageUrl)" :alt="card.labelI18n"
            class="w-16 h-16 object-cover rounded-xl" />
          <span v-else class="text-5xl">{{ cardIconChar(card) }}</span>
        </div>
      </div>
      <div v-if="!store.loading && store.cards.length === 0"
        class="text-center text-gray-400 py-12 text-xl">
        No cards yet. Ask your parent to add some!
      </div>
    </div>
  </div>
</template>
