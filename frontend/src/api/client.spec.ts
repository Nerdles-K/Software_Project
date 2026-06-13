import { describe, it, expect } from 'vitest'
import { assetUrl } from './client'

// assetUrl turns a stored path into something the browser can load: absolute URLs
// pass through, /uploads/* get the API base prepended, everything else is returned as-is.
describe('assetUrl', () => {
  it('returns empty string for nullish input', () => {
    expect(assetUrl(null)).toBe('')
    expect(assetUrl(undefined)).toBe('')
    expect(assetUrl('')).toBe('')
  })

  it('passes absolute http(s) URLs through untouched', () => {
    expect(assetUrl('https://cdn.example.com/x.png')).toBe('https://cdn.example.com/x.png')
    expect(assetUrl('http://foo/bar.jpg')).toBe('http://foo/bar.jpg')
  })

  it('prepends the API base to /uploads paths', () => {
    // Default base (no VITE_API_BASE_URL in test env) is http://localhost:8080.
    expect(assetUrl('/uploads/pic.png')).toBe('http://localhost:8080/uploads/pic.png')
  })

  it('leaves emoji/text pseudo-urls as-is', () => {
    expect(assetUrl('emoji:🍎')).toBe('emoji:🍎')
  })
})
