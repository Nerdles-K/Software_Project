import { ref } from 'vue'
import { defineStore } from 'pinia'
import { api, uploadFile } from '../api/client'
import { useAuthStore } from './auth'

/**
 * Category list shared by parent/child views. Order = tab order. People/Action/Time
 * power the conversation experience (subjects + verbs + adverbs); Eat/Drink/Play/Feel
 * are the original PECS categories.
 */
export const CATEGORIES = [
  // Conversation grammar (chat composer): subjects + verbs + adverbs.
  { key: 'People', emoji: '👨‍👩‍👧' },
  { key: 'Action', emoji: '🤲' },
  { key: 'Time',   emoji: '⏰' },
  // Daily-life PECS board: requests, self-care routines, places, and the
  // concrete object categories.
  { key: 'Need',   emoji: '🙏' },
  { key: 'Daily',  emoji: '🪥' },
  { key: 'Place',  emoji: '🏠' },
  { key: 'Eat',    emoji: '🍎' },
  { key: 'Drink',  emoji: '🥛' },
  { key: 'Play',   emoji: '⚽' },
  { key: 'Feel',   emoji: '😊' },
] as const

export type CategoryKey = (typeof CATEGORIES)[number]['key']

/**
 * The subset shown on the PECS board (child home, parent card management,
 * schedule builder). Excludes People/Action/Time — those are sentence-grammar
 * pieces used only in the chat composer.
 */
export const BOARD_CATEGORIES = CATEGORIES.filter(
  c => !['People', 'Action', 'Time'].includes(c.key)
)

const CATEGORY_EMOJI: Record<string, string> = Object.fromEntries(
  CATEGORIES.map(c => [c.key, c.emoji])
)

export function cardIcon(card: { imageUrl?: string | null; category: string }): string {
  const u = card.imageUrl || ''
  if (u.startsWith('emoji:')) return u.slice('emoji:'.length)
  if (u.startsWith('text:')) return u.slice('text:'.length)
  return CATEGORY_EMOJI[card.category] || '📌'
}
export function isPhotoCard(card: { imageUrl?: string | null }): boolean {
  return !!card.imageUrl && card.imageUrl.startsWith('/uploads/')
}
/**
 * Text-phrase pictogram (e.g. "text:I want") — a short daily-life expression that
 * stands in for an emoji. Rendered as a word tile; its phrase doubles as the label,
 * so render sites show it once instead of icon + duplicate word.
 */
export function isTextCard(card: { imageUrl?: string | null }): boolean {
  return !!card.imageUrl && card.imageUrl.startsWith('text:')
}
export function cardText(card: { imageUrl?: string | null }): string {
  return (card.imageUrl || '').slice('text:'.length)
}

export interface Card {
  id: number
  familyId: string
  category: string
  imageUrl: string
  labelI18n: string
  isCustom: boolean
  sortOrder: number
}

export const useCardStore = defineStore('cards', () => {
  const cards = ref<Card[]>([])
  const loading = ref(false)

  // Full family-wide card set, used by MessageBubble to resolve sentence.cardIds
  // regardless of which category the PECS grid is currently filtered to.
  const allCards = ref<Card[]>([])

  // Sentence bar (A-2)
  const sentenceCards = ref<Card[]>([])

  async function fetchCards(category?: string) {
    loading.value = true
    const auth = useAuthStore()
    const params = new URLSearchParams({ familyId: auth.familyId })
    if (category) params.set('category', category)
    try {
      cards.value = await api<Card[]>(`/api/cards?${params}`)
    } finally {
      loading.value = false
    }
  }

  // Family-wide fetch for chat views. Always refetches so a card the parent
  // just created shows up in the conversation feed without manual reload.
  // Independent of the category-filtered `cards` used by the PECS grid.
  async function fetchAllCards() {
    const auth = useAuthStore()
    const params = new URLSearchParams({ familyId: auth.familyId })
    allCards.value = await api<Card[]>(`/api/cards?${params}`)
  }

  // A-2: Drag card to sentence bar
  function addToSentence(card: Card) {
    if (!sentenceCards.value.find(c => c.id === card.id)) {
      sentenceCards.value.push(card)
    }
  }

  function removeFromSentence(index: number) {
    sentenceCards.value.splice(index, 1)
  }

  function clearSentence() {
    sentenceCards.value = []
  }

  /** Legacy save (no-op kept for old A-2 flow). Use sentences store sendSentence() instead. */
  async function saveSentence() {
    if (sentenceCards.value.length === 0) return
    await api('/api/sentences', {
      method: 'POST',
      body: JSON.stringify({ cardIds: sentenceCards.value.map(c => c.id) }),
    })
    clearSentence()
  }

  // A-4: Parent card management
  async function createCard(card: Partial<Card>): Promise<Card> {
    const auth = useAuthStore()
    const created = await api<Card>('/api/cards', {
      method: 'POST',
      body: JSON.stringify({ ...card, familyId: auth.familyId }),
    })
    cards.value.push(created)
    return created
  }

  async function deleteCard(id: number) {
    await api(`/api/cards/${id}`, { method: 'DELETE' })
    cards.value = cards.value.filter(c => c.id !== id)
  }

  async function reorderCards(ordered: Card[]) {
    await api('/api/cards/reorder', {
      method: 'PUT',
      body: JSON.stringify(ordered),
    })
    ordered.forEach((c, i) => { c.sortOrder = i })
  }

  async function renameCard(id: number, labelI18n: string): Promise<Card> {
    const updated = await api<Card>(`/api/cards/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ labelI18n }),
    })
    const local = cards.value.find(c => c.id === id)
    if (local) local.labelI18n = updated.labelI18n
    return updated
  }

  // A-5: upload photo to backend, returns relative URL
  async function uploadPhoto(file: File): Promise<string> {
    const res = await uploadFile<{ url: string }>('/api/uploads', file)
    return res.url
  }

  // A-5 AC #2: upload → card immediately appears in target category.
  // Label defaults to filename (without extension); parent can rename in the list.
  async function uploadAndCreateCard(file: File, category: string): Promise<Card> {
    const url = await uploadPhoto(file)
    const auth = useAuthStore()
    const baseName = file.name.replace(/\.[^.]+$/, '') || 'Photo'
    const created = await api<Card>('/api/cards', {
      method: 'POST',
      body: JSON.stringify({
        familyId: auth.familyId,
        category,
        labelI18n: baseName,
        imageUrl: url,
        isCustom: true,
      }),
    })
    cards.value.push(created)
    return created
  }

  return {
    cards, allCards, loading, sentenceCards,
    fetchCards, fetchAllCards,
    addToSentence, removeFromSentence, clearSentence, saveSentence,
    createCard, deleteCard, reorderCards, renameCard,
    uploadPhoto, uploadAndCreateCard,
  }
})
