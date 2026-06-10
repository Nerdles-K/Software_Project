<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import { useCardStore, type Card } from '../../../stores/cards'
import { assetUrl } from '../../../api/client'
import ParentNav from '../../../components/ParentNav.vue'

const CATEGORY_EMOJI: Record<string, string> = {
  Eat: '🍎', Drink: '🥛', Play: '⚽', Feel: '😊',
}

// Curated emoji palette grouped by category — parent picks one when creating a text card.
const EMOJI_PALETTE: Record<string, string[]> = {
  Eat: ['🍎', '🍞', '🍪', '🍌', '🍕', '🥪', '🥕', '🥚', '🧀', '🍰', '🍇', '🍓'],
  Drink: ['🥛', '☕', '🧃', '🥤', '🍵', '🫖'],
  Play: ['⚽', '🧩', '🎨', '🎲', '🚗', '📚', '🏀', '🧸', '🎮', '🎵'],
  Feel: ['😊', '😢', '😠', '😨', '😴', '🤗', '😌', '🥰', '😟', '🤩'],
}

// What the card actually renders. Photo cards use `/uploads/...`; emoji cards use `emoji:🍎`.
function cardIconChar(card: Pick<Card, 'imageUrl' | 'category'>): string {
  const u = card.imageUrl || ''
  if (u.startsWith('emoji:')) return u.slice('emoji:'.length)
  return CATEGORY_EMOJI[card.category] ?? '📌'
}
function isPhoto(card: Pick<Card, 'imageUrl'>): boolean {
  return !!card.imageUrl && card.imageUrl.startsWith('/uploads/')
}

const store = useCardStore()
const showAddForm = ref(false)
const deleteTarget = ref<Card | null>(null)

const uploading = ref(false)
const uploadError = ref('')

// Category that the upload button targets (also reused by manual add form).
const uploadCategory = ref<string>('Eat')

const newCard = ref({ category: 'Eat', labelI18n: '', emoji: '🍎' })
const labelInputRef = ref<HTMLInputElement | null>(null)
const justCreatedFlash = ref('')

const editingId = ref<number | null>(null)
const editingLabel = ref('')
const editInputRef = ref<HTMLInputElement | null>(null)

const categories = ['Eat', 'Drink', 'Play', 'Feel']

// Writable per-category lists for VueDraggable v-model
const grouped = reactive<Record<string, Card[]>>({
  Eat: [], Drink: [], Play: [], Feel: [],
})

function rebuildGrouped() {
  for (const c of categories) grouped[c] = []
  for (const card of store.cards) {
    if (!grouped[card.category]) grouped[card.category] = []
    grouped[card.category].push(card)
  }
}

watch(() => store.cards, rebuildGrouped, { immediate: true, deep: false })

onMounted(() => store.fetchCards())

// A-5: file picked → validate → upload → auto-create card in `uploadCategory`.
// New card shows up in the matching category list immediately (AC #2).
async function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploadError.value = ''

  if (file.size > 5 * 1024 * 1024) {
    uploadError.value = 'File too large. Max size is 5MB.'
    input.value = ''
    return
  }
  if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type.toLowerCase())) {
    uploadError.value = 'Only JPG or PNG files are allowed.'
    input.value = ''
    return
  }

  uploading.value = true
  try {
    await store.uploadAndCreateCard(file, uploadCategory.value)
  } catch (err) {
    uploadError.value = err instanceof Error ? err.message : 'Upload failed. Please try again.'
  } finally {
    uploading.value = false
    input.value = ''
  }
}

// A-4: manual text-only card creation (still useful for non-photo entries).
// Stays open after each Create so the parent can chain-add cards without
// reopening the form (user feedback 6/4).
async function addCard() {
  const label = newCard.value.labelI18n.trim()
  if (!label) return
  const cat = newCard.value.category
  const emoji = newCard.value.emoji
  await store.createCard({
    category: cat,
    labelI18n: label,
    imageUrl: `emoji:${emoji}`,
    isCustom: false,
  })
  justCreatedFlash.value = `${emoji} ${label}`
  setTimeout(() => { justCreatedFlash.value = '' }, 1200)
  // Keep category + emoji, clear label, re-focus for the next entry.
  newCard.value.labelI18n = ''
  await nextTick()
  labelInputRef.value?.focus()
}

