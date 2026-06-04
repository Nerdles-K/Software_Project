import { api } from './client'
import { useAuthStore } from '../stores/auth'

export type PecsCategory = 'Eat' | 'Drink' | 'Play' | 'Feel'
export type StarterCategory = 'Action'

export interface PictogramCardDto {
  id: number
  category: string
  imageUrl: string | null
  labelI18n: string | null
  isCustom: boolean
  sortOrder: number
}

export interface SentenceCard {
  instanceId: string
  card: PictogramCardDto
}

export function fetchCardsByCategory(category: string): Promise<PictogramCardDto[]> {
  const auth = useAuthStore()
  const params = new URLSearchParams({ familyId: auth.familyId, category })
  return api<PictogramCardDto[]>(`/api/cards?${params}`)
}

export function cloneToSentence(card: PictogramCardDto): SentenceCard {
  return {
    instanceId: `${card.id}-${Date.now()}`,
    card,
  }
}

/** Display emoji from team seed format (emoji:🍎) or plain emoji. */
export function cardDisplaySymbol(imageUrl: string | null | undefined): string {
  if (!imageUrl) return '📌'
  if (imageUrl.startsWith('emoji:')) return imageUrl.slice('emoji:'.length)
  return imageUrl
}

export function isRemoteImage(url: string | null | undefined): boolean {
  return !!url && (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/'))
}
