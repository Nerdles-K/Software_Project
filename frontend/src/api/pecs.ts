import { api } from './client'

export type PecsCategory = 'eat' | 'drink' | 'play' | 'feel'
export type PecsCardCategory = PecsCategory | 'core'

export interface SentenceCard {
  /** Unique per drag instance (same pictogram can appear once in sentence) */
  instanceId: string
  card: PictogramCardDto
}

export interface PictogramCardDto {
  id: number
  category: string
  imageUrl: string | null
  labelI18n: string | null
  isCustom: boolean
  sortOrder: number
}

export function fetchCardsByCategory(category: PecsCardCategory): Promise<PictogramCardDto[]> {
  return api<PictogramCardDto[]>(`/api/cards?category=${encodeURIComponent(category)}`)
}

export function cloneToSentence(card: PictogramCardDto): SentenceCard {
  return {
    instanceId: `${card.id}-${Date.now()}`,
    card,
  }
}

/** True when imageUrl is a remote asset; otherwise treat as emoji/symbol text. */
export function isRemoteImage(url: string | null | undefined): boolean {
  return !!url && (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/'))
}