function onCategoryChanged(newCat: string) {
  // When parent changes category, default to that category's first emoji
  // (unless they already picked one in this session).
  const palette = EMOJI_PALETTE[newCat] || []
  if (palette.length && !palette.includes(newCard.value.emoji)) {
    newCard.value.emoji = palette[0]
  }
}

function confirmDelete(card: Card) {
  deleteTarget.value = card
}

async function executeDelete() {
  if (deleteTarget.value) {
    await store.deleteCard(deleteTarget.value.id)
  }
  deleteTarget.value = null
}

async function persistOrder() {
  const flat: Card[] = []
  for (const c of categories) flat.push(...(grouped[c] || []))
  await store.reorderCards(flat)
}

async function startEdit(card: Card) {
  editingId.value = card.id
  editingLabel.value = card.labelI18n
  await nextTick()
  editInputRef.value?.focus()
  editInputRef.value?.select()
}

async function saveEdit() {
  if (editingId.value == null) return
  const label = editingLabel.value.trim()
  if (label) {
    await store.renameCard(editingId.value, label)
  }
  editingId.value = null
}

function cancelEdit() {
  editingId.value = null
}
</script>

<template>
  <ParentNav />
  <div class="min-h-screen bg-gray-100 p-6">
    <div class="max-w-2xl mx-auto">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-3xl font-bold text-gray-900">Manage Cards</h2>
        <button @click="showAddForm = !showAddForm"
          class="px-5 py-3 rounded-2xl bg-blue-500 text-white font-semibold text-lg hover:bg-blue-600 transition-colors shadow">
          {{ showAddForm ? 'Hide form' : '+ Add Card' }}
        </button>
      </div>

      <!-- A-5: photo upload zone — selecting a file immediately creates a card. -->
      <div class="bg-white rounded-2xl p-5 shadow mb-6 space-y-3">
        <h3 class="font-bold text-lg text-gray-800">Upload custom photo card</h3>
        <p class="text-sm text-gray-500">
          Pick a JPG/PNG (max 5MB). The card appears in the selected category right after upload —
          rename it later with the pencil button.
        </p>
        <div class="flex items-center gap-3">
          <select v-model="uploadCategory"
            class="px-4 py-3 rounded-xl border border-gray-300 text-lg">
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <!-- Custom English button: hides the native file input whose label
               text is localized by the browser (e.g. shows "选择文件" on zh). -->
          <label class="flex-1">
            <input type="file" accept="image/jpeg,image/png"
              @change="handleFileSelect"
              :disabled="uploading"
              class="hidden" />
            <span
              class="inline-block px-4 py-2 rounded-xl bg-blue-50 text-blue-600 text-sm font-semibold cursor-pointer hover:bg-blue-100 select-none"
              :class="uploading ? 'opacity-50 pointer-events-none' : ''">
              Choose image
            </span>
          </label>
        </div>
        <p v-if="uploading" class="text-sm text-blue-500">Uploading...</p>
        <p v-if="uploadError" class="text-sm text-red-500">{{ uploadError }}</p>
      </div>

      <!-- A-4: manual (text-only) add form — emoji picker + stays open for chain-adds. -->
      <div v-if="showAddForm" class="bg-white rounded-2xl p-5 shadow mb-6 space-y-3">
        <h3 class="font-bold text-lg text-gray-800">New text card</h3>

        <div class="grid grid-cols-2 gap-3">
          <select v-model="newCard.category" @change="onCategoryChanged(newCard.category)"
            class="px-4 py-3 rounded-xl border border-gray-300 text-lg">
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <input v-model="newCard.labelI18n" ref="labelInputRef"
            @keydown.enter="addCard"
            type="text" placeholder="Card label"
            class="px-4 py-3 rounded-xl border border-gray-300 text-lg" />
        </div>

        <!-- Emoji picker -->
        <div>
          <p class="text-sm font-semibold text-gray-700 mb-2">Pick an icon</p>
          <div class="flex flex-wrap gap-2">
            <button v-for="e in EMOJI_PALETTE[newCard.category] || []" :key="e"
              type="button" @click="newCard.emoji = e"
              :class="newCard.emoji === e ? 'border-orange-500 bg-orange-50 ring-2 ring-orange-300' : 'border-gray-200 bg-white hover:bg-gray-50'"
              class="w-12 h-12 rounded-xl border-2 text-2xl flex items-center justify-center">
              {{ e }}
            </button>
          </div>
        </div>

        <div class="flex items-center gap-3 pt-1">
          <div class="flex-1 text-sm text-gray-500">
            Preview:
            <span class="text-2xl ml-2">{{ newCard.emoji }}</span>
            <span class="ml-1 font-semibold text-gray-800">
              {{ newCard.labelI18n || '(label)' }}
            </span>
          </div>
          <button @click="addCard" :disabled="!newCard.labelI18n.trim()"
            class="px-5 py-3 rounded-2xl bg-blue-500 text-white font-semibold hover:bg-blue-600 disabled:opacity-50">
            Create Card
          </button>
        </div>

        <p v-if="justCreatedFlash" class="text-sm text-emerald-600 font-medium">
          ✓ Added {{ justCreatedFlash }} — form stays open for the next one.
        </p>
      </div>

      <!-- Delete confirmation dialog (A-4 AC: 二次确认弹窗) -->
      <div v-if="deleteTarget"
        class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div class="bg-white rounded-2xl p-6 shadow-xl max-w-sm w-full mx-4">
          <h3 class="text-xl font-bold text-gray-900 mb-2">Delete Card?</h3>
          <p class="text-gray-600 mb-4">
            "{{ deleteTarget.labelI18n }}" will be permanently removed.
          </p>
          <div class="flex gap-3">
            <button @click="deleteTarget = null"
              class="flex-1 px-4 py-3 rounded-xl bg-gray-200 text-gray-700 font-semibold">
              Cancel
            </button>
            <button @click="executeDelete"
              class="flex-1 px-4 py-3 rounded-xl bg-red-500 text-white font-semibold hover:bg-red-600">
              Delete
            </button>
          </div>
        </div>
      </div>

      <!-- Cards by category with drag-reorder (A-4) -->
      <div v-for="cat in categories" :key="cat" class="mb-6">
        <h3 class="text-lg font-bold text-gray-700 mb-2">{{ cat }}</h3>
        <div v-if="(grouped[cat] || []).length === 0"
          class="text-gray-400 text-sm py-2">No cards</div>
        <VueDraggable
          v-model="grouped[cat]"
          :animation="200"
          ghost-class="opacity-40"
          handle=".drag-handle"
          class="space-y-2"
          @end="persistOrder"
        >
          <div v-for="card in grouped[cat]" :key="card.id"
            class="bg-white rounded-xl p-4 shadow-sm flex items-center justify-between">
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <span class="drag-handle cursor-grab text-gray-400 text-xl select-none">≡</span>
              <img v-if="isPhoto(card)"
                :src="assetUrl(card.imageUrl)" :alt="card.labelI18n"
                class="w-10 h-10 object-cover rounded-lg" />
              <span v-else class="text-2xl">{{ cardIconChar(card) }}</span>
              <input v-if="editingId === card.id"
                ref="editInputRef"
                v-model="editingLabel"
                @keydown.enter="saveEdit"
                @keydown.esc="cancelEdit"
                @blur="saveEdit"
                class="flex-1 min-w-0 px-2 py-1 rounded border border-blue-300 font-semibold text-gray-800" />
              <span v-else class="font-semibold text-gray-800 truncate">{{ card.labelI18n }}</span>
              <span v-if="card.isCustom"
                class="text-xs bg-blue-100 text-blue-600 px-2 py-1 rounded-full">
                custom
              </span>
            </div>
            <div class="flex items-center gap-2 ml-3">
              <button v-if="editingId !== card.id" @click="startEdit(card)"
                class="w-10 h-10 rounded-full bg-gray-100 text-gray-600 flex items-center justify-center hover:bg-gray-200"
                :aria-label="`Rename ${card.labelI18n}`">
                ✏️
              </button>
              <button @click="confirmDelete(card)"
                class="w-10 h-10 rounded-full bg-red-50 text-red-500 flex items-center justify-center font-bold hover:bg-red-100">
                &times;
              </button>
            </div>
          </div>
        </VueDraggable>
      </div>
    </div>
  </div>
</template>
