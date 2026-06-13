import { describe, it, expect } from 'vitest'
import {
  cardIcon, isPhotoCard, isTextCard, cardText,
  CATEGORIES, BOARD_CATEGORIES,
} from './cards'

// Pure rendering helpers that decide how a PECS card is drawn from its imageUrl
// convention (emoji: / text: / /uploads/ / fallback). No Vue or network here.
describe('card icon helpers', () => {
  it('renders an emoji card from the "emoji:" prefix', () => {
    expect(cardIcon({ imageUrl: 'emoji:🍎', category: 'Eat' })).toBe('🍎')
  })

  it('renders a text-phrase card from the "text:" prefix', () => {
    expect(cardIcon({ imageUrl: 'text:I need help', category: 'Need' })).toBe('I need help')
    expect(cardText({ imageUrl: 'text:I need help' })).toBe('I need help')
  })

  it('falls back to the category emoji when there is no usable imageUrl', () => {
    expect(cardIcon({ imageUrl: null, category: 'Eat' })).toBe('🍎')   // Eat category emoji
    expect(cardIcon({ imageUrl: '', category: 'Unknown' })).toBe('📌') // ultimate fallback
  })

  it('classifies photo vs text cards by their url scheme', () => {
    expect(isPhotoCard({ imageUrl: '/uploads/abc.png' })).toBe(true)
    expect(isPhotoCard({ imageUrl: 'emoji:🍎' })).toBe(false)
    expect(isTextCard({ imageUrl: 'text:Hello' })).toBe(true)
    expect(isTextCard({ imageUrl: '/uploads/abc.png' })).toBe(false)
  })
})

describe('category sets', () => {
  it('BOARD_CATEGORIES excludes the chat-grammar categories', () => {
    const keys = BOARD_CATEGORIES.map(c => c.key)
    expect(keys).not.toContain('People')
    expect(keys).not.toContain('Action')
    expect(keys).not.toContain('Time')
    expect(keys).toContain('Need')
    expect(keys).toContain('Feel')
  })

  it('every category carries a non-empty emoji', () => {
    expect(CATEGORIES.every(c => c.emoji.length > 0)).toBe(true)
  })
})
