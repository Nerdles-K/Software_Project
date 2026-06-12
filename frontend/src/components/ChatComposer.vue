<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCardStore, CATEGORIES, cardIcon, isPhotoCard, type Card } from '../stores/cards'
import { assetUrl } from '../api/client'

const props = defineProps<{
  dark?: boolean         // parent uses dark theme variant
  placeholder?: string   // hint shown when sentence bar empty
}>()

const emit = defineEmits<{
  (e: 'send', cardIds: number[]): void
}>()

const cards = useCardStore()
const activeCategory = ref<string>('People')
const draft = ref<Card[]>([])
const sending = ref(false)
const feedbackId = ref<number | null>(null)

onMounted(async () => {
  // Need the family-wide set so every category tab has its cards, regardless
  // of which category the PECS grid was last filtered to.
  if (cards.allCards.length === 0) await cards.fetchAllCards()
})

const categoryCards = computed(() =>
  cards.allCards.filter(c => c.category === activeCategory.value))

function addCard(card: Card) {
  draft.value.push(card)
  feedbackId.value = card.id
  setTimeout(() => { feedbackId.value = null }, 200)
}

function removeAt(i: number) {
  draft.value.splice(i, 1)
}

function clearDraft() { draft.value = [] }

async function submit() {
  if (draft.value.length === 0 || sending.value) return
  sending.value = true
  try {
    emit('send', draft.value.map(c => c.id))
    draft.value = []
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <div :class="dark ? 'bg-slate-800 border-slate-700' : 'bg-white border-gray-200'"
    class="rounded-2xl border shadow-sm overflow-hidden">

    <!-- Draft sentence bar -->
    <div :class="dark ? 'bg-slate-900' : 'bg-amber-50'"
      class="p-3 min-h-[88px] flex items-center gap-2 flex-wrap border-b"
      :style="dark ? 'border-color: rgb(30 41 59)' : 'border-color: rgb(245 158 11 / 0.25)'">
      <span v-if="draft.length === 0"
        :class="dark ? 'text-slate-500' : 'text-gray-400'"
        class="text-base px-2">
        {{ placeholder || 'Tap cards below to build a sentence' }}
      </span>
      <div v-for="(c, i) in draft" :key="`${c.id}-${i}`"
        :class="[
          dark ? 'bg-slate-700' : 'bg-white border border-orange-200',
          feedbackId === c.id ? 'scale-110' : '',
        ]"
        class="relative flex items-center gap-2 px-3 py-2 rounded-xl shadow-sm transition-transform duration-200">
        <img v-if="isPhotoCard(c)" :src="assetUrl(c.imageUrl)" :alt="c.labelI18n"
          class="w-8 h-8 object-cover rounded" />
        <span v-else class="text-2xl">{{ cardIcon(c) }}</span>
        <span :class="dark ? 'text-slate-100' : 'text-gray-800'" class="text-sm font-semibold">
          {{ c.labelI18n }}
        </span>
        <button @click="removeAt(i)"
          class="ml-1 w-6 h-6 rounded-full bg-red-100 text-red-500 flex items-center justify-center text-sm font-bold hover:bg-red-200"
          aria-label="Remove">×</button>
      </div>
      <div v-if="draft.length > 0" class="ml-auto flex gap-2">
        <button @click="clearDraft"
          :class="dark ? 'bg-slate-700 text-slate-200 hover:bg-slate-600' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'"
          class="px-4 py-2 rounded-xl font-semibold text-sm">
          Clear
        </button>
        <button @click="submit" :disabled="sending"
          class="px-5 py-2 rounded-xl bg-emerald-500 text-white font-semibold text-sm hover:bg-emerald-600 disabled:opacity-50">
          {{ sending ? '…' : 'Send' }}
        </button>
      </div>
    </div>

    <!-- Category tabs -->
    <div :class="dark ? 'bg-slate-800' : 'bg-white'"
      class="flex gap-2 px-3 py-2 overflow-x-auto">
      <button v-for="cat in CATEGORIES" :key="cat.key"
        @click="activeCategory = cat.key"
        :class="activeCategory === cat.key
          ? (dark ? 'bg-emerald-600 text-white' : 'bg-orange-400 text-white shadow')
          : (dark ? 'bg-slate-700 text-slate-300' : 'bg-gray-100 text-gray-700')"
        class="px-3 py-1.5 rounded-full font-semibold text-sm flex items-center gap-1 whitespace-nowrap">
        <span class="text-base">{{ cat.emoji }}</span>{{ cat.key }}
      </button>
    </div>

    <!-- Card grid -->
    <div :class="dark ? 'bg-slate-900' : 'bg-gray-50'" class="p-3">
      <div v-if="categoryCards.length === 0"
        :class="dark ? 'text-slate-500' : 'text-gray-400'"
        class="text-center py-6 text-sm">
        No cards in this category yet.
      </div>
      <div v-else class="grid grid-cols-4 sm:grid-cols-5 gap-2">
        <button v-for="c in categoryCards" :key="c.id" @click="addCard(c)"
          :class="dark
            ? 'bg-slate-800 border border-slate-700 hover:border-emerald-500 hover:bg-slate-700'
            : 'bg-white border border-gray-200 hover:border-orange-400 hover:bg-orange-50'"
          class="rounded-xl p-2 flex flex-col items-center justify-center transition-colors min-h-[92px]">
          <img v-if="isPhotoCard(c)" :src="assetUrl(c.imageUrl)" :alt="c.labelI18n"
            class="w-12 h-12 object-cover rounded mb-1" />
          <span v-else class="text-4xl mb-1">{{ cardIcon(c) }}</span>
          <span :class="dark ? 'text-slate-300' : 'text-gray-700'" class="text-xs font-semibold truncate w-full text-center">
            {{ c.labelI18n }}
          </span>
        </button>
      </div>
    </div>
  </div>
</template>
