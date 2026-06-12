import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api/client'
import { useCardStore, type Card } from './cards'

export interface Sentence {
  id: number
  familyId: string
  senderRole: 'child' | 'parent'
  cardIds: number[]
  createdAt: string
}

/**
 * Family conversation feed (PECS messages). Both child and parent see the
 * same thread; sender_role drives left/right alignment in the UI.
 */
export const useSentenceStore = defineStore('sentences', () => {
  const messages = ref<Sentence[]>([])
  const lastSeenId = computed(() => messages.value.at(-1)?.id ?? 0)
  const loading = ref(false)

  async function fetchAll() {
    loading.value = true
    try {
      messages.value = await api<Sentence[]>('/api/sentences')
    } finally {
      loading.value = false
    }
  }

  /** Append-only poll. Safe to call repeatedly. */
  async function pollNew() {
    const since = lastSeenId.value
    const fresh = await api<Sentence[]>(`/api/sentences?sinceId=${since}`)
    if (fresh.length > 0) messages.value.push(...fresh)
    return fresh
  }

  async function send(cardIds: number[]) {
    if (cardIds.length === 0) return
    const s = await api<Sentence>('/api/sentences', {
      method: 'POST',
      body: JSON.stringify({ cardIds }),
    })
    messages.value.push(s)
    return s
  }

  /**
   * Resolve card ids to full Card objects for rendering. Skips unknown ids.
   * Prefers `allCards` (family-wide) because the category-filtered `cards`
   * would drop messages whose cards are in a non-active category.
   */
  function expand(s: Sentence): Card[] {
    const store = useCardStore()
    const pool = store.allCards.length > 0 ? store.allCards : store.cards
    const byId = new Map(pool.map(c => [c.id, c]))
    return s.cardIds.map(id => byId.get(id)).filter((c): c is Card => !!c)
  }

  return { messages, loading, lastSeenId, fetchAll, pollNew, send, expand }
})
